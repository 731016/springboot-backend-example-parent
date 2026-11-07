package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 采集源测试请求
 *
 * @author tuaofei
 */
@Data
public class TestCollectorSourceRequest implements Serializable {

    private Long sourceId;

    /**
     * 可选：指定点位时执行一次真实采集，否则仅做连接测试
     */
    private Long pointId;

    private static final long serialVersionUID = 1L;
}
