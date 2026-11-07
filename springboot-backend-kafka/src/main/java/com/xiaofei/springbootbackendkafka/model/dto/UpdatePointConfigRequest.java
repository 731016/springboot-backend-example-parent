package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新采集点配置请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePointConfigRequest {

    /**
     * 主键 ID
     */
    @NotNull(message = "ID 不能为空")
    private Long id;

    /**
     * 点位编码（目前不允许修改，仅校验唯一性）
     */
    @NotBlank(message = "点位编码不能为空")
    private String pointCode;

    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    private String validUrl;

    @NotBlank(message = "数据URL不能为空")
    private String dataUrl;

    private BigDecimal minLimit;

    private BigDecimal maxLimit;

    @Min(value = 2, message = "SPC子组大小不能小于2")
    @Max(value = 25, message = "SPC子组大小不能大于25")
    private Integer spcGroupSize;

    private String spcRuleConfig;

    @Min(value = 1, message = "采集间隔必须大于0")
    private Integer intervalSeconds;

    private Integer isMainPoint;

    private Integer status;
}

