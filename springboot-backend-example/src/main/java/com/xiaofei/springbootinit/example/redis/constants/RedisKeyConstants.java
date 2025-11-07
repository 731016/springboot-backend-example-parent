package com.xiaofei.springbootinit.example.redis.constants;

import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
public interface RedisKeyConstants {

    // 设置在指定时间点过期
    Date EXPIRY_DATE = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时后

    String CACHE_KEY_PREFIX = "code_dictionary:";
}
