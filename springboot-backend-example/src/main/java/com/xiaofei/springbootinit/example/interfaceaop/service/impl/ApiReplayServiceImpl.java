package com.xiaofei.springbootinit.example.interfaceaop.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.example.interfaceaop.mapper.ApiRequestRecordMapper;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiRequestRecord;
import com.xiaofei.springbootinit.example.interfaceaop.service.ApiReplayService;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */

@Service
@Slf4j
public class ApiReplayServiceImpl implements ApiReplayService {

    @Resource
    private ApiRequestRecordMapper apiRequestRecordMapper;

    @Override
    public Object replayRequest(Long recordId) {
        // 获取历史请求记录
        ApiRequestRecord record = apiRequestRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 创建计时器
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            // 创建请求对象
            HttpRequest request = createHttpRequest(record);

            // 设置 Content-Type
            String contentType = record.getContentType();
            if (StringUtils.isBlank(contentType)) {
                // 默认使用 application/json
                contentType = "application/json";
            }
            request.header("Content-Type", contentType);

            // 设置其他请求头
            if (StringUtils.isNotBlank(record.getHeaders())) {
                JSONObject headerJson = JSONUtil.parseObj(record.getHeaders());
                headerJson.forEach((key, value) -> {
                    if (!"content-length".equalsIgnoreCase(key)) {  // 跳过 content-length
                        request.header(key, String.valueOf(value));
                    }
                });
            }

            // 设置请求体
            if (StringUtils.isNotBlank(record.getRequestParams())) {
                if (StringUtils.containsIgnoreCase(contentType, "application/json")) {
                    // JSON 格式
                    request.body(record.getRequestParams());
                } else if (StringUtils.containsIgnoreCase(contentType, "application/x-www-form-urlencoded")) {
                    // 表单格式
                    JSONObject params = JSONUtil.parseObj(record.getRequestParams());
                    params.forEach((key, value) -> request.form(key, value));
                } else if (StringUtils.containsIgnoreCase(contentType, "multipart/form-data")) {
                    // 文件上传格式
                    JSONObject params = JSONUtil.parseObj(record.getRequestParams());
                    params.forEach((key, value) -> request.form(key, value));
                } else {
                    // 其他格式，直接设置为字符串
                    request.body(record.getRequestParams());
                }
            }

            // 执行请求
            HttpResponse response = request.execute();

            stopWatch.stop();

            // 保存重放结果
            ApiRequestRecord replayRecord = new ApiRequestRecord();
            BeanUtils.copyProperties(record, replayRecord);
            replayRecord.setId(null);
            replayRecord.setResponseData(response.body());
            replayRecord.setStatus(response.getStatus());
            replayRecord.setCreateTime(new Date());
            replayRecord.setTimeConsumed(stopWatch.getLastTaskTimeMillis());
            apiRequestRecordMapper.insert(replayRecord);

            return response.body();

        } catch (Exception e) {
            log.error("接口重放失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口重放失败：" + e.getMessage());
        }
    }

    /**
     * 根据记录创建对应的 HttpRequest
     */
    private HttpRequest createHttpRequest(ApiRequestRecord record) {
        String method = record.getHttpMethod().toUpperCase();
        switch (method) {
            case "GET":
                return HttpUtil.createGet(record.getUrl());
            case "POST":
                return HttpUtil.createPost(record.getUrl());
            case "PUT":
                return HttpUtil.createRequest(Method.PUT, record.getUrl());
            case "DELETE":
                return HttpUtil.createRequest(Method.DELETE, record.getUrl());
            case "PATCH":
                return HttpUtil.createRequest(Method.PATCH, record.getUrl());
            case "HEAD":
                return HttpUtil.createRequest(Method.HEAD, record.getUrl());
            case "OPTIONS":
                return HttpUtil.createRequest(Method.OPTIONS, record.getUrl());
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的 HTTP 方法：" + record.getHttpMethod());
        }
    }

}
