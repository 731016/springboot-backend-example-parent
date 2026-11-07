package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 点位协议绑定（按协议取用对应字段）
 *
 * @author tuaofei
 */
@Data
public class PointBindingDTO implements Serializable {

    /**
     * Modbus：COIL/DISCRETE_INPUT/HOLDING_REGISTER/INPUT_REGISTER
     */
    private String registerType;

    /**
     * Modbus：起始寄存器地址(0基)
     */
    private Integer address;

    /**
     * Modbus：读取数量
     */
    private Integer quantity;

    /**
     * Modbus：BOOLEAN/INT16/UINT16/INT32/UINT32/FLOAT32
     */
    private String dataType;

    /**
     * Modbus：ABCD/CDAB/BADC/DCBA
     */
    private String byteOrder;

    /**
     * Modbus：换算系数
     */
    private BigDecimal scale;

    /**
     * OPC UA：NodeId
     */
    private String nodeId;

    /**
     * OPC UA：属性名
     */
    private String attributeName;

    private static final long serialVersionUID = 1L;
}
