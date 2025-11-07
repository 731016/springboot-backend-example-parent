package com.xiaofei.springbootinit.example.kafka.task;

import com.xiaofei.springbootinit.example.kafka.model.entity.PointConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description TODO
 * @date 2024/12/26
 * @author tuaofei
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class CollectAbstractTask {

    /**
     * 当前采集配置
     */
    protected PointConfig config;

    protected int retryCount = 0;

    public void incrementRetryCount() {
        retryCount++;
    }

    public void resetRetryCount() {
        retryCount = 0;
    }

    public long getWaitTimeSeconds() {
        // 实现指数退避策略，基础间隔 * (2^重试次数)
        return config.getIntervalSeconds() * (long)Math.pow(2, retryCount);
    }

}
