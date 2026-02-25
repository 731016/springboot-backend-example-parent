package com.xiaofei.springbootinit.model.dto.blockingQueue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件队列监控状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventQueueStatusVO {
    /** 队列中待处理事件数量 */
    private int queueSize;
    /** 当前工作线程数 */
    private int curWorkerNum;
    /** 最大工作线程数 */
    private int maxWorkers;
    /** 是否启用（允许投递） */
    private boolean enable;
}
