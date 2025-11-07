package com.xiaofei.springbootinit.example.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootinit.example.quartz.model.entity.JobAndTrigger;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
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
