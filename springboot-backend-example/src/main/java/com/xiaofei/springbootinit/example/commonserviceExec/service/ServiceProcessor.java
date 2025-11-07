package com.xiaofei.springbootinit.example.commonserviceExec.service;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;

/**
 * @author tuaofei
 * @description 处理器接口
 * @date 2024/12/24
 */
public interface ServiceProcessor {

    /**
     * 处理方法
     */
    void process(String serviceName, String methodName,
                 CommonServiceRequest<?> request, BaseResponse<?> response);
}
