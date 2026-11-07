package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * OPC UA 采集源配置
 *
 * @author tuaofei
 */
@Data
@TableName("collector_opcua_config")
public class CollectorOpcUaConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceId;

    private String endpointUrl;

    private String securityPolicy;

    private String username;

    private String password;

    private Integer requestTimeoutMillis;
}
