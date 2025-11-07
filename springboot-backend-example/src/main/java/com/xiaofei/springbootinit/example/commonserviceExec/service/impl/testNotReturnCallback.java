package com.xiaofei.springbootinit.example.commonserviceExec.service.impl;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.example.commonserviceExec.service.ServiceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Slf4j
@Component("testNotReturnCallback")
public class testNotReturnCallback implements ServiceProcessor {

    @Override
    public void process(String serviceName, String methodName,
                        CommonServiceRequest<?> request, BaseResponse<?> response) {
        log.info("Service: {}, Method: {}, Request: {}, Response: {}",
                serviceName, methodName, request, response);
        int i = 1 / 0;
        log.info("模拟执行 {testNotReturnCallback}");
    }
}
