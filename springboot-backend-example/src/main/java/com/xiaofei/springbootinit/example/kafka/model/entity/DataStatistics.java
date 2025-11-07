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
@TableName("data_statistics")
public class DataStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pointCode;

    private Date startTime;

    private Date endTime;

    private BigDecimal maximumValue;  // 改为 maximumValue
    private BigDecimal minimumValue;  // 改为 minimumValue
    private BigDecimal averageValue;  // 改为 averageValue

    private Integer status;

    private Date createTime;

    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
