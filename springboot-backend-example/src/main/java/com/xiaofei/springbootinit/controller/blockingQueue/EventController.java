package com.xiaofei.springbootinit.controller.blockingQueue;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootinit.service.blockingQueue.EventTracker;
import com.xiaofei.springbootinit.model.dto.blockingQueue.EventQueueStatusVO;
import com.xiaofei.springbootinit.model.dto.blockingQueue.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author tuaofei
 * @description 工作队列事件处理
 * @date 2025/3/3
 */
@RestController
@RequestMapping("/event")
@Slf4j
public class EventController {

    @Resource
    private EventTracker eventTracker;

    /**
     * 获取队列监控状态（队列数量、工作线程数、是否启用）
     */
    @GetMapping("/status")
    public BaseResponse<EventQueueStatusVO> getStatus() {
        EventQueueStatusVO vo = EventQueueStatusVO.builder()
                .queueSize(eventTracker.getEventSize())
                .curWorkerNum(eventTracker.getCurWorkerNum())
                .maxWorkers(eventTracker.getMaxWorkers())
                .enable(eventTracker.isEnable())
                .build();
        return ResultUtils.success(vo);
    }

    /**
     * 手动投递事件到队列
     */
    @PostMapping("/receive")
    public BaseResponse<String> receiveEvent(@RequestBody RequestData requestData) {
        eventTracker.addEvent(requestData);
        return ResultUtils.success("队列已接收数据：" + JSONUtil.toJsonStr(requestData));
    }

    /**
     * 队列中事件数量（兼容旧接口）
     */
    @PostMapping("/getEventCount")
    public BaseResponse<String> getEventCount() {
        return ResultUtils.success("队列中事件数量为：" + eventTracker.getEventSize());
    }
}
