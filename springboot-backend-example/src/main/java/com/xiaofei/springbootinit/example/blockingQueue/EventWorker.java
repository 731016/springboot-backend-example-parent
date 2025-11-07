package com.xiaofei.springbootinit.example.blockingQueue;

import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * @author tuaofei
 * @description 真正处理队列中数据的工作者
 * @date 2025/3/3
 */
public class EventWorker implements Runnable{

    private final static Log logger = LogFactory.getLog(EventWorker.class);

    /**
     * 事件解析器
     */
    private final List<EventResolver> eventResolvers;

    /**
     * 事件调度器
     */
    private final EventTracker eventTracker;

    public EventWorker(List<EventResolver> eventResolvers, EventTracker eventTracker) {
        this.eventResolvers = eventResolvers;
        this.eventTracker = eventTracker;
    }


    @Override
    public void run() {
        RequestData event;
        while ((event = eventTracker.popEvent()) != null) {
            try {
                EventContext context = new EventContext();
                // 处理消息
                for (EventResolver eventResolver : eventResolvers) {
                    if (eventResolver.support(event)) {
                        eventResolver.resolve(event, context);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int eventSize = eventTracker.getEventSize();
            logger.info("当前回调事件数量：" + eventSize);
        }
        // 释放一个工作者
        eventTracker.subWorkers();
    }
}
