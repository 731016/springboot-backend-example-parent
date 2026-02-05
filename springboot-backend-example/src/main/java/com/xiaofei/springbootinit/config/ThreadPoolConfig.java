package com.xiaofei.springbootinit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author tuaofei
 * @description 线程池配置
 * @date 2026/02/04
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(value = "defaultExecutor")
    public ThreadPoolTaskExecutor defaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(100);
        // 队列容量
        executor.setQueueCapacity(1000000);
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        executor.setDaemon(true);
        //设置关机时是否等待计划任务完成，不中断正在运行的任务并执行队列中的所有任务。
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize(); // 初始化
        return executor;
    }
}

