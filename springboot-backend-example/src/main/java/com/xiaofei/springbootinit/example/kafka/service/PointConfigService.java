package com.xiaofei.springbootinit.example.kafka.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.example.kafka.mapper.PointConfigMapper;
import com.xiaofei.springbootinit.example.kafka.model.dto.AddPointConfigRequest;
import com.xiaofei.springbootinit.example.kafka.model.entity.PointConfig;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private PointConfigMapper pointConfigMapper;

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

        // 3. 创建点位配置
        PointConfig pointConfig = new PointConfig();
        pointConfig.setPointCode(request.getPointCode());
        pointConfig.setPointName(request.getPointName());
        pointConfig.setValidUrl(request.getValidUrl());
        pointConfig.setDataUrl(request.getDataUrl());
        pointConfig.setMinLimit(request.getMinLimit());
        pointConfig.setMaxLimit(request.getMaxLimit());
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

        // 5. 如果URL不为空，验证连接
        if (StrUtil.isNotBlank(request.getValidUrl())) {
            try {
                boolean isValid = testConnection(request.getValidUrl());
                if (!isValid) {
                    log.warn("点位验证URL连接失败: {}", request.getValidUrl());
                }
            } catch (Exception e) {
                log.error("验证URL连接异常: {}", request.getValidUrl(), e);
            }
        }

        return pointConfig.getId();
    }

    /**
     * 测试连接
     */
    private boolean testConnection(String url) {
        try {
            HttpResponse response = HttpUtil.createPost(url)
                    .setConnectionTimeout(3000)
                    .setReadTimeout(3000)
                    .execute();
            return response.isOk();
        } catch (Exception e) {
            log.error("连接测试失败: {}", url, e);
            return false;
        }
    }
}
