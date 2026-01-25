package com.xiaofei.springbootinit.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootinit.annotation.ApiLog;
import com.xiaofei.springbootinit.annotation.Replayable;
import com.xiaofei.springbootinit.model.entity.TestRequest;
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
