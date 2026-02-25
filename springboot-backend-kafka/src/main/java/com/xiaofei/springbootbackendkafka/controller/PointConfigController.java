package com.xiaofei.springbootbackendkafka.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.dto.AddPointConfigRequest;
import com.xiaofei.springbootbackendkafka.model.dto.PointConfigQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.UpdatePointConfigRequest;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import com.xiaofei.springbootbackendkafka.service.PointConfigService;
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
@RequestMapping("/kafka/kafkaPointConfig")
@Slf4j
public class PointConfigController {

    @Autowired
    private PointConfigService pointConfigService;

    @PostMapping("/point/add")
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

    /**
     * 更新采集点配置
     */
    @PostMapping("/point/update")
    public BaseResponse<Boolean> updatePointConfig(@RequestBody @Valid UpdatePointConfigRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验限值
        if (request.getMaxLimit() != null && request.getMinLimit() != null
                && request.getMaxLimit().compareTo(request.getMinLimit()) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最大限值不能小于最小限值");
        }

        pointConfigService.updatePointConfig(request);
        return ResultUtils.success(true);
    }

    /**
     * 分页查询采集点配置
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/point/list/page")
    public BaseResponse<Page<PointConfig>> listPointConfigByPage(@RequestBody PointConfigQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Page<PointConfig> pointConfigPage = pointConfigService.listPointConfigByPage(queryRequest);
        return ResultUtils.success(pointConfigPage);
    }
}
