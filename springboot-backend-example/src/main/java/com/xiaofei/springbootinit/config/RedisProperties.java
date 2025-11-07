package com.xiaofei.springbootinit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@ConfigurationProperties(prefix = "spring.redis")
@Component
@Data
public class RedisProperties {
    private String host;
    private int port;
    private Duration timeout;
}
