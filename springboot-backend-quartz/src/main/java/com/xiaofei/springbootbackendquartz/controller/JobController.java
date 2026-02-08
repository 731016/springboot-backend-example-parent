package com.xiaofei.springbootbackendquartz.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendquartz.model.dto.JobForm;
import com.xiaofei.springbootbackendquartz.model.dto.QueryJob;
import com.xiaofei.springbootbackendquartz.model.entity.JobAndTrigger;
import com.xiaofei.springbootbackendquartz.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
@RestController
@RequestMapping("/job")
@Slf4j
public class JobController {

    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * 保存定时任务
     */
    @PostMapping("/addJob")
    public BaseResponse<String> addJob(@Valid @RequestBody JobForm form) {
        try {
            jobService.addJob(form);
        } catch (Exception e) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        return ResultUtils.success("操作成功");
    }

    /**
     * 删除定时任务
     */
    @PostMapping("/deleteJob")
    public BaseResponse<String> deleteJob(@RequestBody JobForm form) {
        if (StrUtil.hasBlank(form.getJobGroupName(), form.getJobClassName())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "任务组名和类名不能为空");
        }

        try {
            boolean deleteJob = jobService.deleteJob(form);
            if (deleteJob){
                return ResultUtils.success("删除成功");
            }else{
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除定时任务失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 暂停定时任务
     */
    @PostMapping("/pause")
    public BaseResponse<String> pauseJob(@RequestBody JobForm form) throws SchedulerException {
        if (StrUtil.hasBlank(form.getJobGroupName(), form.getJobClassName())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        jobService.pauseJob(form);
        return ResultUtils.success("暂停成功");
    }

    /**
     * 恢复定时任务
     */
    @PostMapping("/resume")
    public BaseResponse<String> resumeJob(@RequestBody JobForm form) throws SchedulerException {
        if (StrUtil.hasBlank(form.getJobGroupName(), form.getJobClassName())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        jobService.resumeJob(form);
        return ResultUtils.success("恢复成功");
    }

    /**
     * 修改定时任务，定时时间
     */
    @PostMapping("/cron")
    public BaseResponse<String> cronJob(@Valid @RequestBody JobForm form) {
        try {
            jobService.cronJob(form);
        } catch (Exception e) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        return ResultUtils.success("修改成功");
    }

    @GetMapping("/jobList")
    public BaseResponse<List<JobAndTrigger>> jobList(Integer currentPage, Integer pageSize) {
        if (ObjectUtil.isNull(currentPage)) {
            currentPage = 1;
        }
        if (ObjectUtil.isNull(pageSize)) {
            pageSize = 10;
        }
        Page<JobAndTrigger> all = jobService.list(currentPage, pageSize);
        List<JobAndTrigger> records = all.getRecords();
        return ResultUtils.success(records);
    }

    @PostMapping("/queryJobNextFireTimes")
    public BaseResponse<List<Date>> queryJobNextFireTimes(@Valid QueryJob queryJob) {
        List<Date> dateList = new ArrayList<>();
        try {
            dateList = jobService.getNextFireTimesByJob(queryJob);
        } catch (Exception e) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        return ResultUtils.success(dateList);
    }

}
