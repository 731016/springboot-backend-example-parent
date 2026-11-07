package com.xiaofei.springbootbackendkafka.collector;

import com.xiaofei.springbootbackendkafka.model.entity.CollectorHttpConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorModbusConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorOpcUaConfig;
import com.xiaofei.springbootbackendkafka.model.entity.CollectorSource;
import lombok.Builder;
import lombok.Data;

/**
 * 采集源快照（源 + 对应协议的配置）
 *
 * @author tuaofei
 */
@Data
@Builder
public class CollectorSourceSnapshot {

    private CollectorSource source;

    private CollectorHttpConfig httpConfig;

    private CollectorModbusConfig modbusConfig;

    private CollectorOpcUaConfig opcUaConfig;

    public CollectorProtocol protocol() {
        return CollectorProtocol.from(source.getProtocol());
    }
}
