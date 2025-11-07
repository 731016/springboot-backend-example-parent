package com.xiaofei.springbootinit.example.blockingQueue;

import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;
import com.xiaofei.springbootinit.model.entity.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tuaofei
 * @description 事件订阅调度器
 * @date 2025/3/3
 */
@Component
public class EventTracker {

    private final static Log logger = LogFactory.getLog(EventTracker.class.getName());

    private static ThreadPoolExecutor threadPoolExecutor = null;

    static {
        threadPoolExecutor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 任务队列
     */
    private final static LinkedBlockingQueue<RequestData> eventsQueue = new LinkedBlockingQueue<>();

    /**
     * 任务处理器
     */
    private List<EventResolver> eventResolvers = new ArrayList<>();


    public List<EventResolver> getEventResolvers() {
        return eventResolvers;
    }

    public void setEventResolvers(List<EventResolver> eventResolvers) {
        this.eventResolvers = eventResolvers;
    }

    /**
     * 添加处理器
     *
     * @param eventResolver
     */
    public void addResolver(EventResolver eventResolver) {
        this.eventResolvers.add(eventResolver);
    }

    /**
     * 排序
     */
    public void sort() {
        // 针对于所有的执行器进行排序,保证执行顺序
        if (eventResolvers != null) {
            Collections.sort(eventResolvers, Comparator.comparingInt(EventResolver::order));
        }
    }

    /**
     * 工作的线程数 默认1个
     */
    private final static AtomicInteger curWorkerNum = new AtomicInteger(0);

    /**
     * 最大工作线程数
     */
    private final int max_workers = 1;

    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 当队列中的任务大于此数 并且线程没有达到最大值的时候，会添加新的处理线程
     **/
    private static final int limit = 10;


    /**
     * 添加事件
     *
     * @param events
     */
    public void addEvent(RequestData events) {
        if (!enable) {
            logger.info("####################    events.enable:false,not allow addEvent ########################");
            return;
        }
        try {
            eventsQueue.put(events);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 设置处理线程
        addWorkers();
    }

    /**
     * 添加工作线程
     */
    private void addWorkers() {
        int eventSize = eventsQueue.size();
        int intValue = curWorkerNum.getAndIncrement();
        if (intValue == 0 || (intValue < max_workers && eventSize > limit)) {
            // 当接收到任务的时候 如果当前任务数小大于10 并且
            logger.info("\n##############################\tWsdHikvisionEventTracker info:addWorkers\t############################################\n" +
                    "\t\t\t当前任务数:" + eventSize + ",\t工作线程数:" + intValue + ",\t最大工作线程:" + max_workers + "\n" +
                    "##############################\tWsdHikvisionEventTracker info\t############################################\n");
            threadPoolExecutor.execute(new EventWorker(eventResolvers, this));
        } else {
            curWorkerNum.getAndDecrement();
        }
    }

    /**
     * 削减一个工作线程
     */
    public void subWorkers() {
        int intValue = curWorkerNum.decrementAndGet();
        int eventSize = eventsQueue.size();
        logger.info("\n##############################\tWsdHikvisionEventTracker info:subWorkers\t############################################\n" +
                "\t\t\t当前任务数:" + eventSize + ",\t工作线程数:" + intValue + ",\t最大工作线程:" + max_workers + "\n" +
                "##############################\tWsdHikvisionEventTracker info\t############################################\n");
    }


    /**
     * 判断是否还有任务
     *
     * @return
     */
    public boolean hasEvents() {
        return eventsQueue.size() > 0;
    }

    /**
     * 获取事件数量
     *
     * @return
     */
    public int getEventSize() {
        return eventsQueue.size();
    }

    /**
     * 弹出事件 每个线程最多等待1分钟
     *
     * @return
     */
    public RequestData popEvent() {
        try {
            return eventsQueue.poll(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
