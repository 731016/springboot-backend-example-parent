package com.xiaofei.springbootinit.example.commonserviceExec.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.example.commonserviceExec.utils.ServiceUtils;
import com.xiaofei.springbootinit.model.entity.User;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@RestController("service")
@RequestMapping("/common/service")
public class TestController {

    @PostMapping("/exec")
    public BaseResponse<T> commonExec(@RequestBody CommonServiceRequest request) {
        return ServiceUtils.commonExec(request);
    }

    @PostMapping("/textExec")
    public BaseResponse<T> textExec() {
        CommonServiceRequest serviceRequest = new CommonServiceRequest();
        serviceRequest.setServiceName("testService");
        serviceRequest.setMethodName("testNotReturn");
        return commonExec(serviceRequest);
    }

    @PostMapping("/testParametersExec")
    public BaseResponse<T> testParametersExec() {
        User user = new User();
        CommonServiceRequest serviceRequest = new CommonServiceRequest();
        serviceRequest.setServiceName("testService");
        serviceRequest.setMethodName("testParameters");
        serviceRequest.setRequestData(user);
        return commonExec(serviceRequest);
    }

}
