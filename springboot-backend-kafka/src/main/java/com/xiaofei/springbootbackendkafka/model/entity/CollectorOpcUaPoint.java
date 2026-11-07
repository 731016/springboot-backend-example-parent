package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * OPC UA 点位绑定
 *
 * @author tuaofei
 */
@Data
@TableName("collector_opcua_point")
public class CollectorOpcUaPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pointId;

    private Long sourceId;

    private String nodeId;

    private String attributeName;
}
