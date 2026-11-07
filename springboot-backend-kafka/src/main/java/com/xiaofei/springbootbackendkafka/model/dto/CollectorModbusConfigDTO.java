package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

/**
 * Modbus TCP 采集源配置
 *
 * @author tuaofei
 */
@Data
public class CollectorModbusConfigDTO {

    private String host;

    private Integer port;

    private Integer slaveId;

    private Integer connectTimeoutMillis;

    private Integer readTimeoutMillis;
}
