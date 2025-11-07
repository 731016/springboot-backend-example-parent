package com.xiaofei.springbootinit.example.kafka.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Min(value = 1, message = "采集间隔必须大于0")
    private Integer intervalSeconds;

    private Integer isMainPoint;

    private Integer status = 1;  // 默认启用
}
