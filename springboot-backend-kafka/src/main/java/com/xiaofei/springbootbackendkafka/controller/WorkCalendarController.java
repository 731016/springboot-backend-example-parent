package com.xiaofei.springbootbackendkafka.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.dto.WorkCalendarQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.WorkCalendarSaveRequest;
import com.xiaofei.springbootbackendkafka.model.entity.WorkCalendar;
import com.xiaofei.springbootbackendkafka.service.WorkCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 工作日历维护接口
 */
@RestController
@RequestMapping("/kafka/workCalendar")
@Slf4j
public class WorkCalendarController {

    @Autowired
    private WorkCalendarService workCalendarService;

    /**
     * 新增 / 修改
     */
    @PostMapping("/save")
    public BaseResponse<Long> save(@RequestBody @Valid WorkCalendarSaveRequest request) {
        Long id = workCalendarService.saveWorkCalendar(request);
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
        boolean result = workCalendarService.deleteById(id);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<WorkCalendar>> listByPage(
            @RequestBody WorkCalendarQueryRequest queryRequest) {
        Page<WorkCalendar> page = workCalendarService.listByPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 根据 ID 获取详情
     */
    @GetMapping("/get/{id}")
    public BaseResponse<WorkCalendar> getById(@PathVariable("id") Long id) {
        WorkCalendar workCalendar = workCalendarService.getById(id);
        return ResultUtils.success(workCalendar);
    }
}