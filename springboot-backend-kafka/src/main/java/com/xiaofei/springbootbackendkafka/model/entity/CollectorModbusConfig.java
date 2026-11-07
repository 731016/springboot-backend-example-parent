package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Modbus TCP 采集源配置
 *
 * @author tuaofei
 */
@Data
@TableName("collector_modbus_config")
public class CollectorModbusConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceId;

    private String host;

    private Integer port;

    private Integer slaveId;

    private Integer connectTimeoutMillis;

    private Integer readTimeoutMillis;
}
