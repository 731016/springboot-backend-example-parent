package com.xiaofei.springbootbackendkafka.collector;

import cn.hutool.core.util.StrUtil;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaPoint;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * OPC UA 采集适配器
 *
 * @author tuaofei
 */
@Slf4j
@Component
public class OpcUaCollectorAdapter implements CollectorAdapter {

    private static final int DEFAULT_TIMEOUT_MILLIS = 5000;

    private final Map<String, OpcUaClient> clients = new ConcurrentHashMap<>();

    @Override
    public CollectorProtocol protocol() {
        return CollectorProtocol.OPC_UA;
    }

    @Override
    public void testConnection(CollectorSourceSnapshot source) {
        CollectorOpcUaConfig config = requireConfig(source);
        try {
            getOrConnect(config);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            invalidate(config);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPC UA连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public CollectResult collect(CollectorSourceSnapshot source, PointBinding binding, Date collectTime) {
        CollectorOpcUaConfig config = requireConfig(source);
        if (binding == null || binding.getOpcUaPoint() == null
                || StrUtil.isBlank(binding.getOpcUaPoint().getNodeId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "OPC UA点位缺少NodeId配置");
        }
        CollectorOpcUaPoint point = binding.getOpcUaPoint();
        int timeout = config.getRequestTimeoutMillis() == null ? DEFAULT_TIMEOUT_MILLIS : config.getRequestTimeoutMillis();
        try {
            OpcUaClient client = getOrConnect(config);
            ReadValueId readValueId = new ReadValueId(NodeId.parse(point.getNodeId()),
                    AttributeId.Value.uid(), null, null);
            ReadResponse response = client.read(0.0, TimestampsToReturn.Both,
                    Collections.singletonList(readValueId)).get(timeout, TimeUnit.MILLISECONDS);
            DataValue dataValue = response.getResults()[0];
            if (dataValue.getStatusCode() != null && !dataValue.getStatusCode().isGood()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "OPC UA读取状态异常: " + dataValue.getStatusCode());
            }
            Object rawValue = dataValue.getValue() == null ? null : dataValue.getValue().getValue();
            String attributeName = StrUtil.isBlank(point.getAttributeName())
                    ? binding.getPointCode() : point.getAttributeName();
            return new CollectResult(toBigDecimal(rawValue), attributeName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            invalidate(config);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPC UA采集失败: " + e.getMessage());
        }
    }

    private OpcUaClient getOrConnect(CollectorOpcUaConfig config) throws Exception {
        String key = clientKey(config);
        OpcUaClient client = clients.get(key);
        if (client != null) {
            return client;
        }
        synchronized (clients) {
            client = clients.get(key);
            if (client == null) {
                client = createClient(config);
                clients.put(key, client);
            }
        }
        int timeout = config.getRequestTimeoutMillis() == null ? DEFAULT_TIMEOUT_MILLIS : config.getRequestTimeoutMillis();
        client.connect().get(timeout, TimeUnit.MILLISECONDS);
        log.info("OPC UA连接成功: {}", config.getEndpointUrl());
        return client;
    }

    private OpcUaClient createClient(CollectorOpcUaConfig config) {
        try {
            SecurityPolicy securityPolicy = resolveSecurityPolicy(config.getSecurityPolicy());
            Function<List<EndpointDescription>, Optional<EndpointDescription>> endpointSelection =
                    endpoints -> endpoints.stream()
                            .filter(endpoint -> Objects.equals(endpoint.getSecurityPolicyUri(), securityPolicy.getUri()))
                            .findFirst();
            IdentityProvider identityProvider = StrUtil.isBlank(config.getUsername())
                    ? new AnonymousProvider()
                    : new UsernameProvider(config.getUsername(), config.getPassword());
            int timeout = config.getRequestTimeoutMillis() == null
                    ? DEFAULT_TIMEOUT_MILLIS : config.getRequestTimeoutMillis();
            return OpcUaClient.create(
                    config.getEndpointUrl(),
                    endpointSelection,
                    builder -> builder
                            .setApplicationName(LocalizedText.english("spc-data-collector"))
                            .setApplicationUri("urn:spc:data-collector")
                            .setRequestTimeout(Unsigned.uint(timeout))
                            .setConnectTimeout(Unsigned.uint(timeout))
                            .setIdentityProvider(identityProvider)
                            .build());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建OPC UA客户端失败: " + e.getMessage());
        }
    }

    private SecurityPolicy resolveSecurityPolicy(String value) {
        String name = StrUtil.blankToDefault(value, "None");
        try {
            return SecurityPolicy.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的OPC UA安全策略: " + value);
        }
    }

    private void invalidate(CollectorOpcUaConfig config) {
        OpcUaClient client = clients.remove(clientKey(config));
        if (client != null) {
            try {
                client.disconnect().get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("关闭OPC UA连接失败: {}", config.getEndpointUrl(), e);
            }
        }
    }

    private String clientKey(CollectorOpcUaConfig config) {
        return config.getEndpointUrl() + "|" + StrUtil.blankToDefault(config.getUsername(), "");
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        if (value == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPC UA节点值为空");
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPC UA节点值不是有效数值: " + value);
        }
    }

    private CollectorOpcUaConfig requireConfig(CollectorSourceSnapshot source) {
        if (source == null || source.getOpcUaConfig() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "OPC UA采集源配置不存在");
        }
        return source.getOpcUaConfig();
    }

    @PreDestroy
    public void shutdown() {
        clients.forEach((key, client) -> {
            try {
                client.disconnect().get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("关闭OPC UA连接失败: {}", key, e);
            }
        });
        clients.clear();
    }
}
