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
@TableName("data_detail")
public class DataDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pointCode;
    private Date collectTime;
    private BigDecimal value;
    private String attributeName;
    private Long statisticsId;
    private Date createTime;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
