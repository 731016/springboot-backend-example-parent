package com.xiaofei.springbootinit.example.kafka.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Data
@TableName("point_config")
public class PointConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pointCode;
    private String pointName;
    private String validUrl;
    private String dataUrl;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private Integer intervalSeconds;
    private Integer isMainPoint;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    private Integer runningStatus;  // 添加运行状态字段

    /**
     * 是否删除
     */
    @TableField(value = "isDeleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
