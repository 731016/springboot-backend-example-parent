package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * HTTP 采集源配置
 *
 * @author tuaofei
 */
@Data
@TableName("collector_http_config")
public class CollectorHttpConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceId;

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
