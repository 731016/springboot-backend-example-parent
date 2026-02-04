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

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心线程数
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(200); // 队列容量
        executor.setThreadNamePrefix("custom-executor-"); // 线程名前缀
        executor.setKeepAliveSeconds(60);  // 空闲线程存活时间（秒）
        executor.initialize(); // 初始化
        return executor;
    }
}

