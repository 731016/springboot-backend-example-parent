package com.xiaofei.springbootbackendkafka.task;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootbackendkafka.collector.CollectResult;
import com.xiaofei.springbootbackendkafka.collector.CollectorAdapter;
import com.xiaofei.springbootbackendkafka.collector.CollectorSourceSnapshot;
import com.xiaofei.springbootbackendkafka.collector.PointBinding;
import com.xiaofei.springbootbackendkafka.model.dto.CollectedData;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * @author tuaofei
 * @description 采集任务
 * @date 2024/12/26
 */
@Slf4j
public class CollDataTask extends CollectAbstractTask implements Callable<String> {

    /**
     * 采集源快照
     */
    private final CollectorSourceSnapshot source;

    /**
     * 点位协议绑定
     */
    private final PointBinding binding;

    /**
     * 协议适配器
     */
    private final CollectorAdapter adapter;

    public CollDataTask(PointConfig config, CollectorSourceSnapshot source,
                        PointBinding binding, CollectorAdapter adapter) {
        super(config, 0);
        this.source = source;
        this.binding = binding;
        this.adapter = adapter;
    }

    @Override
    public String call() {
        try {
            Date collectTime = new Date();
            CollectResult result = adapter.collect(source, binding, collectTime);
            if (result == null || result.getValue() == null) {
                log.warn("采集结果为空: pointCode={}", config.getPointCode());
                return null;
            }
            CollectedData data = new CollectedData();
            data.setPointCode(config.getPointCode());
            data.setCollectTime(collectTime);
            data.setValue(result.getValue());
            data.setAttributeName(result.getAttributeName());
            log.info("数据采集成功: pointCode={}, value={}", config.getPointCode(), result.getValue());
            return JSONUtil.toJsonStr(data);
        } catch (Exception e) {
            log.error("数据采集失败: {}", config.getPointCode(), e);
        }
        return null;
    }
}
