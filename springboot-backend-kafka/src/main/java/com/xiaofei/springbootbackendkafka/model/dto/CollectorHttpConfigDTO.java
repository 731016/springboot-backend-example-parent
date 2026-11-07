package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

/**
 * HTTP 采集源配置
 *
 * @author tuaofei
 */
@Data
public class CollectorHttpConfigDTO {

    private String baseUrl;

    private String validUrl;

    private String method;

    private String contentType;

    private String headers;

    private String requestTemplate;

    private String responseDataPath;

    private String valueField;

    private String attributeNameField;

    private Integer timeoutMillis;
}
