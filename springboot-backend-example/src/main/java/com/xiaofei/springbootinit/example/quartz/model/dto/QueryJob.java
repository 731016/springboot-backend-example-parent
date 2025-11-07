package com.xiaofei.springbootinit.example.quartz.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/25
 */
@Data
public class QueryJob {
    /**
     * 定时任务全类名
     */
    @NotBlank(message = "类名不能为空")
    private String jobClassName;
    /**
     * 任务组名
     */
    @NotBlank(message = "任务组名不能为空")
    private String jobGroupName;
    /**
     * 定时任务cron表达式
     */
    private String cronExpression;
}
