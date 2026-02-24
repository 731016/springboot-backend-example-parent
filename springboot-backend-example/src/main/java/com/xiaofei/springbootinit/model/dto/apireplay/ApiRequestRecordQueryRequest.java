package com.xiaofei.springbootinit.model.dto.apireplay;

import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 接口请求记录分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiRequestRecordQueryRequest extends PageRequest implements Serializable {

    /**
     * 请求 URL（模糊）
     */
    private String url;

    /**
     * HTTP 方法
     */
    private String httpMethod;

    private static final long serialVersionUID = 1L;
}
