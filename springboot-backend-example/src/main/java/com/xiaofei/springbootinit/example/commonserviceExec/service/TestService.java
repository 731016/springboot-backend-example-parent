package com.xiaofei.springbootinit.example.commonserviceExec.service;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.model.entity.User;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
public interface TestService {

    void testNotReturn(CommonServiceRequest request, BaseResponse response);

    BaseResponse<User> testParameters(CommonServiceRequest<User> request, BaseResponse<User> response);
}
