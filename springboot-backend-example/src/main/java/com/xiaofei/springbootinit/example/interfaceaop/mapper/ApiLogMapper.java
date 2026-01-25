package com.xiaofei.springbootinit.example.interfaceaop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Mapper
public interface ApiLogMapper extends BaseMapper<ApiLog> {
    
    /**
     * 查询指定时间之后创建的日志（用于增量同步）
     *
     * @param minCreateTime 最小创建时间
     * @return 日志列表
     */
    List<ApiLog> listApiLogsAfterTime(@Param("minCreateTime") Date minCreateTime);
}