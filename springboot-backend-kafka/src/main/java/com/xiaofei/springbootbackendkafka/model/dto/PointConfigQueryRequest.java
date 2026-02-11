package com.xiaofei.springbootbackendkafka.model.dto;

import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 采集点配置查询请求
 * @author tuaofei
 * @date 2024/12/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PointConfigQueryRequest extends PageRequest implements Serializable {

    /**
     * 采集点编码（模糊查询）
     */
    private String pointCode;

    /**
     * 采集点名称（模糊查询）
     */
    private String pointName;

    /**
     * 采集间隔(秒)
     */
    private Integer intervalSeconds;

    /**
     * 是否主点 (0-否, 1-是)
     */
    private Integer isMainPoint;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
