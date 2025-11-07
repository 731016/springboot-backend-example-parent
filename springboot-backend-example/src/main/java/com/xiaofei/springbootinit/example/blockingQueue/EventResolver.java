package com.xiaofei.springbootinit.example.blockingQueue;

import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;

/**
 * @author tuaofei
 * @description 事件处理器
 * @date 2025/3/3
 */
public interface EventResolver<T extends RequestData> {

    /**
     * 是否支持解析
     *
     * @param requestData
     * @return
     */
    boolean support(RequestData requestData);

    /**
     * 解析数据
     *
     * @param requestData
     * @param eventContext
     */
    void resolve(RequestData requestData,EventContext eventContext);


    /**
     * 排序码
     *
     * @return
     */
    default int order() {
        return 10;
    }
}
