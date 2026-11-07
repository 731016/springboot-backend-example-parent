package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 采集数据源
 *
 * @author tuaofei
 */
@Data
@TableName("collector_source")
public class CollectorSource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceCode;

    private String sourceName;

    /**
     * 协议：HTTP/MODBUS_TCP/OPC_UA
     */
    private String protocol;

    private Integer status;

    private String remark;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
