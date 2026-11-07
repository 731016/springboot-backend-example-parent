package com.xiaofei.springbootbackendkafka.model.dto;

import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 采集源查询请求
 *
 * @author tuaofei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CollectorSourceQueryRequest extends PageRequest implements Serializable {

    private String sourceCode;

    private String sourceName;

    private String protocol;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
