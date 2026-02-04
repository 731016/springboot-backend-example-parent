package com.xiaofei.springbootinit.example.commonserviceExec.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.example.commonserviceExec.utils.ServiceUtils;
import com.xiaofei.springbootinit.model.entity.User;
import org.apache.poi.ss.formula.functions.T;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ServiceUtils serviceUtils;

    @PostMapping("/exec")
    public BaseResponse<T> commonExec(@RequestBody CommonServiceRequest request) {
        return serviceUtils.commonExec(request);
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
