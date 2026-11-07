package com.xiaofei.springbootbackendkafka.collector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 单次采集结果
 *
 * @author tuaofei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectResult {

    private BigDecimal value;

    private String attributeName;
}
