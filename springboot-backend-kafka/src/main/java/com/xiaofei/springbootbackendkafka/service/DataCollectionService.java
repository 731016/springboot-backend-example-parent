package com.xiaofei.springbootbackendkafka.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.collector.CollectorAdapter;
import com.xiaofei.springbootbackendkafka.collector.CollectorAdapterRegistry;
import com.xiaofei.springbootbackendkafka.collector.CollectorProtocol;
import com.xiaofei.springbootbackendkafka.collector.CollectorSourceSnapshot;
import com.xiaofei.springbootbackendkafka.collector.PointBinding;
import com.xiaofei.springbootbackendkafka.constants.KafkaConstants;
import com.xiaofei.springbootbackendkafka.mapper.DataDetailMapper;
import com.xiaofei.springbootbackendkafka.mapper.DataStatisticsMapper;
import com.xiaofei.springbootbackendkafka.mapper.PointConfigMapper;
import com.xiaofei.springbootbackendkafka.model.entity.DataDetail;
import com.xiaofei.springbootbackendkafka.model.entity.DataStatistics;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import com.xiaofei.springbootbackendkafka.model.vo.TaskStatusVO;
import com.xiaofei.springbootbackendkafka.task.CollDataTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Arrays;
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
    private DataDetailMapper dataDetailMapper;

    @Autowired
    private DataStatisticsMapper dataStatisticsMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CollectorSourceService collectorSourceService;

    @Autowired
    private CollectorPointBindingService collectorPointBindingService;

    @Autowired
    private CollectorAdapterRegistry collectorAdapterRegistry;


    private final Map<String, CollDataTask> taskInstances = new ConcurrentHashMap<>();

    private final Map<String, ScheduledFuture<?>> collectionTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);


    public void startAllCollectTasks() {
        List<PointConfig> allEnabledPoints = pointConfigMapper.getAllEnabledPoints();
        if (CollectionUtil.isEmpty(allEnabledPoints)) {
            return;
        }
        for (PointConfig pointConfig : allEnabledPoints) {
            String pointCode = pointConfig.getPointCode();
            // 检查任务是否已启动
            if (collectionTasks.containsKey(pointCode)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "采集任务已在运行中");
            }
            CollDataTask task;
            try {
                task = createTask(pointConfig);
            } catch (Exception e) {
                log.error("创建采集任务失败，跳过点位: {}", pointCode, e);
                continue;
            }
            taskInstances.put(pointCode, task);
            schedule(pointCode, pointConfig.getIntervalSeconds());

            // 更新运行状态
            pointConfigMapper.updateRunningStatus(pointCode, 1);

            log.info("采集任务已启动: {}", pointCode);
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

        CollDataTask task = createTask(config);
        taskInstances.put(pointCode, task);
        schedule(pointCode, config.getIntervalSeconds());

        //点位启用
        pointConfigMapper.updatePointsStatus(Arrays.asList(pointCode), 1);

        // 更新运行状态
        pointConfigMapper.updateRunningStatus(pointCode, 1);

        log.info("采集任务已启动: {}", pointCode);
    }

    /**
     * 点位配置变更后重启采集任务（未运行时忽略）
     */
    public void reloadPoint(String pointCode) {
        if (!collectionTasks.containsKey(pointCode)) {
            taskInstances.remove(pointCode);
            return;
        }
        stopCollection(pointCode);
        startCollection(pointCode);
    }

    /**
     * 数据源变更后重启引用该数据源的点位任务
     */
    public void restartBySource(Long sourceId) {
        if (sourceId == null) {
            return;
        }
        List<PointConfig> points = pointConfigMapper.selectList(
                new QueryWrapper<PointConfig>().eq("sourceId", sourceId));
        for (PointConfig point : points) {
            try {
                reloadPoint(point.getPointCode());
            } catch (Exception e) {
                log.error("重启采集任务失败: {}", point.getPointCode(), e);
            }
        }
    }

    private void schedule(String pointCode, Integer intervalSeconds) {
        int interval = intervalSeconds == null || intervalSeconds < 1 ? 1 : intervalSeconds;
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        collectData(pointCode);
                    } catch (Exception e) {
                        log.error("采集线程异常: {}", pointCode, e);
                        try {
                            // 发生异常时等待指定时间
                            Thread.sleep(TimeUnit.SECONDS.toMillis(interval));
                            log.warn("{}秒后重试采集: {}", interval, pointCode);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("等待被中断: {}", pointCode, ie);
                        }
                    }
                },
                0,
                interval,
                TimeUnit.SECONDS
        );
        collectionTasks.put(pointCode, future);
    }

    private CollDataTask createTask(PointConfig config) {
        if (config.getSourceId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "点位未关联采集数据源: " + config.getPointCode());
        }
        CollectorSourceSnapshot source = collectorSourceService.getSnapshot(config.getSourceId());
        if (!Integer.valueOf(1).equals(source.getSource().getStatus())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "采集数据源已禁用: " + source.getSource().getSourceName());
        }
        CollectorProtocol protocol = source.protocol();
        PointBinding binding = collectorPointBindingService.getBinding(config, protocol);
        CollectorAdapter adapter = collectorAdapterRegistry.get(protocol);
        return new CollDataTask(config, source, binding, adapter);
    }

    /**
     * 停止采集任务
     */
    public void stopCollection(String pointCode) {
        ScheduledFuture<?> future = collectionTasks.remove(pointCode);
        if (future != null) {
            future.cancel(false);
        }
        taskInstances.remove(pointCode);
        // 更新运行状态
        pointConfigMapper.updateRunningStatus(pointCode, 0);
        log.info("采集任务已停止: {}", pointCode);
    }

    /**
     * 获取任务状态
     */
    public TaskStatusVO getCollectionStatus(String pointCode) {
        PointConfig config = pointConfigMapper.getByPointCode(pointCode);
        if (config == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点位配置不存在");
        }
        Date lastCollectTime = getLastCollectTime(pointCode);
        return TaskStatusVO.builder()
                .pointCode(pointCode)
                .pointName(config.getPointName())
                .running(collectionTasks.containsKey(pointCode))
                .lastCollectTime(getLastCollectTime(pointCode))
                .nextCollectTime(DateUtil.offsetSecond(lastCollectTime,config.getIntervalSeconds()))
                .build();
    }

    /**
     * 获取所有任务状态
     */
    public List<TaskStatusVO> getAllCollectionStatus() {
        List<PointConfig> configs = pointConfigMapper.getAllEnabledPoints();
        return configs.stream()
                .map(config -> {
                    Date lastCollectTime = getLastCollectTime(config.getPointCode());
                    Integer intervalSeconds = config.getIntervalSeconds();
                    return TaskStatusVO.builder()
                            .pointCode(config.getPointCode())
                            .pointName(config.getPointName())
                            .running(collectionTasks.containsKey(config.getPointCode()))
                            .lastCollectTime(lastCollectTime)
                            .nextCollectTime(DateUtil.offsetSecond(lastCollectTime, intervalSeconds))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void collectData(String pointCode) throws Exception {
        CollDataTask collDataTask = taskInstances.get(pointCode);
        if (collDataTask == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "采集任务未初始化: " + pointCode);
        }

        try {
            String result = collDataTask.call();
            if (StringUtils.isNotEmpty(result)) {
                // 将结果转换为 JSON 字符串
                kafkaTemplate.send(KafkaConstants.RAW_DATA_TOPIC, pointCode, JSONUtil.toJsonStr(result));
                // 成功后重置重试次数
                collDataTask.resetRetryCount();
            }
        } catch (Exception e) {
            // 增加重试等待时间并抛出异常
            collDataTask.incrementRetryCount();
            throw e;
        }
    }

    private Date getLastCollectTime(String pointCode) {
        // 从数据明细表获取最后采集时间
        Date lastCollectTime = pointConfigMapper.getLastCollectTime(pointCode);
        return lastCollectTime;
    }

    /** 明细查询默认返回的最近数据条数 */
    private static final int DEFAULT_DETAIL_LIMIT = 2000;

    /** 明细查询单次最大返回条数，避免一次性拉取全量数据 */
    private static final int MAX_DETAIL_LIMIT = 5000;

    /**
     * 根据点位编码查询采集数据
     *
     * @param pointCode 点位编码
     * @param limit     最多返回条数，为空时使用默认值
     * @return 采集数据列表
     */
    public List<DataDetail> getDataDetailsByPointCode(String pointCode, Integer limit) {
        if (StringUtils.isBlank(pointCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位编码不能为空");
        }
        int size = (limit == null || limit <= 0)
                ? DEFAULT_DETAIL_LIMIT
                : Math.min(limit, MAX_DETAIL_LIMIT);
        return dataDetailMapper.getDetailsByPointCode(pointCode, size);
    }

    /**
     * 根据点位编码查询统计数据
     *
     * @param pointCode 点位编码
     * @return 统计数据列表
     */
    public List<DataStatistics> getDataStatisticsByPointCode(String pointCode) {
        if (StringUtils.isBlank(pointCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点位编码不能为空");
        }
        return dataStatisticsMapper.getStatisticsByPointCode(pointCode);
    }

    @PreDestroy
    public void shutdown() {
        // 停止所有任务
        collectionTasks.forEach((pointCode, future) -> {
            future.cancel(false);
            pointConfigMapper.updateRunningStatus(pointCode, 0);
        });
        collectionTasks.clear();
        taskInstances.clear();

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