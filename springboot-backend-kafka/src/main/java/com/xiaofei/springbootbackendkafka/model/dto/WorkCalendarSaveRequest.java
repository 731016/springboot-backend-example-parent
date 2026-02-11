package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 新增 / 修改工作日历请求
 */
@Data
public class WorkCalendarSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 修改时必传，新增时可为空
     */
    private Long id;

    @NotNull(message = "工作日期不能为空")
    private Date workDate;

    @NotNull(message = "班次编码不能为空")
    private String shiftCode;

    @NotNull(message = "班次名称不能为空")
    private String shiftName;

    @NotNull(message = "班次开始时间不能为空")
    private Date shiftStartTime;

    @NotNull(message = "班次结束时间不能为空")
    private Date shiftEndTime;

    /**
     * 状态：0-停用 1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}