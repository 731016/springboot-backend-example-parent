package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 班次班组基础信息分页查询请求
 */
@Data
public class ShiftGroupQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 每页大小
     */
    private long pageSize = 10;

    /**
     * 班次编码
     */
    private String shiftCode;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend / descend）
     */
    private String sortOrder;
}

