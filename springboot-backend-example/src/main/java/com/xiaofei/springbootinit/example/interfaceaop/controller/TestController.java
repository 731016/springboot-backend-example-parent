package com.xiaofei.springbootinit.example.interfaceaop.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.interfaceaop.annotation.ApiLog;
import com.xiaofei.springbootinit.example.interfaceaop.annotation.Replayable;
import com.xiaofei.springbootinit.example.interfaceaop.model.TestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@RestController("interface")
@RequestMapping("/interface/aop")
@Slf4j
public class TestController {

    @Replayable("可重放的测试接口")
    @ApiLog
    @PostMapping("/test")
    public BaseResponse<String> test(@RequestBody TestRequest request) {
        // 业务逻辑
        log.info("收到请求: {}", request);
        return ResultUtils.success("ok");
    }

}
