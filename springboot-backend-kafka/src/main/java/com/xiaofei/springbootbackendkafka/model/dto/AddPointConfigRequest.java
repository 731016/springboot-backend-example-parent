package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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

    private String validUrl;

    @NotBlank(message = "数据URL不能为空")
    private String dataUrl;

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
