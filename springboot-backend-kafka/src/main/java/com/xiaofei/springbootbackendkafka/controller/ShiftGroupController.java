package com.xiaofei.springbootbackendkafka.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.dto.ShiftGroupQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.ShiftGroupSaveRequest;
import com.xiaofei.springbootbackendkafka.model.entity.ShiftGroup;
import com.xiaofei.springbootbackendkafka.service.ShiftGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 班次班组基础信息维护接口
 */
@RestController
@RequestMapping("/kafka/shiftGroup")
@Slf4j
public class ShiftGroupController {

    @Autowired
    private ShiftGroupService shiftGroupService;

    /**
     * 新增 / 修改
     */
    @PostMapping("/save")
    public BaseResponse<Long> save(@RequestBody @Valid ShiftGroupSaveRequest request) {
        Long id = shiftGroupService.saveShiftGroup(request);
        return ResultUtils.success(id);
    }

    /**
     * 删除
     */
    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> delete(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID 非法");
        }
        boolean result = shiftGroupService.deleteById(id);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ShiftGroup>> listByPage(@RequestBody ShiftGroupQueryRequest queryRequest) {
        Page<ShiftGroup> page = shiftGroupService.listByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据 ID 获取详情
     */
    @GetMapping("/get/{id}")
    public BaseResponse<ShiftGroup> getById(@PathVariable("id") Long id) {
        ShiftGroup shiftGroup = shiftGroupService.getById(id);
        return ResultUtils.success(shiftGroup);
    }
}

