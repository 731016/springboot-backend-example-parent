package com.xiaofei.springbootinit.example.commonserviceExec.service.impl;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.example.commonserviceExec.service.TestService;
import com.xiaofei.springbootinit.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Service("testService")
@Slf4j
public class TestServiceImpl implements TestService {
    @Override
    public void testNotReturn(CommonServiceRequest request, BaseResponse response) {
        log.info("exec testNotReturn");
    }

    @Override
    public BaseResponse<User> testParameters(CommonServiceRequest<User> request, BaseResponse<User> response) {
        log.info("exec testParameters");

        User user = request.getRequestData();
        user.setUserName("xiaofei");
        return ResultUtils.success(user);
    }
}
