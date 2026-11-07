package com.xiaofei.springbootbackendkafka.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采集源测试结果
 *
 * @author tuaofei
 */
@Data
@Builder
public class CollectorSourceTestVO {

    private boolean success;

    private String message;

    private BigDecimal value;

    private String attributeName;

    private long costMillis;
}
