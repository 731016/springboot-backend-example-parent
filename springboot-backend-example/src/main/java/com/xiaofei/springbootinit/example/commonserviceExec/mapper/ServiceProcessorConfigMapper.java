package com.xiaofei.springbootinit.example.commonserviceExec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.commonserviceExec.model.entity.ServiceProcessorConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceProcessorConfigMapper extends BaseMapper<ServiceProcessorConfig> {
}
