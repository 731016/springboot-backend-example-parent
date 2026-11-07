package com.xiaofei.springbootbackendkafka.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.constant.CommonConstant;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.collector.CollectorSourceSnapshot;
import com.xiaofei.springbootbackendkafka.mapper.PointConfigMapper;
import com.xiaofei.springbootbackendkafka.model.dto.AddPointConfigRequest;
import com.xiaofei.springbootbackendkafka.model.dto.PointConfigQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.UpdatePointConfigRequest;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Service
@Slf4j
public class PointConfigService {

    /**
     * 默认启用全部 SPC 判异规则
     */
    private static final String DEFAULT_SPC_RULE_CONFIG = "[\"R1\",\"R2\",\"R3\",\"R4\",\"R5\",\"R6\",\"R7\",\"R8\"]";

    @Autowired
    private PointConfigMapper pointConfigMapper;

    @Autowired
    private CollectorSourceService collectorSourceService;

    @Autowired
    private CollectorPointBindingService collectorPointBindingService;

    @Autowired
    private DataCollectionService dataCollectionService;

    @Transactional(rollbackFor = Exception.class)
    public long addPointConfig(AddPointConfigRequest request) {
        // 1. 校验点位编码是否已存在
        PointConfig existPoint = pointConfigMapper.getByPointCode(request.getPointCode());
        if (existPoint != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位编码已存在");
        }

        // 2. 如果是主点位，检查是否已有其他主点位
        if (Integer.valueOf(1).equals(request.getIsMainPoint())) {
            PointConfig mainPoint = pointConfigMapper.getMainPoint();
            if (mainPoint != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "已存在主点位");
            }
        }

        // 3. 校验采集数据源
        CollectorSourceSnapshot source = validateSource(request.getSourceId());

        // 4. 创建点位配置
        PointConfig pointConfig = new PointConfig();
        pointConfig.setPointCode(request.getPointCode());
        pointConfig.setPointName(request.getPointName());
        pointConfig.setUnit(request.getUnit());
        pointConfig.setSourceId(request.getSourceId());
        pointConfig.setMinLimit(request.getMinLimit());
        pointConfig.setMaxLimit(request.getMaxLimit());
        pointConfig.setSpcGroupSize(request.getSpcGroupSize() == null ? 5 : request.getSpcGroupSize());
        pointConfig.setSpcRuleConfig(StrUtil.isBlank(request.getSpcRuleConfig()) ? DEFAULT_SPC_RULE_CONFIG : request.getSpcRuleConfig());
        pointConfig.setIntervalSeconds(request.getIntervalSeconds());
        pointConfig.setIsMainPoint(request.getIsMainPoint());
        pointConfig.setStatus(request.getStatus());
        pointConfig.setRunningStatus(0);  // 初始为停止状态
        pointConfig.setCreateTime(new Date());
        pointConfig.setUpdateTime(new Date());

        // 4. 保存配置
        int result = pointConfigMapper.insert(pointConfig);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增点位配置失败");
        }

        // 5. 保存协议绑定
        collectorPointBindingService.saveBinding(pointConfig.getId(), request.getSourceId(),
                source.protocol(), request.getBinding());

        return pointConfig.getId();
    }

    /**
     * 更新采集点配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePointConfig(UpdatePointConfigRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空或 ID 为空");
        }

        PointConfig existPoint = pointConfigMapper.selectById(request.getId());
        if (existPoint == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位配置不存在");
        }

        // 校验点位编码是否冲突（允许保持原编码）
        PointConfig sameCodePoint = pointConfigMapper.getByPointCode(request.getPointCode());
        if (sameCodePoint != null && !sameCodePoint.getId().equals(request.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位编码已存在");
        }

        // 如果是主点位，检查是否已有其他主点位
        if (Integer.valueOf(1).equals(request.getIsMainPoint())) {
            PointConfig mainPoint = pointConfigMapper.getMainPoint();
            if (mainPoint != null && !mainPoint.getId().equals(request.getId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "已存在主点位");
            }
        }

        // 校验采集数据源
        CollectorSourceSnapshot source = validateSource(request.getSourceId());

        String originalPointCode = existPoint.getPointCode();

        // 更新字段
        existPoint.setPointCode(request.getPointCode());
        existPoint.setPointName(request.getPointName());
        existPoint.setUnit(request.getUnit());
        existPoint.setSourceId(request.getSourceId());
        existPoint.setMinLimit(request.getMinLimit());
        existPoint.setMaxLimit(request.getMaxLimit());
        existPoint.setSpcGroupSize(request.getSpcGroupSize() == null ? 5 : request.getSpcGroupSize());
        existPoint.setSpcRuleConfig(StrUtil.isBlank(request.getSpcRuleConfig()) ? DEFAULT_SPC_RULE_CONFIG : request.getSpcRuleConfig());
        existPoint.setIntervalSeconds(request.getIntervalSeconds());
        existPoint.setIsMainPoint(request.getIsMainPoint());
        existPoint.setStatus(request.getStatus());
        existPoint.setUpdateTime(new Date());

        int result = pointConfigMapper.updateById(existPoint);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新点位配置失败");
        }

        // 保存协议绑定
        collectorPointBindingService.saveBinding(existPoint.getId(), request.getSourceId(),
                source.protocol(), request.getBinding());

        // 点位编码变更时停止旧任务，再按新配置重启
        if (!originalPointCode.equals(request.getPointCode())) {
            dataCollectionService.stopCollection(originalPointCode);
        }
        dataCollectionService.reloadPoint(request.getPointCode());
    }

    /**
     * 校验采集数据源存在且已启用
     */
    private CollectorSourceSnapshot validateSource(Long sourceId) {
        if (sourceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集数据源不能为空");
        }
        CollectorSourceSnapshot source = collectorSourceService.getSnapshot(sourceId);
        if (!Integer.valueOf(1).equals(source.getSource().getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采集数据源已禁用");
        }
        return source;
    }

    /**
     * 分页查询采集点配置
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    public Page<PointConfig> listPointConfigByPage(PointConfigQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        QueryWrapper<PointConfig> queryWrapper = getQueryWrapper(queryRequest);
        return pointConfigMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询请求
     * @return 查询条件包装器
     */
    public QueryWrapper<PointConfig> getQueryWrapper(PointConfigQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String pointCode = queryRequest.getPointCode();
        String pointName = queryRequest.getPointName();
        Integer intervalSeconds = queryRequest.getIntervalSeconds();
        Integer isMainPoint = queryRequest.getIsMainPoint();
        Integer status = queryRequest.getStatus();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        QueryWrapper<PointConfig> queryWrapper = new QueryWrapper<>();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(pointCode), "pointCode", pointCode);
        queryWrapper.like(StringUtils.isNotBlank(pointName), "pointName", pointName);
        // 精确查询
        queryWrapper.eq(intervalSeconds != null, "intervalSeconds", intervalSeconds);
        queryWrapper.eq(isMainPoint != null, "isMainPoint", isMainPoint);
        queryWrapper.eq(status != null, "status", status);
        // 排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc("createTime");
        }
        return queryWrapper;
    }
}
