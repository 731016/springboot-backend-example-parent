package com.xiaofei.springbootinit.example.kafka.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.kafka.model.dto.AddPointConfigRequest;
import com.xiaofei.springbootinit.example.kafka.service.PointConfigService;
import com.xiaofei.springbootinit.exception.BusinessException;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@RestController
@RequestMapping("/point")
@Slf4j
public class PointConfigController {

    @Autowired
    private PointConfigService pointConfigService;

    @PostMapping("/add")
    public BaseResponse<Long> addPointConfig(@RequestBody @Valid AddPointConfigRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验限值
        if (request.getMaxLimit() != null && request.getMinLimit() != null
                && request.getMaxLimit().compareTo(request.getMinLimit()) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最大限值不能小于最小限值");
        }

        long pointId = pointConfigService.addPointConfig(request);
        return ResultUtils.success(pointId);
    }
}
