package com.xiaofei.springbootbackendkafka.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 新增 / 修改 班次班组基础信息
 */
@Data
public class ShiftGroupSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 修改时必传，新增时可为空
     */
    private Long id;

    /**
     * 班次编码
     */
    @NotNull(message = "班次编码不能为空")
    private String shiftCode;

    /**
     * 班次名称
     */
    @NotNull(message = "班次名称不能为空")
    private String shiftName;

    /**
     * 班次开始时间
     */
    @NotNull(message = "班次开始时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date shiftStartTime;

    /**
     * 班次结束时间
     */
    @NotNull(message = "班次结束时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date shiftEndTime;

    /**
     * 状态：0-停用 1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}

