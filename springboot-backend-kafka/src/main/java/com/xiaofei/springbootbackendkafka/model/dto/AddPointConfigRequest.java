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
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPointConfigRequest {

    @NotBlank(message = "点位编码不能为空")
    private String pointCode;

    @NotBlank(message = "点位名称不能为空")
    private String pointName;

    /**
     * 计量单位，如 ℃、mm、%
     */
    private String unit;

    @NotNull(message = "采集数据源不能为空")
    private Long sourceId;

    /**
     * 协议绑定：Modbus / OPC UA 点位必填
     */
    private PointBindingDTO binding;

    private BigDecimal minLimit;

    private BigDecimal maxLimit;

    @Min(value = 2, message = "SPC子组大小不能小于2")
    @Max(value = 25, message = "SPC子组大小不能大于25")
    private Integer spcGroupSize = 5;

    private String spcRuleConfig;

    @Min(value = 1, message = "采集间隔必须大于0")
    private Integer intervalSeconds;

    private Integer isMainPoint;

    private Integer status = 1;  // 默认启用
}
