package com.xiaofei.springbootbackendkafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootbackendkafka.model.entity.ShiftGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班次班组基础表 Mapper
 */
@Mapper
public interface ShiftGroupMapper extends BaseMapper<ShiftGroup> {
}

