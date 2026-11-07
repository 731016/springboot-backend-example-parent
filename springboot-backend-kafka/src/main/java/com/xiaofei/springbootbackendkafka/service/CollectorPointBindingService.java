package com.xiaofei.springbootbackendkafka.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.collector.CollectorProtocol;
import com.xiaofei.springbootbackendkafka.collector.ModbusDataType;
import com.xiaofei.springbootbackendkafka.collector.PointBinding;
import com.xiaofei.springbootbackendkafka.mapper.CollectorModbusPointMapper;
import com.xiaofei.springbootbackendkafka.mapper.CollectorOpcUaPointMapper;
import com.xiaofei.springbootbackendkafka.model.dto.PointBindingDTO;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusPoint;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaPoint;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 点位协议绑定服务
 *
 * @author tuaofei
 */
@Service
public class CollectorPointBindingService {

    @Autowired
    private CollectorModbusPointMapper modbusPointMapper;

    @Autowired
    private CollectorOpcUaPointMapper opcUaPointMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveBinding(Long pointId, Long sourceId, CollectorProtocol protocol, PointBindingDTO dto) {
        deleteByPointId(pointId);
        if (protocol == CollectorProtocol.HTTP) {
            return;
        }
        if (dto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请配置 " + protocol + " 协议的采集参数");
        }
        if (protocol == CollectorProtocol.MODBUS_TCP) {
            modbusPointMapper.insert(buildModbusPoint(pointId, sourceId, dto));
        } else if (protocol == CollectorProtocol.OPC_UA) {
            opcUaPointMapper.insert(buildOpcUaPoint(pointId, sourceId, dto));
        }
    }

    public PointBinding getBinding(PointConfig pointConfig, CollectorProtocol protocol) {
        PointBinding.PointBindingBuilder builder = PointBinding.builder().pointCode(pointConfig.getPointCode());
        if (protocol == CollectorProtocol.MODBUS_TCP) {
            CollectorModbusPoint modbusPoint = modbusPointMapper.selectOne(
                    new QueryWrapper<CollectorModbusPoint>().eq("pointId", pointConfig.getId()));
            if (modbusPoint == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "点位未配置Modbus寄存器: " + pointConfig.getPointCode());
            }
            builder.modbusPoint(modbusPoint);
        } else if (protocol == CollectorProtocol.OPC_UA) {
            CollectorOpcUaPoint opcUaPoint = opcUaPointMapper.selectOne(
                    new QueryWrapper<CollectorOpcUaPoint>().eq("pointId", pointConfig.getId()));
            if (opcUaPoint == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "点位未配置OPC UA NodeId: " + pointConfig.getPointCode());
            }
            builder.opcUaPoint(opcUaPoint);
        }
        return builder.build();
    }

    public PointBindingDTO getBindingDto(Long pointId) {
        PointBindingDTO dto = new PointBindingDTO();
        CollectorModbusPoint modbusPoint = modbusPointMapper.selectOne(
                new QueryWrapper<CollectorModbusPoint>().eq("pointId", pointId));
        if (modbusPoint != null) {
            dto.setRegisterType(modbusPoint.getRegisterType());
            dto.setAddress(modbusPoint.getAddress());
            dto.setQuantity(modbusPoint.getQuantity());
            dto.setDataType(modbusPoint.getDataType());
            dto.setByteOrder(modbusPoint.getByteOrder());
            dto.setScale(modbusPoint.getScale());
        }
        CollectorOpcUaPoint opcUaPoint = opcUaPointMapper.selectOne(
                new QueryWrapper<CollectorOpcUaPoint>().eq("pointId", pointId));
        if (opcUaPoint != null) {
            dto.setNodeId(opcUaPoint.getNodeId());
            dto.setAttributeName(opcUaPoint.getAttributeName());
        }
        return dto;
    }

    public void deleteByPointId(Long pointId) {
        modbusPointMapper.delete(new QueryWrapper<CollectorModbusPoint>().eq("pointId", pointId));
        opcUaPointMapper.delete(new QueryWrapper<CollectorOpcUaPoint>().eq("pointId", pointId));
    }

    private CollectorModbusPoint buildModbusPoint(Long pointId, Long sourceId, PointBindingDTO dto) {
        if (StrUtil.isBlank(dto.getRegisterType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择Modbus寄存器类型");
        }
        if (dto.getAddress() == null || dto.getAddress() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Modbus寄存器地址不能为空且不能小于0");
        }
        ModbusDataType dataType = ModbusDataType.from(dto.getDataType());
        int quantity = dto.getQuantity() == null || dto.getQuantity() < 1 ? 1 : dto.getQuantity();
        if (dataType.needTwoRegisters() && quantity < 2) {
            quantity = 2;
        }
        CollectorModbusPoint point = new CollectorModbusPoint();
        point.setPointId(pointId);
        point.setSourceId(sourceId);
        point.setRegisterType(dto.getRegisterType().trim().toUpperCase());
        point.setAddress(dto.getAddress());
        point.setQuantity(quantity);
        point.setDataType(dataType.name());
        point.setByteOrder(StrUtil.blankToDefault(dto.getByteOrder(), "ABCD").trim().toUpperCase());
        point.setScale(dto.getScale() == null ? BigDecimal.ONE : dto.getScale());
        return point;
    }

    private CollectorOpcUaPoint buildOpcUaPoint(Long pointId, Long sourceId, PointBindingDTO dto) {
        if (StrUtil.isBlank(dto.getNodeId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "OPC UA NodeId不能为空");
        }
        CollectorOpcUaPoint point = new CollectorOpcUaPoint();
        point.setPointId(pointId);
        point.setSourceId(sourceId);
        point.setNodeId(dto.getNodeId().trim());
        point.setAttributeName(dto.getAttributeName());
        return point;
    }
}
