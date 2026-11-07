package com.xiaofei.springbootbackendkafka.collector;

import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;

/**
 * 采集协议
 *
 * @author tuaofei
 */
public enum CollectorProtocol {

    /**
     * HTTP 接口采集
     */
    HTTP,

    /**
     * Modbus TCP 采集
     */
    MODBUS_TCP,

    /**
     * OPC UA 采集
     */
    OPC_UA;

    public static CollectorProtocol from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集协议不能为空");
        }
        try {
            return CollectorProtocol.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的采集协议: " + value);
        }
    }
}
