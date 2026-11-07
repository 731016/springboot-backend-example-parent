package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

/**
 * OPC UA 采集源配置
 *
 * @author tuaofei
 */
@Data
public class CollectorOpcUaConfigDTO {

    private String endpointUrl;

    private String securityPolicy;

    private String username;

    private String password;

    private Integer requestTimeoutMillis;
}
