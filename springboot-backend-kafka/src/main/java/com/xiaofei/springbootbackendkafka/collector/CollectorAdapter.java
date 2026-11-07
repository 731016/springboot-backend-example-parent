package com.xiaofei.springbootbackendkafka.collector;

import java.util.Date;

/**
 * 采集适配器
 *
 * @author tuaofei
 */
public interface CollectorAdapter {

    /**
     * 支持的协议
     */
    CollectorProtocol protocol();

    /**
     * 连接测试，失败抛出 BusinessException
     */
    void testConnection(CollectorSourceSnapshot source);

    /**
     * 执行一次采集
     */
    CollectResult collect(CollectorSourceSnapshot source, PointBinding binding, Date collectTime) throws Exception;
}
