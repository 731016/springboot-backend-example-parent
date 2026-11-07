package com.xiaofei.springbootbackendkafka.collector;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorHttpConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * HTTP 采集适配器
 *
 * @author tuaofei
 */
@Slf4j
@Component
public class HttpCollectorAdapter implements CollectorAdapter {

    private static final String DEFAULT_TEMPLATE = "{\"pointCode\":\"${pointCode}\",\"collectTime\":\"${collectTime}\"}";

    private static final int DEFAULT_TIMEOUT_MILLIS = 5000;

    @Override
    public CollectorProtocol protocol() {
        return CollectorProtocol.HTTP;
    }

    @Override
    public void testConnection(CollectorSourceSnapshot source) {
        CollectorHttpConfig config = requireConfig(source);
        String url = StrUtil.isNotBlank(config.getValidUrl()) ? config.getValidUrl() : config.getBaseUrl();
        HttpResponse response = buildRequest(config, url, "TEST", new Date()).execute();
        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    String.format("HTTP连接测试失败: status=%s, body=%s", response.getStatus(), response.body()));
        }
    }

    @Override
    public CollectResult collect(CollectorSourceSnapshot source, PointBinding binding, Date collectTime) {
        CollectorHttpConfig config = requireConfig(source);
        if (binding == null || StrUtil.isBlank(binding.getPointCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTTP采集缺少点位编码");
        }
        String body = buildRequest(config, config.getBaseUrl(), binding.getPointCode(), collectTime).execute().body();
        return parseBody(config, body);
    }

    private HttpRequest buildRequest(CollectorHttpConfig config, String url, String pointCode, Date collectTime) {
        int timeout = config.getTimeoutMillis() == null ? DEFAULT_TIMEOUT_MILLIS : config.getTimeoutMillis();
        HttpRequest request = HttpUtil.createRequest(resolveMethod(config.getMethod()), url)
                .header("Content-Type", StrUtil.blankToDefault(config.getContentType(), "application/json"))
                .setConnectionTimeout(timeout)
                .setReadTimeout(timeout)
                .body(renderTemplate(config.getRequestTemplate(), pointCode, collectTime));
        if (StrUtil.isNotBlank(config.getHeaders())) {
            JSONObject headers = JSONUtil.parseObj(config.getHeaders());
            for (String key : headers.keySet()) {
                request.header(key, String.valueOf(headers.get(key)));
            }
        }
        return request;
    }

    private Method resolveMethod(String method) {
        if (StrUtil.isBlank(method)) {
            return Method.POST;
        }
        try {
            return Method.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的HTTP方法: " + method);
        }
    }

    private String renderTemplate(String template, String pointCode, Date collectTime) {
        String content = StrUtil.isBlank(template) ? DEFAULT_TEMPLATE : template;
        return content.replace("${pointCode}", pointCode)
                .replace("${collectTime}", DateUtil.formatDateTime(collectTime));
    }

    private CollectResult parseBody(CollectorHttpConfig config, String body) {
        if (StrUtil.isBlank(body)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "HTTP采集响应为空");
        }
        Object root;
        try {
            root = JSONUtil.parse(body);
        } catch (Exception e) {
            root = body;
        }
        Object node = navigate(root, config.getResponseDataPath());
        if (node == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "HTTP响应中未找到数据节点: " + config.getResponseDataPath());
        }
        String valueField = StrUtil.blankToDefault(config.getValueField(), "value");
        String attributeNameField = StrUtil.blankToDefault(config.getAttributeNameField(), "attributeName");
        BigDecimal value;
        String attributeName = null;
        if (node instanceof JSONObject) {
            JSONObject data = (JSONObject) node;
            Object rawValue = data.get(valueField);
            if (rawValue == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "HTTP响应中未找到值字段: " + valueField);
            }
            value = toBigDecimal(rawValue);
            Object rawAttributeName = data.get(attributeNameField);
            attributeName = rawAttributeName == null ? null : String.valueOf(rawAttributeName);
        } else {
            value = toBigDecimal(node);
        }
        return new CollectResult(value, attributeName);
    }

    private Object navigate(Object node, String path) {
        if (StrUtil.isBlank(path)) {
            return node;
        }
        Object current = node;
        for (String segment : path.split("\\.")) {
            if (current instanceof JSONObject) {
                current = ((JSONObject) current).get(segment);
            } else if (current instanceof JSONArray) {
                int index;
                try {
                    index = Integer.parseInt(segment);
                } catch (NumberFormatException e) {
                    return null;
                }
                JSONArray array = (JSONArray) current;
                current = index >= 0 && index < array.size() ? array.get(index) : null;
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private BigDecimal toBigDecimal(Object value) {
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "采集值不是有效数值: " + value);
        }
    }

    private CollectorHttpConfig requireConfig(CollectorSourceSnapshot source) {
        if (source == null || source.getHttpConfig() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTTP采集源配置不存在");
        }
        return source.getHttpConfig();
    }
}
