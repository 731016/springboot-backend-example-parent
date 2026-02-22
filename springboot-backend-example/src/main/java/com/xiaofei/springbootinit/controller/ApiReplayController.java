package com.xiaofei.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootinit.model.dto.apireplay.ApiRequestRecordQueryRequest;
import com.xiaofei.springbootinit.model.entity.ApiRequestRecord;
import com.xiaofei.springbootinit.service.ApiReplayService;
import com.xiaofei.springbootinit.mapper.ApiRequestRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 接口重放：查询已执行接口记录并支持重放
 */
@RestController
@RequestMapping("/replay")
@Slf4j
public class ApiReplayController {

    @Resource
    private ApiReplayService apiReplayService;

    @Resource
    private ApiRequestRecordMapper apiRequestRecordMapper;

    @PostMapping("/execute")
    public BaseResponse<Object> replayRequest(@RequestParam Long recordId) {
        return ResultUtils.success(apiReplayService.replayRequest(recordId));
    }

    @GetMapping("/records")
    public BaseResponse<List<ApiRequestRecord>> getReplayableRecords() {
        LambdaQueryWrapper<ApiRequestRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ApiRequestRecord::getCreateTime);
        return ResultUtils.success(apiRequestRecordMapper.selectList(queryWrapper));
    }

    /**
     * 分页查询已执行接口记录
     */
    @PostMapping("/records/page")
    public BaseResponse<Page<ApiRequestRecord>> getReplayableRecordsByPage(
            @RequestBody ApiRequestRecordQueryRequest queryRequest) {
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        LambdaQueryWrapper<ApiRequestRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getUrl()), ApiRequestRecord::getUrl, queryRequest.getUrl());
        queryWrapper.eq(StringUtils.isNotBlank(queryRequest.getHttpMethod()), ApiRequestRecord::getHttpMethod, queryRequest.getHttpMethod());
        queryWrapper.orderByDesc(ApiRequestRecord::getCreateTime);
        Page<ApiRequestRecord> page = apiRequestRecordMapper.selectPage(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page);
    }
}
