package com.xiaofei.springbootinit.example.blockingQueue;

import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @author tuaofei
 * @description TODO
 * @date 2025/3/10
 */
public abstract class EventAbstractResolver<T extends RequestData> implements EventResolver<T>, InitializingBean {

    @Resource
    private EventTracker eventTracker;

    /**
     * 将当前的事件处理器添加到调度器中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        eventTracker.addResolver(this);
    }
}
