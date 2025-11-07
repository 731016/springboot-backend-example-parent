package com.xiaofei.springbootinit.example.kafka.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.kafka.model.vo.TaskStatusVO;
import com.xiaofei.springbootinit.example.kafka.service.DataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@RestController
@RequestMapping("/collection")
@Slf4j
public class DataCollectionController {

    @Autowired
    private DataCollectionService dataCollectionService;

    /**
     * 启动采集任务
     */
    @PostMapping("/start/{pointCode}")
    public BaseResponse<String> startCollection(@PathVariable String pointCode) {
        try {
            dataCollectionService.startCollection(pointCode);
            return ResultUtils.success("采集任务启动成功");
        } catch (Exception e) {
            log.error("启动采集任务失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "启动采集任务失败：" + e.getMessage());
        }
    }

    /**
     * 停止采集任务
     */
    @PostMapping("/stop/{pointCode}")
    public BaseResponse<String> stopCollection(@PathVariable String pointCode) {
        try {
            dataCollectionService.stopCollection(pointCode);
            return ResultUtils.success("采集任务停止成功");
        } catch (Exception e) {
            log.error("停止采集任务失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "停止采集任务失败：" + e.getMessage());
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/status/{pointCode}")
    public BaseResponse<TaskStatusVO> getCollectionStatus(@PathVariable String pointCode) {
        try {
            TaskStatusVO status = dataCollectionService.getCollectionStatus(pointCode);
            return ResultUtils.success(status);
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "获取任务状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有任务状态
     */
    @GetMapping("/status/all")
    public BaseResponse<List<TaskStatusVO>> getAllCollectionStatus() {
        try {
            List<TaskStatusVO> statusList = dataCollectionService.getAllCollectionStatus();
            return ResultUtils.success(statusList);
        } catch (Exception e) {
            log.error("获取所有任务状态失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "获取所有任务状态失败：" + e.getMessage());
        }
    }
}
