package com.xiaofei.springbootinit.example.quartz.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.example.quartz.job.base.BaseJob;
import com.xiaofei.springbootinit.example.quartz.mapper.JobMapper;
import com.xiaofei.springbootinit.example.quartz.model.dto.JobForm;
import com.xiaofei.springbootinit.example.quartz.model.dto.QueryJob;
import com.xiaofei.springbootinit.example.quartz.model.entity.JobAndTrigger;
import com.xiaofei.springbootinit.example.quartz.service.JobService;
import com.xiaofei.springbootinit.example.quartz.utils.JobUtil;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
@Service
@Slf4j
public class JobServiceImpl extends ServiceImpl<JobMapper, JobAndTrigger> implements JobService {

    private final Scheduler scheduler;

    private final JobMapper jobMapper;

    @Autowired
    public JobServiceImpl(Scheduler scheduler, JobMapper jobMapper) {
        this.scheduler = scheduler;
        this.jobMapper = jobMapper;
    }

    /**
     * 添加并启动定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws Exception 异常
     */
    @Override
    public void addJob(JobForm form) throws Exception {
        //启动调度器
        scheduler.start();

        //构建Job信息
        Class<? extends BaseJob> jobClass = JobUtil.getClass(form.getJobClassName()).getClass();
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass);
        JobDetail jobDetail = jobBuilder.withIdentity(form.getJobClassName(), form.getJobGroupName()).build();

        // cron表达式调度构建器（任务执行时间）
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(form.getCronExpression());

        //根据cron表达式构建一个trigger
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        CronTrigger trigger = triggerBuilder.withIdentity(form.getJobClassName(), form.getJobGroupName()).withSchedule(cron).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("【定时任务】创建失败！", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "【定时任务】创建失败！");
        }
    }

    /**
     * 删除定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    @Override
    public void deleteJob(JobForm form) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(form.getJobClassName(), form.getJobGroupName());
        //暂停触发器
        scheduler.pauseTrigger(triggerKey);
        //移除触发器
        scheduler.unscheduleJob(triggerKey);

        JobKey jobKey = JobKey.jobKey(form.getJobClassName(), form.getJobGroupName());
        //删除任务
        scheduler.deleteJob(jobKey);
    }

    /**
     * 暂停定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    @Override
    public void pauseJob(JobForm form) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(form.getJobClassName(), form.getJobGroupName()));
    }

    /**
     * 恢复定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    @Override
    public void resumeJob(JobForm form) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(form.getJobClassName(), form.getJobGroupName()));
    }

    /**
     * 重新配置定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws Exception 异常
     */
    @Override
    public void cronJob(JobForm form) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(form.getJobClassName(), form.getJobGroupName());

        // cron表达式调度构建器（任务执行时间）
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(form.getCronExpression());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cron).build();

        try {
            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            log.error("【定时任务】更新失败！", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "【定时任务】更新失败！");
        }
    }

    /**
     * 查询定时任务列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return 定时任务列表
     */
    @Override
    public Page<JobAndTrigger> list(Integer currentPage, Integer pageSize) {
        Page<JobAndTrigger> page = new Page<>(currentPage, pageSize);
        List<JobAndTrigger> list = jobMapper.list(page);
        page.setRecords(list);
        return page;
    }

    @Override
    public List<Date> getNextFireTimes(String cronExpression, Integer numTimes) {
        List<Date> nextFireTimes = new ArrayList<>();
        try {
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            Date now = new Date();
            Date nextFireTime = trigger.getFireTimeAfter(now);

            // 获取接下来的n次执行时间
            for (int i = 0; i < numTimes && nextFireTime != null; i++) {
                nextFireTimes.add(nextFireTime);
                nextFireTime = trigger.getFireTimeAfter(nextFireTime);
            }

        } catch (Exception e) {
            log.error("获取下次执行时间失败: {}", cronExpression, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cron表达式无效");
        }
        return nextFireTimes;
    }

    @Override
    public List<Date> getNextFireTimesByJob(QueryJob queryJob) {
        List<Date> dateList = new ArrayList<>();
        if (queryJob == null) {
            return dateList;
        }
        JobAndTrigger jobAndTriggerQuery = new JobAndTrigger();
        jobAndTriggerQuery.setJobClassName(queryJob.getJobClassName());
        jobAndTriggerQuery.setJobGroup(queryJob.getJobGroupName());
        List<JobAndTrigger> jobAndTriggers = jobMapper.selectQrtzCronTriggers(jobAndTriggerQuery);
        if (CollectionUtil.isEmpty(jobAndTriggers)) {
            return dateList;
        }
        JobAndTrigger jobAndTrigger = jobAndTriggers.get(0);
        String cronExpression = jobAndTrigger.getCronExpression();
        List<Date> nextFireTimes = getNextFireTimes(cronExpression, 5);
        return nextFireTimes;
    }
}
