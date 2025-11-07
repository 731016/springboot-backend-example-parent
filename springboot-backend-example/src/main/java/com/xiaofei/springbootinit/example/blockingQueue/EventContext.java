package com.xiaofei.springbootinit.example.blockingQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuaofei
 * @description 事件处理上下文对象
 * @date 2025/3/3
 */
public class EventContext {
    /**
     * 存放数据的容器
     */
    private Map<String, Object> params = new HashMap<>(16);

    /**
     * 获取参数
     *
     * @param key
     * @return
     */
    public Object getParam(String key) {
        return params.get(key);
    }

    /**
     * 设置参数
     *
     * @param key
     * @param val
     * @return
     */
    public Object putParam(String key, Object val) {
        return params.put(key, val);
    }
}
