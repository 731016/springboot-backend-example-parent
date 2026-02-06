package com.xiaofei.springbootbackendquartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendquartz.model.entity.JobAndTrigger;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
@Mapper
public interface JobMapper extends BaseMapper<JobAndTrigger> {

    /**
     * 查询定时作业和触发器列表
     * @return 定时作业和触发器列表
     */
    List<JobAndTrigger> list(Page<JobAndTrigger> page);

    /**
     * 查询指定定时任务
     * @param jobAndTrigger
     * @return
     */
    List<JobAndTrigger> selectQrtzCronTriggers(JobAndTrigger jobAndTrigger);
}
