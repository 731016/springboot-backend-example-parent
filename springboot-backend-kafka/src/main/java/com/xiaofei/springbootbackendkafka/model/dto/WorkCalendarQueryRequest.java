package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工作日历分页查询请求
 */
@Data
public class WorkCalendarQueryRequest implements Serializable {

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
     * 工作日期（查询某一天，可选）
     */
    private Date workDate;

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