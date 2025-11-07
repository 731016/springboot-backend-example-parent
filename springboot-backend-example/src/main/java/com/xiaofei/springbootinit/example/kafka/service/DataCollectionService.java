package com.xiaofei.springbootinit.example.kafka.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.example.kafka.constants.KafkaConstants;
import com.xiaofei.springbootinit.example.kafka.mapper.PointConfigMapper;
import com.xiaofei.springbootinit.example.kafka.model.entity.PointConfig;
import com.xiaofei.springbootinit.example.kafka.model.vo.TaskStatusVO;
import com.xiaofei.springbootinit.example.kafka.task.CollDataTask;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author tuaofei
 * @description 数据采集服务
 * @date 2024/12/26
 */
@Service
@Slf4j
public class DataCollectionService {

    @Autowired
    private PointConfigMapper pointConfigMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    private final Map<String, CollDataTask> taskInstances = new ConcurrentHashMap<>();

    private final Map<String, ScheduledFuture<?>> collectionTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);


    public void startAllCollectTasks() {
        List<PointConfig> allEnabledPoints = pointConfigMapper.getAllEnabledPoints();
        if (CollectionUtil.isNotEmpty(allEnabledPoints)) {
            for (PointConfig pointConfig : allEnabledPoints) {
                String pointCode = pointConfig.getPointCode();
                // 检查任务是否已启动
                if (collectionTasks.containsKey(pointCode)) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "采集任务已在运行中");
                }
                // 创建并启动采集任务
                ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                        () -> {
                            try {
                                collectData(pointConfig);
                            } catch (Exception e) {
                                log.error("采集线程异常: {}", pointConfig.getPointCode(), e);
                                try {
                                    // 发生异常时等待指定时间
                                    Thread.sleep(TimeUnit.SECONDS.toMillis(pointConfig.getIntervalSeconds()));
                                    log.warn("{}秒后重试采集: {}",
                                            pointConfig.getIntervalSeconds(),
                                            pointConfig.getPointCode());
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    log.error("等待被中断: {}", pointConfig.getPointCode(), ie);
                                }
                            }
                        },
                        0,
                        pointConfig.getIntervalSeconds(),
                        TimeUnit.SECONDS
                );

                // 保存任务引用
                collectionTasks.put(pointCode, future);

                // 更新运行状态
                pointConfigMapper.updateRunningStatus(pointCode, 1);

                log.info("采集任务已启动: {}", pointCode);
            }
        }
    }

    /**
     * 启动采集任务
     */
    public void startCollection(String pointCode) {
        // 获取点位配置
        PointConfig config = pointConfigMapper.getByPointCode(pointCode);
        if (config == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点位配置不存在");
        }

        // 检查任务是否已启动
        if (collectionTasks.containsKey(pointCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "采集任务已在运行中");
        }

        // 创建并启动采集任务
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        collectData(config);
                    } catch (Exception e) {
                        log.error("采集线程异常: {}", config.getPointCode(), e);
                        try {
                            // 发生异常时等待指定时间
                            Thread.sleep(TimeUnit.SECONDS.toMillis(config.getIntervalSeconds()));
                            log.warn("{}秒后重试采集: {}",
                                    config.getIntervalSeconds(),
                                    config.getPointCode());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("等待被中断: {}", config.getPointCode(), ie);
                        }
                    }
                },
                0,
                config.getIntervalSeconds(),
                TimeUnit.SECONDS
        );

        // 保存任务引用
        collectionTasks.put(pointCode, future);

        // 更新运行状态
        pointConfigMapper.updateRunningStatus(pointCode, 1);

        log.info("采集任务已启动: {}", pointCode);
    }

    /**
     * 停止采集任务
     */
    public void stopCollection(String pointCode) {
        ScheduledFuture<?> future = collectionTasks.remove(pointCode);
        if (future != null) {
            future.cancel(false);
            // 更新运行状态
            pointConfigMapper.updateRunningStatus(pointCode, 0);
            log.info("采集任务已停止: {}", pointCode);
        }
    }

    /**
     * 获取任务状态
     */
    public TaskStatusVO getCollectionStatus(String pointCode) {
        PointConfig config = pointConfigMapper.getByPointCode(pointCode);
        if (config == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点位配置不存在");
        }

        return TaskStatusVO.builder()
                .pointCode(pointCode)
                .pointName(config.getPointName())
                .running(collectionTasks.containsKey(pointCode))
                .lastCollectTime(getLastCollectTime(pointCode))
                .build();
    }

    /**
     * 获取所有任务状态
     */
    public List<TaskStatusVO> getAllCollectionStatus() {
        List<PointConfig> configs = pointConfigMapper.getAllEnabledPoints();
        return configs.stream()
                .map(config -> TaskStatusVO.builder()
                        .pointCode(config.getPointCode())
                        .pointName(config.getPointName())
                        .running(collectionTasks.containsKey(config.getPointCode()))
                        .lastCollectTime(getLastCollectTime(config.getPointCode()))
                        .build())
                .collect(Collectors.toList());
    }

    private void collectData(PointConfig config) throws Exception {
        // 获取或创建任务实例
        CollDataTask collDataTask = taskInstances.computeIfAbsent(
                config.getPointCode(),
                k -> new CollDataTask(config)
        );

        try {
            String result = collDataTask.call();
            // 将结果转换为 JSON 字符串
            kafkaTemplate.send(KafkaConstants.RAW_DATA_TOPIC, config.getPointCode(), JSONUtil.toJsonStr(result));
            // 成功后重置重试次数
            collDataTask.resetRetryCount();
        } catch (Exception e) {
            // 增加重试等待时间并抛出异常
            collDataTask.incrementRetryCount();
            throw e;
        }
    }

    private Date getLastCollectTime(String pointCode) {
        // 从数据明细表获取最后采集时间
        return null; // TODO: 实现此方法
    }

    @PreDestroy
    public void shutdown() {
        // 停止所有任务
        collectionTasks.forEach((pointCode, future) -> {
            future.cancel(false);
            pointConfigMapper.updateRunningStatus(pointCode, 0);
        });
        collectionTasks.clear();

        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}