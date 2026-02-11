package com.xiaofei.springbootbackendkafka.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.constant.CommonConstant;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.mapper.ShiftGroupMapper;
import com.xiaofei.springbootbackendkafka.model.dto.ShiftGroupQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.ShiftGroupSaveRequest;
import com.xiaofei.springbootbackendkafka.model.entity.ShiftGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ShiftGroupService {

    @Autowired
    private ShiftGroupMapper shiftGroupMapper;

    /**
     * 新增或修改班次班组基础信息
     */
    public Long saveShiftGroup(ShiftGroupSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        //开始时间不能超过完成时间
        Date shiftStartTime = request.getShiftStartTime();
        Date shiftEndTime = request.getShiftEndTime();
        if (DateUtil.compare(shiftStartTime, shiftEndTime, "HH:mm:ss") > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, String.format("开始时间【%s】不能超过结束时间【%s】！", DateUtil.format(shiftStartTime, "HH:mm:ss"), DateUtil.format(shiftEndTime, "HH:mm:ss")));
        }

        //判断班次编码不能重复
        QueryWrapper<ShiftGroup> existWrapper = new QueryWrapper<>();
        existWrapper.eq("shiftCode", request.getShiftCode());
        if (request.getId() != null) {
            existWrapper.ne("id", request.getId());
        }
        Long exist = shiftGroupMapper.selectCount(existWrapper);
        if (exist > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, String.format("班次编码【%s】已存在，请重新输入！", request.getShiftCode()));
        }


        ShiftGroup entity = new ShiftGroup();
        BeanUtils.copyProperties(request, entity);

        Date now = new Date();
        if (entity.getId() == null) {
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            int result = shiftGroupMapper.insert(entity);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增班次班组失败");
            }
        } else {
            entity.setUpdateTime(now);
            int result = shiftGroupMapper.updateById(entity);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新班次班组失败");
            }
        }
        return entity.getId();
    }

    /**
     * 删除（逻辑删除）
     */
    public boolean deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID 非法");
        }
        int result = shiftGroupMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 分页查询
     */
    public Page<ShiftGroup> listByPage(ShiftGroupQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();

        QueryWrapper<ShiftGroup> queryWrapper = getQueryWrapper(queryRequest);
        return shiftGroupMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    /**
     * 查询条件封装
     */
    public QueryWrapper<ShiftGroup> getQueryWrapper(ShiftGroupQueryRequest queryRequest) {
        QueryWrapper<ShiftGroup> queryWrapper = new QueryWrapper<>();

        String shiftCode = queryRequest.getShiftCode();
        String shiftName = queryRequest.getShiftName();
        Integer status = queryRequest.getStatus();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        queryWrapper.like(StringUtils.isNotBlank(shiftCode), "shiftCode", shiftCode);
        queryWrapper.like(StringUtils.isNotBlank(shiftName), "shiftName", shiftName);
        queryWrapper.eq(status != null, "status", status);

        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            queryWrapper.orderByAsc("shiftCode", "shiftName");
        }
        return queryWrapper;
    }

    /**
     * 根据 ID 查询
     */
    public ShiftGroup getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID 非法");
        }
        return shiftGroupMapper.selectById(id);
    }
}

