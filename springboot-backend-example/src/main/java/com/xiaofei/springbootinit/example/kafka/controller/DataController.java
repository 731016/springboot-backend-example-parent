package com.xiaofei.springbootinit.example.kafka.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.kafka.model.dto.CollectedData;
import com.xiaofei.springbootinit.example.kafka.model.dto.QueryCollectTask;
import com.xiaofei.springbootinit.example.kafka.model.vo.TaskStatusVO;
import com.xiaofei.springbootinit.example.kafka.utils.RandomDataUtils;
import com.xiaofei.springbootinit.example.kafka.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@RestController
@Slf4j
public class DataController {

    /**
     * 查询数据是否生效
     *
     * @param queryCollectTask
     * @return
     */
    @PostMapping("/queryCollectTaskIsStart")
    public BaseResponse<TaskStatusVO> queryCollectTaskIsStart(@RequestBody QueryCollectTask queryCollectTask) {
        Date collectTime = queryCollectTask.getCollectTime();
        TaskStatusVO taskStatusVO = new TaskStatusVO();
        taskStatusVO.setLastCollectTime(collectTime);
        if (queryCollectTask == null) {
            return ResultUtils.success(taskStatusVO);
        }
        if (TimeUtils.isInWorkingHours(collectTime)) {
            taskStatusVO.setRunning(true);
        } else {
            taskStatusVO.setRunning(false);
        }
        return ResultUtils.success(taskStatusVO);
    }

    /**
     * 采集数据
     * @param queryCollectTask
     * @return
     */
    @PostMapping("/queryCollectData")
    public BaseResponse<CollectedData> queryCollectData(@RequestBody QueryCollectTask queryCollectTask) {
        Date collectTime = queryCollectTask.getCollectTime();
        String pointCode = queryCollectTask.getPointCode();
        CollectedData collectedData = new CollectedData();
        collectedData.setCollectTime(collectTime);
        collectedData.setPointCode(pointCode);
        if (queryCollectTask == null) {
            return ResultUtils.success(collectedData);
        }
        BigDecimal val = RandomDataUtils.randomWithFluctuation(new BigDecimal("50"), 10, 0);
        collectedData.setValue(val);
        return ResultUtils.success(collectedData);
    }
}
