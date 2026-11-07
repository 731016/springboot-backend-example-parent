package com.xiaofei.springbootbackendkafka.collector;

import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 采集适配器注册表
 *
 * @author tuaofei
 */
@Component
public class CollectorAdapterRegistry {

    private final Map<CollectorProtocol, CollectorAdapter> adapters = new EnumMap<>(CollectorProtocol.class);

    public CollectorAdapterRegistry(List<CollectorAdapter> adapterList) {
        for (CollectorAdapter adapter : adapterList) {
            adapters.put(adapter.protocol(), adapter);
        }
    }

    public CollectorAdapter get(CollectorProtocol protocol) {
        CollectorAdapter adapter = adapters.get(protocol);
        if (adapter == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未找到采集协议适配器: " + protocol);
        }
        return adapter;
    }
}
