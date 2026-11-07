package com.xiaofei.springbootbackendkafka.collector;

import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Modbus 寄存器数据类型与字节序解码
 *
 * @author tuaofei
 */
public enum ModbusDataType {

    BOOLEAN,
    INT16,
    UINT16,
    INT32,
    UINT32,
    FLOAT32;

    public static ModbusDataType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return INT16;
        }
        try {
            return ModbusDataType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的Modbus数据类型: " + value);
        }
    }

    /**
     * 是否需要 2 个寄存器
     */
    public boolean needTwoRegisters() {
        return this == INT32 || this == UINT32 || this == FLOAT32;
    }

    public BigDecimal decode(int[] registers, String byteOrder) {
        if (registers == null || registers.length == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Modbus寄存器数据为空");
        }
        if (needTwoRegisters() && registers.length < 2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, getDataTypeMessage());
        }
        switch (this) {
            case BOOLEAN:
                return registers[0] != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            case INT16:
                return BigDecimal.valueOf((short) registers[0]);
            case UINT16:
                return BigDecimal.valueOf(registers[0] & 0xFFFF);
            case INT32:
                return BigDecimal.valueOf(ByteBuffer.wrap(toBytes(registers, byteOrder)).getInt());
            case UINT32:
                return BigDecimal.valueOf(ByteBuffer.wrap(toBytes(registers, byteOrder)).getInt() & 0xFFFFFFFFL);
            case FLOAT32:
                float floatValue = ByteBuffer.wrap(toBytes(registers, byteOrder)).getFloat();
                return new BigDecimal(Float.toString(floatValue));
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的Modbus数据类型: " + this);
        }
    }

    private String getDataTypeMessage() {
        return this + " 需要读取 2 个寄存器，请将读取数量设置为 2";
    }

    private byte[] toBytes(int[] registers, String byteOrder) {
        int first = registers[0] & 0xFFFF;
        int second = registers[1] & 0xFFFF;
        int highFirst = (first >> 8) & 0xFF;
        int lowFirst = first & 0xFF;
        int highSecond = (second >> 8) & 0xFF;
        int lowSecond = second & 0xFF;
        String order = byteOrder == null ? "ABCD" : byteOrder.trim().toUpperCase();
        byte[] bytes;
        switch (order) {
            case "CDAB":
                bytes = new byte[]{(byte) highSecond, (byte) lowSecond, (byte) highFirst, (byte) lowFirst};
                break;
            case "BADC":
                bytes = new byte[]{(byte) lowFirst, (byte) highFirst, (byte) lowSecond, (byte) highSecond};
                break;
            case "DCBA":
                bytes = new byte[]{(byte) lowSecond, (byte) highSecond, (byte) lowFirst, (byte) highFirst};
                break;
            case "ABCD":
            default:
                bytes = new byte[]{(byte) highFirst, (byte) lowFirst, (byte) highSecond, (byte) lowSecond};
                break;
        }
        return bytes;
    }
}
