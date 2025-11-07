package com.xiaofei.springbootinit.example.datasource.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@RestController("dataSource")
@RequestMapping("/dataSource")
public class DataSourceController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/test1")
    public BaseResponse<String> test1() {
        boolean save = userService.synchronizationUser();
        if (save) {
            return ResultUtils.success("插入成功");
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "插入失败");
    }

    @PostMapping("/test2")
    public BaseResponse<String> test2() {
        boolean insert = userService.execInsert();
        if (insert) {
            return ResultUtils.success("插入成功");
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "插入失败");
    }

    @PostMapping("/test3")
    public BaseResponse<User> test3() {
        User user = userService.queryLastNewUser();
        if (user != null) {
            return ResultUtils.success(user);
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询失败");
    }

}
