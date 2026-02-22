package com.xiaofei.springbootbackendkafka.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaofei.springbootbackendkafka.constants.KafkaConstants;
import com.xiaofei.springbootbackendkafka.mapper.DataDetailMapper;
import com.xiaofei.springbootbackendkafka.mapper.DataStatisticsMapper;
import com.xiaofei.springbootbackendkafka.model.dto.CollectedData;
import com.xiaofei.springbootbackendkafka.model.entity.DataDetail;
import com.xiaofei.springbootbackendkafka.model.entity.DataStatistics;
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
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private SimpMessagingTemplate wsTemplate;

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

        // 获取当前进行中的统计记录
        DataStatistics statistics = dataStatisticsMapper.selectOne(
                new QueryWrapper<DataStatistics>()
                        .eq("pointCode", data.getPointCode())
                        .eq("status", 1)
        );

        if (statistics != null) {
            detail.setStatisticsId(statistics.getId());
            dataDetailMapper.insert(detail);

            // 更新统计数据
            updateStatistics(statistics.getId());
        } else {
            DataStatistics newDataStatistics = new DataStatistics();
            newDataStatistics.setPointCode(detail.getPointCode());
            newDataStatistics.setStartTime(detail.getCollectTime());
            newDataStatistics.setMaximumValue(detail.getValue());
            newDataStatistics.setMinimumValue(detail.getValue());
            newDataStatistics.setAverageValue(detail.getValue());
            newDataStatistics.setCreateTime(detail.getCollectTime());
            newDataStatistics.setUpdateTime(detail.getCollectTime());
            dataStatisticsMapper.insert(newDataStatistics);

            detail.setStatisticsId(newDataStatistics.getId());
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

    private void updateStatistics(Long statisticsId) {
        // 获取该统计ID下的所有明细数据
        List<DataDetail> details = dataDetailMapper.selectList(
                new QueryWrapper<DataDetail>()
                        .eq("statisticsId", statisticsId)
        );

        if (!details.isEmpty()) {
            // 计算统计值
            BigDecimal maxValue = details.stream()
                    .map(DataDetail::getValue)
                    .max(BigDecimal::compareTo)
                    .orElse(null);

            BigDecimal minValue = details.stream()
                    .map(DataDetail::getValue)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            BigDecimal avgValue = details.stream()
                    .map(DataDetail::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(details.size()), 2, RoundingMode.HALF_UP);

            // 更新统计记录
            DataStatistics statistics = new DataStatistics();
            statistics.setId(statisticsId);
            statistics.setMaximumValue(maxValue);
            statistics.setMinimumValue(minValue);
            statistics.setAverageValue(avgValue);

            dataStatisticsMapper.updateById(statistics);
        }
    }
}