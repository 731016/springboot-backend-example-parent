package com.xiaofei.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * log_data
 */
@TableName(value = "log_data")
@Data
public class LogData implements Serializable {


    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务主键ID
     */
    private Long valueid;

    /**
     * 数据json
     */
    private String data;

    /**
     * 创建时间
     */
    private Date createdate;

    /**
     * 创建人
     */
    private Long createuser;

    private static final long serialVersionUID = 1L;
}