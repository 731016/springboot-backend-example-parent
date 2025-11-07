package com.xiaofei.springbootinit.example.quartz.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootinit.example.quartz.job.base.BaseJob;
import com.xiaofei.springbootinit.example.quartz.model.entity.JobAndTrigger;
import com.xiaofei.springbootinit.example.quartz.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
@Slf4j
public class TestJob1 implements BaseJob {

    @Resource
    private JobService jobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行定时任务...");
        log.warn("执行时间: {}", DateUtil.now());
        JobDetail jobDetail = context.getJobDetail();
        Page<JobAndTrigger> list = jobService.list(1, 10);
        log.info("所有定时任务信息: {}", JSONUtil.toJsonStr(list));
        log.info("结束执行定时任务...");
    }
}
