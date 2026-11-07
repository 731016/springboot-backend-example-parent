package com.xiaofei.springbootbackendkafka.collector;

import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusPoint;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaPoint;
import lombok.Builder;
import lombok.Data;

/**
 * 点位协议绑定
 *
 * @author tuaofei
 */
@Data
@Builder
public class PointBinding {

    private String pointCode;

    private CollectorModbusPoint modbusPoint;

    private CollectorOpcUaPoint opcUaPoint;
}
