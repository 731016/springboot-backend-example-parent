package com.xiaofei.springbootinit.example.interfaceaop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiRequestRecord;
import com.xiaofei.springbootinit.example.interfaceaop.service.ApiReplayService;
import com.xiaofei.springbootinit.example.interfaceaop.mapper.ApiRequestRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
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
}
