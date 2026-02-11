package com.xiaofei.springbootbackendkafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootbackendkafka.model.entity.WorkCalendar;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作日历 Mapper
 */
@Mapper
public interface WorkCalendarMapper extends BaseMapper<WorkCalendar> {
}