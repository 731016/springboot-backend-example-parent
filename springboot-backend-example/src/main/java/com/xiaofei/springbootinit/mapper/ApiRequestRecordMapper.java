package com.xiaofei.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.springbootinit.model.entity.ApiRequestRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Mapper
public interface ApiRequestRecordMapper extends BaseMapper<ApiRequestRecord> {
}
