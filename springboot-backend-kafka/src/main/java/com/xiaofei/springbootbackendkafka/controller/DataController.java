package com.xiaofei.springbootbackendkafka.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendkafka.model.dto.CollectedData;
import com.xiaofei.springbootbackendkafka.model.dto.QueryCollectTask;
import com.xiaofei.springbootbackendkafka.model.vo.TaskStatusVO;
import com.xiaofei.springbootbackendkafka.utils.RandomDataUtils;
import com.xiaofei.springbootbackendkafka.utils.TimeUtils;
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
@RestController("/kafka/dataQuery")
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
