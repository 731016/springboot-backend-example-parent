package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 工作日历（班次）实体
 */
@Data
@TableName("work_calendar")
public class WorkCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工作日期（只关心日期部分）
     */
    private Date workDate;

    /**
     * 班次编码（如 A/B/C）
     */
    private String shiftCode;

    /**
     * 班次名称（如 早班/中班/晚班）
     */
    private String shiftName;

    /**
     * 班次开始时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date shiftStartTime;

    /**
     * 班次结束时间
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date shiftEndTime;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private Date createTime;

    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableField(value = "isDeleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}