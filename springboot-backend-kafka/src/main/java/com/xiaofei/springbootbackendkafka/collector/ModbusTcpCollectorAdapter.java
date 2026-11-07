package com.xiaofei.springbootbackendkafka.collector;

import cn.hutool.core.util.StrUtil;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Modbus TCP 采集适配器
 *
 * @author tuaofei
 */
@Slf4j
@Component
public class ModbusTcpCollectorAdapter implements CollectorAdapter {

    private static final String REGISTER_COIL = "COIL";

    private static final String REGISTER_DISCRETE_INPUT = "DISCRETE_INPUT";

    private static final String REGISTER_HOLDING = "HOLDING_REGISTER";

    private static final String REGISTER_INPUT = "INPUT_REGISTER";

    @Override
    public CollectorProtocol protocol() {
        return CollectorProtocol.MODBUS_TCP;
    }

    @Override
    public void testConnection(CollectorSourceSnapshot source) {
        CollectorModbusConfig config = requireConfig(source);
        try (ModbusTcpClient ignored = open(config)) {
            log.info("Modbus连接测试成功: {}:{}", config.getHost(), config.getPort());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Modbus连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public CollectResult collect(CollectorSourceSnapshot source, PointBinding binding, Date collectTime) {
        CollectorModbusConfig config = requireConfig(source);
        if (binding == null || binding.getModbusPoint() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Modbus点位缺少寄存器绑定配置");
        }
        CollectorModbusPoint point = binding.getModbusPoint();
        int address = point.getAddress() == null ? 0 : point.getAddress();
        int quantity = point.getQuantity() == null || point.getQuantity() < 1 ? 1 : point.getQuantity();
        ModbusDataType dataType = ModbusDataType.from(point.getDataType());
        if (dataType.needTwoRegisters() && quantity < 2) {
            quantity = 2;
        }
        BigDecimal rawValue;
        try (ModbusTcpClient client = open(config)) {
            String registerType = StrUtil.blankToDefault(point.getRegisterType(), REGISTER_HOLDING).toUpperCase();
            switch (registerType) {
                case REGISTER_COIL:
                    rawValue = boolToValue(client.readCoils(config.getSlaveId(), address, quantity)[0]);
                    break;
                case REGISTER_DISCRETE_INPUT:
                    rawValue = boolToValue(client.readDiscreteInputs(config.getSlaveId(), address, quantity)[0]);
                    break;
                case REGISTER_INPUT:
                    rawValue = dataType.decode(
                            client.readInputRegisters(config.getSlaveId(), address, quantity),
                            point.getByteOrder());
                    break;
                case REGISTER_HOLDING:
                default:
                    rawValue = dataType.decode(
                            client.readHoldingRegisters(config.getSlaveId(), address, quantity),
                            point.getByteOrder());
                    break;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Modbus采集失败: " + e.getMessage());
        }
        BigDecimal scale = point.getScale() == null ? BigDecimal.ONE : point.getScale();
        return new CollectResult(rawValue.multiply(scale), binding.getPointCode());
    }

    private BigDecimal boolToValue(boolean value) {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    private ModbusTcpClient open(CollectorModbusConfig config) throws Exception {
        int connectTimeout = config.getConnectTimeoutMillis() == null ? 3000 : config.getConnectTimeoutMillis();
        int readTimeout = config.getReadTimeoutMillis() == null ? 3000 : config.getReadTimeoutMillis();
        int port = config.getPort() == null ? 502 : config.getPort();
        return ModbusTcpClient.open(config.getHost(), port, connectTimeout, readTimeout);
    }

    private CollectorModbusConfig requireConfig(CollectorSourceSnapshot source) {
        if (source == null || source.getModbusConfig() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Modbus采集源配置不存在");
        }
        return source.getModbusConfig();
    }
}
