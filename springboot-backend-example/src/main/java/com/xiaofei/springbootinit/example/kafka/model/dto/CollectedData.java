package com.xiaofei.springbootinit.example.kafka.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author tuaofei
 * @description 采集数据模型
 * @date 2024/12/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectedData {
    private String pointCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    private BigDecimal value;
    private String attributeName;
}
