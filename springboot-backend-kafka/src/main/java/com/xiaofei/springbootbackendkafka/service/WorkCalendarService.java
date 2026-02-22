package com.xiaofei.springbootbackendkafka.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.constant.CommonConstant;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.mapper.WorkCalendarMapper;
import com.xiaofei.springbootbackendkafka.model.dto.WorkCalendarQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.WorkCalendarSaveRequest;
import com.xiaofei.springbootbackendkafka.model.entity.WorkCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
public class WorkCalendarService {

    @Autowired
    private WorkCalendarMapper workCalendarMapper;

    /**
     * 新增或修改工作日历
     */
    public Long saveWorkCalendar(WorkCalendarSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        WorkCalendar entity = new WorkCalendar();
        BeanUtils.copyProperties(request, entity);

        Date now = new Date();
        if (entity.getId() == null) {
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            int result = workCalendarMapper.insert(entity);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增工作日历失败");
            }
        } else {
            entity.setUpdateTime(now);
            int result = workCalendarMapper.updateById(entity);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新工作日历失败");
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
        int result = workCalendarMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 分页查询
     */
    public Page<WorkCalendar> listByPage(WorkCalendarQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();

        QueryWrapper<WorkCalendar> queryWrapper = getQueryWrapper(queryRequest);
        return workCalendarMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    /**
     * 查询条件封装
     */
    public QueryWrapper<WorkCalendar> getQueryWrapper(WorkCalendarQueryRequest queryRequest) {
        QueryWrapper<WorkCalendar> queryWrapper = new QueryWrapper<>();

        Date workDate = queryRequest.getWorkDate();
        Date workDateStart = queryRequest.getWorkDateStart();
        Date workDateEnd = queryRequest.getWorkDateEnd();
        String shiftCode = queryRequest.getShiftCode();
        String shiftName = queryRequest.getShiftName();
        Integer status = queryRequest.getStatus();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        // 工作日期只按「年月日」过滤，不比较时分秒
        if (workDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(workDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date nextDayStart = cal.getTime();
            queryWrapper.ge("workDate", dayStart);
            queryWrapper.lt("workDate", nextDayStart);
        } else if (workDateStart != null || workDateEnd != null) {
            if (workDateStart != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(workDateStart);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                queryWrapper.ge("workDate", cal.getTime());
            }
            if (workDateEnd != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(workDateEnd);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                queryWrapper.le("workDate", cal.getTime());
            }
        }
        queryWrapper.like(StringUtils.isNotBlank(shiftCode), "shiftCode", shiftCode);
        queryWrapper.like(StringUtils.isNotBlank(shiftName), "shiftName", shiftName);
        queryWrapper.eq(status != null, "status", status);

        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            queryWrapper.orderByDesc("workDate", "shiftStartTime");
        }
        return queryWrapper;
    }

    /**
     * 根据 ID 查询
     */
    public WorkCalendar getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID 非法");
        }
        return workCalendarMapper.selectById(id);
    }
}