package com.xiaofei.springbootinit.example.kafka.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.example.kafka.model.dto.QueryCollectTask;
import com.xiaofei.springbootinit.example.kafka.model.entity.DataDetail;
import com.xiaofei.springbootinit.example.kafka.model.entity.DataStatistics;
import com.xiaofei.springbootinit.example.kafka.model.entity.PointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author tuaofei
 * @description 采集任务
 * @date 2024/12/26
 */
@Slf4j
public class CollDataTask extends CollectAbstractTask implements Callable<String> {

    /**
     * 当前采集数据-主表
     */
    private DataStatistics dataStatistics;

    /**
     * 当前采集数据
     */
    private List<DataDetail> collectedDataList;

    /**
     * 当前采集查询对象
     */
    private QueryCollectTask collectedData;

    public CollDataTask(PointConfig config) {
        this.config = config;
        initCollect();
    }

    private void initCollect() {
        collectedData = new QueryCollectTask();
        collectedData.setPointCode(config.getPointCode());
        collectedData.setCollectTime(new Date());
    }

    /**
     * 获取当前查询参数
     *
     * @return
     */
    public QueryCollectTask getCollectedData() {
        return collectedData;
    }

    @Override
    public String call() throws Exception {
        try {
            // 发送HTTP请求
            HttpResponse response = HttpUtil.createRequest(Method.POST, config.getDataUrl())
                    .header("Content-Type", "application/json")
                    .setConnectionTimeout(5000)
                    .setReadTimeout(5000)
                    .body(JSONUtil.toJsonStr(collectedData))
                    .execute();
            // 检查响应状态
            if (response.isOk()) {
                String body = response.body();
                log.info("数据采集成功: pointCode={}, value={}, response={}",
                        config.getPointCode(),
                        body,
                        response.body());
                //下次采集时间
                collectedData.setCollectTime(DateUtil.offsetSecond(collectedData.getCollectTime(), config.getIntervalSeconds()));
                JSONObject jsonObject = JSONUtil.parseObj(body);
                JSONObject data = jsonObject.getJSONObject("data");
                // 返回
                return JSONUtil.toJsonStr(data);
            } else {
                log.error("数据采集失败: pointCode={}, statusCode={}, response={}",
                        config.getPointCode(),
                        response.getStatus(),
                        response.body());
            }
        } catch (Exception e) {
            log.error("数据采集失败: {}", config.getPointCode(), e);
        }
        return null;
    }
}
