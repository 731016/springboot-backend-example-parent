package com.xiaofei.springbootinit.example.blockingQueue.controller;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.example.blockingQueue.EventTracker;
import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author tuaofei
 * @description TODO
 * @date 2025/3/3
 */
@RestController
@RequestMapping("/event")
@Slf4j
public class EventController {


    @Resource
    private EventTracker eventTracker;

    @PostMapping("/receive")
    public String receiveEvent(@RequestBody RequestData requestData) {
        eventTracker.addEvent(requestData);
        return "队列已接收数据" + JSONUtil.toJsonStr(requestData);
    }

    @PostMapping("/getEventCount")
    public String getEventCount()  {
        return "队列中事件数量为：" + eventTracker.getEventSize();
    }
}
