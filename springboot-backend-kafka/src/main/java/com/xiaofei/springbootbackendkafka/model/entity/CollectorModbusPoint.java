package com.xiaofei.springbootbackendkafka.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Modbus 点位绑定
 *
 * @author tuaofei
 */
@Data
@TableName("collector_modbus_point")
public class CollectorModbusPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pointId;

    private Long sourceId;

    /**
     * COIL/DISCRETE_INPUT/HOLDING_REGISTER/INPUT_REGISTER
     */
    private String registerType;

    private Integer address;

    private Integer quantity;

    /**
     * BOOLEAN/INT16/UINT16/INT32/UINT32/FLOAT32
     */
    private String dataType;

    /**
     * ABCD/CDAB/BADC/DCBA
     */
    private String byteOrder;

    private BigDecimal scale;
}
