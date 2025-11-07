package com.xiaofei.springbootinit.example.interfaceaop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Mapper
public interface ApiLogMapper extends BaseMapper<ApiLog> {
}