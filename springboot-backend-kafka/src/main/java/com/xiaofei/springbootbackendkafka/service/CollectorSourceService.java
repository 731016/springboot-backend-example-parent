package com.xiaofei.springbootbackendkafka.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.collector.CollectResult;
import com.xiaofei.springbootbackendkafka.collector.CollectorAdapter;
import com.xiaofei.springbootbackendkafka.collector.CollectorAdapterRegistry;
import com.xiaofei.springbootbackendkafka.collector.CollectorProtocol;
import com.xiaofei.springbootbackendkafka.collector.CollectorSourceSnapshot;
import com.xiaofei.springbootbackendkafka.mapper.CollectorHttpConfigMapper;
import com.xiaofei.springbootbackendkafka.mapper.CollectorModbusConfigMapper;
import com.xiaofei.springbootbackendkafka.mapper.CollectorOpcUaConfigMapper;
import com.xiaofei.springbootbackendkafka.mapper.CollectorSourceMapper;
import com.xiaofei.springbootbackendkafka.mapper.PointConfigMapper;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorHttpConfigDTO;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorModbusConfigDTO;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorOpcUaConfigDTO;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorSourceQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.SaveCollectorSourceRequest;
import com.xiaofei.springbootbackendkafka.model.dto.TestCollectorSourceRequest;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorHttpConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorSource;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import com.xiaofei.springbootbackendkafka.model.vo.CollectorSourceTestVO;
import com.xiaofei.springbootbackendkafka.model.vo.CollectorSourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 采集数据源服务
 *
 * @author tuaofei
 */
@Service
@Slf4j
public class CollectorSourceService {

    private static final String DEFAULT_HTTP_TEMPLATE =
            "{\"pointCode\":\"${pointCode}\",\"collectTime\":\"${collectTime}\"}";

    @Autowired
    private CollectorSourceMapper sourceMapper;

    @Autowired
    private CollectorHttpConfigMapper httpConfigMapper;

    @Autowired
    private CollectorModbusConfigMapper modbusConfigMapper;

    @Autowired
    private CollectorOpcUaConfigMapper opcUaConfigMapper;

    @Autowired
    private PointConfigMapper pointConfigMapper;

    @Autowired
    private CollectorPointBindingService bindingService;

    @Autowired
    private CollectorAdapterRegistry adapterRegistry;

    private final Map<Long, CollectorSourceSnapshot> snapshotCache = new ConcurrentHashMap<>();

    public Page<CollectorSourceVO> listByPage(CollectorSourceQueryRequest request) {
        QueryWrapper<CollectorSource> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(request.getSourceCode()), "sourceCode", request.getSourceCode());
        wrapper.like(StrUtil.isNotBlank(request.getSourceName()), "sourceName", request.getSourceName());
        wrapper.eq(StrUtil.isNotBlank(request.getProtocol()), "protocol", request.getProtocol());
        wrapper.eq(request.getStatus() != null, "status", request.getStatus());
        wrapper.orderByDesc("updateTime");
        Page<CollectorSource> page = sourceMapper.selectPage(
                new Page<>(request.getCurrent(), request.getPageSize()), wrapper);
        Page<CollectorSourceVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    public List<CollectorSourceVO> listAll() {
        List<CollectorSource> sources = sourceMapper.selectList(
                new QueryWrapper<CollectorSource>().eq("status", 1).orderByAsc("sourceName"));
        return sources.stream().map(this::toVO).collect(Collectors.toList());
    }

    public CollectorSourceVO detail(Long id) {
        CollectorSource source = sourceMapper.selectById(id);
        if (source == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集数据源不存在");
        }
        return toVO(source);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(SaveCollectorSourceRequest request) {
        CollectorProtocol protocol = CollectorProtocol.from(request.getProtocol());
        CollectorSource sameCode = sourceMapper.selectOne(
                new QueryWrapper<CollectorSource>().eq("sourceCode", request.getSourceCode().trim()));
        if (sameCode != null && !sameCode.getId().equals(request.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据源编码已存在");
        }

        CollectorSource source;
        if (request.getId() == null) {
            source = new CollectorSource();
            source.setCreateTime(new Date());
            source.setIsDeleted(0);
        } else {
            source = sourceMapper.selectById(request.getId());
            if (source == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集数据源不存在");
            }
        }
        source.setSourceCode(request.getSourceCode().trim());
        source.setSourceName(request.getSourceName().trim());
        source.setProtocol(protocol.name());
        source.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        source.setRemark(request.getRemark());
        source.setUpdateTime(new Date());
        if (request.getId() == null) {
            sourceMapper.insert(source);
        } else {
            sourceMapper.updateById(source);
        }

        saveProtocolConfig(source.getId(), protocol, request);
        snapshotCache.remove(source.getId());
        return source.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CollectorSource source = sourceMapper.selectById(id);
        if (source == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集数据源不存在");
        }
        Long pointCount = pointConfigMapper.selectCount(
                new QueryWrapper<PointConfig>().eq("sourceId", id));
        if (pointCount != null && pointCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "该数据源已被 " + pointCount + " 个点位引用，无法删除");
        }
        sourceMapper.deleteById(id);
        deleteProtocolConfig(id);
        snapshotCache.remove(id);
    }

    public CollectorSourceSnapshot getSnapshot(Long sourceId) {
        if (sourceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位未关联采集数据源");
        }
        CollectorSourceSnapshot cached = snapshotCache.get(sourceId);
        if (cached != null) {
            return cached;
        }
        CollectorSourceSnapshot snapshot = loadSnapshot(sourceId);
        snapshotCache.put(sourceId, snapshot);
        return snapshot;
    }

    public void evict(Long sourceId) {
        if (sourceId != null) {
            snapshotCache.remove(sourceId);
        }
    }

    public CollectorSourceTestVO test(TestCollectorSourceRequest request) {
        long start = System.currentTimeMillis();
        try {
            CollectorSourceSnapshot snapshot = getSnapshot(request.getSourceId());
            CollectorAdapter adapter = adapterRegistry.get(snapshot.protocol());
            if (request.getPointId() == null) {
                adapter.testConnection(snapshot);
                return CollectorSourceTestVO.builder()
                        .success(true)
                        .message("连接测试成功")
                        .costMillis(System.currentTimeMillis() - start)
                        .build();
            }
            PointConfig point = pointConfigMapper.selectById(request.getPointId());
            if (point == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位不存在");
            }
            if (!request.getSourceId().equals(point.getSourceId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位未关联该数据源");
            }
            CollectResult result = adapter.collect(snapshot,
                    bindingService.getBinding(point, snapshot.protocol()), new Date());
            return CollectorSourceTestVO.builder()
                    .success(true)
                    .message("采集成功")
                    .value(result.getValue())
                    .attributeName(result.getAttributeName())
                    .costMillis(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            log.warn("采集源测试失败: sourceId={}", request.getSourceId(), e);
            return CollectorSourceTestVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .costMillis(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private CollectorSourceSnapshot loadSnapshot(Long sourceId) {
        CollectorSource source = sourceMapper.selectById(sourceId);
        if (source == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "采集数据源不存在: " + sourceId);
        }
        CollectorProtocol protocol = CollectorProtocol.from(source.getProtocol());
        CollectorSourceSnapshot.CollectorSourceSnapshotBuilder builder =
                CollectorSourceSnapshot.builder().source(source);
        if (protocol == CollectorProtocol.HTTP) {
            builder.httpConfig(httpConfigMapper.selectOne(
                    new QueryWrapper<CollectorHttpConfig>().eq("sourceId", sourceId)));
        } else if (protocol == CollectorProtocol.MODBUS_TCP) {
            builder.modbusConfig(modbusConfigMapper.selectOne(
                    new QueryWrapper<CollectorModbusConfig>().eq("sourceId", sourceId)));
        } else if (protocol == CollectorProtocol.OPC_UA) {
            builder.opcUaConfig(opcUaConfigMapper.selectOne(
                    new QueryWrapper<CollectorOpcUaConfig>().eq("sourceId", sourceId)));
        }
        return builder.build();
    }

    private void saveProtocolConfig(Long sourceId, CollectorProtocol protocol, SaveCollectorSourceRequest request) {
        CollectorHttpConfig httpConfig = protocol == CollectorProtocol.HTTP
                ? buildHttpConfig(sourceId, request.getHttpConfig()) : null;
        CollectorModbusConfig modbusConfig = protocol == CollectorProtocol.MODBUS_TCP
                ? buildModbusConfig(sourceId, request.getModbusConfig()) : null;
        CollectorOpcUaConfig opcUaConfig = protocol == CollectorProtocol.OPC_UA
                ? buildOpcUaConfig(sourceId, request.getOpcUaConfig()) : null;

        deleteProtocolConfig(sourceId);
        if (httpConfig != null) {
            httpConfigMapper.insert(httpConfig);
        }
        if (modbusConfig != null) {
            modbusConfigMapper.insert(modbusConfig);
        }
        if (opcUaConfig != null) {
            opcUaConfigMapper.insert(opcUaConfig);
        }
    }

    private void deleteProtocolConfig(Long sourceId) {
        httpConfigMapper.delete(new QueryWrapper<CollectorHttpConfig>().eq("sourceId", sourceId));
        modbusConfigMapper.delete(new QueryWrapper<CollectorModbusConfig>().eq("sourceId", sourceId));
        opcUaConfigMapper.delete(new QueryWrapper<CollectorOpcUaConfig>().eq("sourceId", sourceId));
    }

    private CollectorHttpConfig buildHttpConfig(Long sourceId, CollectorHttpConfigDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getBaseUrl())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTTP采集地址不能为空");
        }
        CollectorHttpConfig config = new CollectorHttpConfig();
        config.setSourceId(sourceId);
        config.setBaseUrl(dto.getBaseUrl().trim());
        config.setValidUrl(StrUtil.isBlank(dto.getValidUrl()) ? null : dto.getValidUrl().trim());
        config.setMethod(StrUtil.blankToDefault(dto.getMethod(), "POST").trim().toUpperCase());
        config.setContentType(StrUtil.blankToDefault(dto.getContentType(), "application/json"));
        config.setHeaders(dto.getHeaders());
        config.setRequestTemplate(StrUtil.blankToDefault(dto.getRequestTemplate(), DEFAULT_HTTP_TEMPLATE));
        config.setResponseDataPath(StrUtil.blankToDefault(dto.getResponseDataPath(), "data"));
        config.setValueField(StrUtil.blankToDefault(dto.getValueField(), "value"));
        config.setAttributeNameField(StrUtil.blankToDefault(dto.getAttributeNameField(), "attributeName"));
        config.setTimeoutMillis(dto.getTimeoutMillis() == null ? 5000 : dto.getTimeoutMillis());
        return config;
    }

    private CollectorModbusConfig buildModbusConfig(Long sourceId, CollectorModbusConfigDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getHost())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Modbus从站地址不能为空");
        }
        int port = dto.getPort() == null ? 502 : dto.getPort();
        if (port < 1 || port > 65535) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Modbus端口必须在1~65535之间");
        }
        int slaveId = dto.getSlaveId() == null ? 1 : dto.getSlaveId();
        if (slaveId < 0 || slaveId > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Modbus从站ID必须在0~255之间");
        }
        CollectorModbusConfig config = new CollectorModbusConfig();
        config.setSourceId(sourceId);
        config.setHost(dto.getHost().trim());
        config.setPort(port);
        config.setSlaveId(slaveId);
        config.setConnectTimeoutMillis(
                dto.getConnectTimeoutMillis() == null ? 3000 : dto.getConnectTimeoutMillis());
        config.setReadTimeoutMillis(dto.getReadTimeoutMillis() == null ? 3000 : dto.getReadTimeoutMillis());
        return config;
    }

    private CollectorOpcUaConfig buildOpcUaConfig(Long sourceId, CollectorOpcUaConfigDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getEndpointUrl())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "OPC UA Endpoint地址不能为空");
        }
        CollectorOpcUaConfig config = new CollectorOpcUaConfig();
        config.setSourceId(sourceId);
        config.setEndpointUrl(dto.getEndpointUrl().trim());
        config.setSecurityPolicy(StrUtil.blankToDefault(dto.getSecurityPolicy(), "None"));
        config.setUsername(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setRequestTimeoutMillis(
                dto.getRequestTimeoutMillis() == null ? 5000 : dto.getRequestTimeoutMillis());
        return config;
    }

    private CollectorSourceVO toVO(CollectorSource source) {
        CollectorSourceVO vo = new CollectorSourceVO();
        BeanUtil.copyProperties(source, vo);
        vo.setPointCount(pointConfigMapper.selectCount(
                new QueryWrapper<PointConfig>().eq("sourceId", source.getId())));
        CollectorProtocol protocol = CollectorProtocol.from(source.getProtocol());
        if (protocol == CollectorProtocol.HTTP) {
            CollectorHttpConfig config = httpConfigMapper.selectOne(
                    new QueryWrapper<CollectorHttpConfig>().eq("sourceId", source.getId()));
            if (config != null) {
                vo.setHttpConfig(BeanUtil.copyProperties(config, CollectorHttpConfigDTO.class));
            }
        } else if (protocol == CollectorProtocol.MODBUS_TCP) {
            CollectorModbusConfig config = modbusConfigMapper.selectOne(
                    new QueryWrapper<CollectorModbusConfig>().eq("sourceId", source.getId()));
            if (config != null) {
                vo.setModbusConfig(BeanUtil.copyProperties(config, CollectorModbusConfigDTO.class));
            }
        } else if (protocol == CollectorProtocol.OPC_UA) {
            CollectorOpcUaConfig config = opcUaConfigMapper.selectOne(
                    new QueryWrapper<CollectorOpcUaConfig>().eq("sourceId", source.getId()));
            if (config != null) {
                vo.setOpcUaConfig(BeanUtil.copyProperties(config, CollectorOpcUaConfigDTO.class));
            }
        }
        return vo;
    }
}
