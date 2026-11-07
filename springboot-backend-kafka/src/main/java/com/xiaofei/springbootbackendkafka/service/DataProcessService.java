package com.xiaofei.springbootbackendkafka.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.constants.KafkaConstants;
import com.xiaofei.springbootbackendkafka.mapper.DataDetailMapper;
import com.xiaofei.springbootbackendkafka.mapper.DataStatisticsMapper;
import com.xiaofei.springbootbackendkafka.mapper.PointConfigMapper;
import com.xiaofei.springbootbackendkafka.mapper.WorkCalendarMapper;
import com.xiaofei.springbootbackendkafka.model.dto.CollectedData;
import com.xiaofei.springbootbackendkafka.model.entity.DataDetail;
import com.xiaofei.springbootbackendkafka.model.entity.DataStatistics;
import com.xiaofei.springbootbackendkafka.model.entity.PointConfig;
import com.xiaofei.springbootbackendkafka.model.entity.WorkCalendar;
import com.xiaofei.springbootbackendwebsocket.common.WebSocketConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author tuaofei
 * @description 数据处理服务（Kafka消费者）
 * @date 2024/12/26
 */
@Service
@Slf4j
public class DataProcessService {

    @Autowired
    private DataStatisticsMapper dataStatisticsMapper;

    @Autowired
    private DataDetailMapper dataDetailMapper;

    @Autowired
    private PointConfigMapper pointConfigMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private SimpMessagingTemplate wsTemplate;

    @Autowired
    private WorkCalendarMapper workCalendarMapper;

    @KafkaListener(topics = KafkaConstants.RAW_DATA_TOPIC, groupId = "${app.kafka.raw-data.group-id:raw-data-process-group}", containerFactory = "ackContainerFactory")
    public void processData(ConsumerRecords<String, String> records, Acknowledgment acknowledgment) {
        Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
        while (iterator.hasNext()) {
            ConsumerRecord<String, String> record = iterator.next();
            String value = record.value();

            // 获取当前重试次数
            int retryCount = getRetryCount(record);
            try {
                processMessage(value);

                // 处理成功，确认消息
                acknowledgment.acknowledge();
                log.info("消息处理成功，已提交offset");

            } catch (Exception e) {
                log.error("数据处理失败: topic = {}, partition = {}, offset = {}, retryCount = {}",
                        record.topic(), record.partition(), record.offset(), retryCount, e);
                handleFailedMessage(record, retryCount, e);
                // 确认原消息，因为消息已经被转发到重试队列或死信队列
                acknowledgment.acknowledge();
            }
        }
    }

    private void processMessage(String message) throws JsonProcessingException {
        // 业务处理逻辑
        CollectedData data = new ObjectMapper()
                .readValue(message, CollectedData.class);

        // 保存明细数据
        DataDetail detail = new DataDetail();
        detail.setPointCode(data.getPointCode());
        detail.setCollectTime(data.getCollectTime());
        detail.setValue(data.getValue());
        detail.setAttributeName(data.getAttributeName());
        // 根据工作日历当前班次时间段判断是否为正常数据
        WorkCalendar currentShift = findCurrentShift(data.getCollectTime());
        boolean inCurrentShift = currentShift != null;
        detail.setDataType(inCurrentShift ? KafkaConstants.DATA_TYPE_NORMAL : KafkaConstants.DATA_TYPE_NON_STATISTICAL);

        Integer dataType = detail.getDataType();
        if (KafkaConstants.DATA_TYPE_NORMAL.equals(dataType)) {
            // 获取点位配置
            PointConfig config = pointConfigMapper.getByPointCode(detail.getPointCode());
            if (config == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点位配置不存在");
            }
            BigDecimal minLimit = config.getMinLimit();
            BigDecimal maxLimit = config.getMaxLimit();
            BigDecimal value = detail.getValue();
            if (minLimit != null && maxLimit != null) {
                if (value.compareTo(minLimit) >= 0 && value.compareTo(maxLimit) < 0) {

                }else{
                    detail.setDataType(KafkaConstants.DATA_TYPE_MORE_THAN_LIMIT);
                }
            }
        }

        if (inCurrentShift) {
            // 班次内的数据按点位归入同一条进行中的统计记录
            DataStatistics statistics = dataStatisticsMapper.selectOne(
                    new QueryWrapper<DataStatistics>()
                            .eq("pointCode", data.getPointCode())
                            .eq("status", 1)
            );

            // 进行中的统计记录不属于本次采集所在班次时，先关闭旧记录
            if (statistics != null
                    && !isSameShift(statistics.getStartTime(), detail.getCollectTime(), currentShift)) {
                closeStatistics(statistics);
                statistics = null;
            }

            if (statistics != null) {
                detail.setStatisticsId(statistics.getId());
                dataDetailMapper.insert(detail);

                // 更新统计数据
                updateStatistics(statistics.getId());
            } else {
                createStatistics(detail);
            }
        } else {
            // 非统计数据：仍保存明细，只是不参与统计
            dataDetailMapper.insert(detail);
        }

        // 通过 WebSocket 推送实时数据
        if (wsTemplate != null) {
            try {
                wsTemplate.convertAndSend(WebSocketConsts.PUSH_KAFKA_DATA, detail);
                log.info("已推送实时数据到 WebSocket: pointCode={}, value={}", detail.getPointCode(), detail.getValue());
            } catch (Exception e) {
                log.warn("推送 WebSocket 消息失败", e);
            }
        }
    }

    /**
     * 获取当前重试次数
     *
     * @param record
     * @return
     */
    private int getRetryCount(ConsumerRecord<String, String> record) {
        Headers headers = record.headers();
        Header retryHeader = headers.lastHeader(KafkaConstants.RAW_DATA_HEADER_RETRY_COUNT);
        return retryHeader == null ? 0 : ByteBuffer.wrap(retryHeader.value()).getInt();
    }

    /**
     * 处理失败消息
     *
     * @param record
     * @param retryCount
     * @param e
     */
    private void handleFailedMessage(ConsumerRecord<String, String> record, int retryCount, Exception e) {
        if (retryCount < KafkaConstants.RAW_DATA_MAX_RETRY_COUNT) {
            // 未达到最大重试次数，发送到重试队列
            sendToRetryTopic(record, retryCount + 1);
        } else {
            // 达到最大重试次数，发送到死信队列
            sendToDLQ(record, e);
        }
    }

    /**
     * 发送到重试队列
     *
     * @param record
     * @param nextRetryCount
     */
    private void sendToRetryTopic(ConsumerRecord<String, String> record, int nextRetryCount) {
        ProducerRecord<String, String> retryRecord = new ProducerRecord<>(
                KafkaConstants.RAW_DATA_TOPIC,  // 仍然发送到原主题
                record.partition(),
                record.key(),
                record.value()
        );

        // 添加重试次数到header
        retryRecord.headers().add(
                KafkaConstants.RAW_DATA_HEADER_RETRY_COUNT,
                ByteBuffer.allocate(4).putInt(nextRetryCount).array()
        );

        try {
            kafkaTemplate.send(retryRecord).get(10, TimeUnit.SECONDS);
            log.info("消息已发送到重试队列: retryCount = {}", nextRetryCount);
        } catch (Exception ex) {
            log.error("发送到重试队列失败", ex);
            // 如果发送重试消息失败，则发送到死信队列
            sendToDLQ(record, ex);
        }
    }

    /**
     * 发送到死信队列
     *
     * @param record
     * @param e
     */
    private void sendToDLQ(ConsumerRecord<String, String> record, Exception e) {
        // 构建死信消息，包含原始消息信息和错误信息
        Map<String, Object> dlqMessage = new HashMap<>();
        dlqMessage.put("originalTopic", record.topic());
        dlqMessage.put("originalPartition", record.partition());
        dlqMessage.put("originalOffset", record.offset());
        dlqMessage.put("originalMessage", record.value());
        dlqMessage.put("error", e.getMessage());
        dlqMessage.put("stackTrace", ExceptionUtils.getStackTrace(e));
        dlqMessage.put("timestamp", System.currentTimeMillis());

        try {
            String dlqMessageJson = new ObjectMapper().writeValueAsString(dlqMessage);
            kafkaTemplate.send(KafkaConstants.RAW_DATA_TOPIC_DLQ, dlqMessageJson).get(10, TimeUnit.SECONDS);
            log.info("消息已发送到死信队列");
        } catch (Exception ex) {
            log.error("发送到死信队列失败", ex);
        }
    }

    /**
     * 新建一条进行中的统计记录，并关联当前明细
     */
    private void createStatistics(DataDetail detail) {
        DataStatistics statistics = new DataStatistics();
        statistics.setPointCode(detail.getPointCode());
        statistics.setStartTime(detail.getCollectTime());
        statistics.setEndTime(detail.getCollectTime());
        statistics.setSampleCount(0);
        statistics.setOutOfLimitCount(0);
        statistics.setStatus(1);

        if (isNormalDetail(detail)) {
            statistics.setSampleCount(1);
            statistics.setMaximumValue(detail.getValue());
            statistics.setMinimumValue(detail.getValue());
            statistics.setAverageValue(detail.getValue());
            statistics.setStandardDeviation(BigDecimal.ZERO);
        } else {
            statistics.setOutOfLimitCount(1);
        }

        statistics.setCreateTime(detail.getCollectTime());
        statistics.setUpdateTime(detail.getCollectTime());
        dataStatisticsMapper.insert(statistics);

        detail.setStatisticsId(statistics.getId());
        dataDetailMapper.insert(detail);
    }

    /**
     * 关闭指定统计记录（班次结束后不再接收数据）
     */
    private void closeStatistics(DataStatistics statistics) {
        DataStatistics update = new DataStatistics();
        update.setId(statistics.getId());
        update.setStatus(2);
        update.setEndTime(statistics.getEndTime() != null
                ? statistics.getEndTime() : statistics.getUpdateTime());
        dataStatisticsMapper.updateById(update);
    }

    private void updateStatistics(Long statisticsId) {
        // 获取该统计ID下的所有明细数据
        List<DataDetail> details = dataDetailMapper.selectList(
                new QueryWrapper<DataDetail>()
                        .eq("statisticsId", statisticsId)
        );

        // 仅使用正常数据（dataType 为空或为正常数据）参与统计
        List<DataDetail> validDetails = details.stream()
                .filter(this::isNormalDetail)
                .collect(Collectors.toList());

        DataStatistics statistics = new DataStatistics();
        statistics.setId(statisticsId);
        statistics.setSampleCount(validDetails.size());
        statistics.setOutOfLimitCount((int) details.stream()
                .filter(detail -> KafkaConstants.DATA_TYPE_MORE_THAN_LIMIT.equals(detail.getDataType()))
                .count());

        if (!validDetails.isEmpty()) {
            BigDecimal sumValue = validDetails.stream()
                    .map(DataDetail::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal preciseAverage = sumValue.divide(
                    BigDecimal.valueOf(validDetails.size()), 10, RoundingMode.HALF_UP);

            BigDecimal maxValue = validDetails.stream()
                    .map(DataDetail::getValue)
                    .max(BigDecimal::compareTo)
                    .orElse(null);

            BigDecimal minValue = validDetails.stream()
                    .map(DataDetail::getValue)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            statistics.setMaximumValue(maxValue);
            statistics.setMinimumValue(minValue);
            statistics.setAverageValue(preciseAverage.setScale(6, RoundingMode.HALF_UP));
            statistics.setStandardDeviation(calcStandardDeviation(validDetails, preciseAverage));
        }

        // 起止时间取该统计记录下明细的实际时间范围
        details.stream()
                .map(DataDetail::getCollectTime)
                .filter(Objects::nonNull)
                .min(Date::compareTo)
                .ifPresent(statistics::setStartTime);
        details.stream()
                .map(DataDetail::getCollectTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .ifPresent(statistics::setEndTime);

        dataStatisticsMapper.updateById(statistics);
    }

    /**
     * 计算样本标准差（n-1）
     */
    private BigDecimal calcStandardDeviation(List<DataDetail> details, BigDecimal average) {
        if (details.size() < 2) {
            return BigDecimal.ZERO;
        }
        BigDecimal sumOfSquares = details.stream()
                .map(detail -> detail.getValue().subtract(average).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = sumOfSquares.divide(
                BigDecimal.valueOf(details.size() - 1), 10, RoundingMode.HALF_UP);
        return variance.sqrt(new MathContext(10, RoundingMode.HALF_UP))
                .setScale(6, RoundingMode.HALF_UP);
    }

    private boolean isNormalDetail(DataDetail detail) {
        return detail.getDataType() == null
                || KafkaConstants.DATA_TYPE_NORMAL.equals(detail.getDataType());
    }

    /**
     * 判断统计记录的起始时间与本次采集时间是否属于同一个班次
     *
     * @param statisticsStartTime 统计记录开始时间
     * @param collectTime         本次采集时间
     * @param shift               采集时间所在的班次
     * @return true-同一班次，false-不同班次
     */
    private boolean isSameShift(Date statisticsStartTime, Date collectTime, WorkCalendar shift) {
        if (statisticsStartTime == null || collectTime == null || shift == null
                || shift.getShiftStartTime() == null || shift.getShiftEndTime() == null) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!dateFormat.format(statisticsStartTime).equals(dateFormat.format(collectTime))) {
            return false;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String statisticsTime = timeFormat.format(statisticsStartTime);
        String shiftStart = timeFormat.format(shift.getShiftStartTime());
        String shiftEnd = timeFormat.format(shift.getShiftEndTime());
        return statisticsTime.compareTo(shiftStart) >= 0 && statisticsTime.compareTo(shiftEnd) <= 0;
    }

    /**
     * 查询采集时间所在的工作日历班次
     *
     * @param collectTime 采集时间
     * @return 所在班次，不在任何班次时间段内时返回 null
     */
    private WorkCalendar findCurrentShift(Date collectTime) {
        if (collectTime == null) {
            return null;
        }

        // 只按「年月日」定位工作日
        Calendar cal = Calendar.getInstance();
        cal.setTime(collectTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dayStart = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date nextDayStart = cal.getTime();

        List<WorkCalendar> calendars = workCalendarMapper.selectList(
                new QueryWrapper<WorkCalendar>()
                        .ge("workDate", dayStart)
                        .lt("workDate", nextDayStart)
                        .eq("status", 1)
        );

        if (calendars == null || calendars.isEmpty()) {
            // 当天没有启用的班次配置
            return null;
        }

        // 只比较时分秒部分，忽略日期
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String collectTimeStr = timeFormat.format(collectTime);

        for (WorkCalendar wc : calendars) {
            if (wc.getShiftStartTime() == null || wc.getShiftEndTime() == null) {
                continue;
            }
            String startStr = timeFormat.format(wc.getShiftStartTime());
            String endStr = timeFormat.format(wc.getShiftEndTime());

            // 班次开始时间 <= 采集时间 <= 班次结束时间
            if (collectTimeStr.compareTo(startStr) >= 0 && collectTimeStr.compareTo(endStr) <= 0) {
                return wc;
            }
        }

        return null;
    }
}