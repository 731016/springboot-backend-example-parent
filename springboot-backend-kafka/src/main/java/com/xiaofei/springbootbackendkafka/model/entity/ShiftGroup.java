package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 班次班组基础表
 */
@Data
@TableName("shift_group")
public class ShiftGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 班次编码（如 A/B/C）
     */
    private String shiftCode;

    /**
     * 班次名称（如 早班/中班/晚班）
     */
    private String shiftName;

    /**
     * 班次开始时间（仅时间部分）
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date shiftStartTime;

    /**
     * 班次结束时间（仅时间部分）
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

