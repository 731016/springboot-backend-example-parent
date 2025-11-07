package com.xiaofei.springbootinit.example.quartz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.springbootinit.example.quartz.model.dto.JobForm;
import com.xiaofei.springbootinit.example.quartz.model.dto.QueryJob;
import com.xiaofei.springbootinit.example.quartz.model.entity.JobAndTrigger;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
public interface JobService extends IService<JobAndTrigger> {

    /**
     * 添加并启动定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws Exception 异常
     */
    void addJob(JobForm form) throws Exception;

    /**
     * 删除定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    void deleteJob(JobForm form) throws SchedulerException;

    /**
     * 暂停定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    void pauseJob(JobForm form) throws SchedulerException;

    /**
     * 恢复定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws SchedulerException 异常
     */
    void resumeJob(JobForm form) throws SchedulerException;

    /**
     * 重新配置定时任务
     *
     * @param form 表单参数 {@link JobForm}
     * @throws Exception 异常
     */
    void cronJob(JobForm form) throws Exception;

    /**
     * 查询定时任务列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return 定时任务列表
     */
    Page<JobAndTrigger> list(Integer currentPage, Integer pageSize);


    /**
     * 查询该表达式的下次执行时间
     * @param cronExpression
     * @param numTimes
     * @return
     */
    List<Date> getNextFireTimes(String cronExpression, Integer numTimes);

    /**
     * 通过定时任务获取执行时间
     * @param queryJob
     * @return
     */
    List<Date> getNextFireTimesByJob(QueryJob queryJob);
}
