package com.xiaofei.springbootbackendkafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusPoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tuaofei
 */
@Mapper
public interface CollectorModbusPointMapper extends BaseMapper<CollectorModbusPoint> {
}
