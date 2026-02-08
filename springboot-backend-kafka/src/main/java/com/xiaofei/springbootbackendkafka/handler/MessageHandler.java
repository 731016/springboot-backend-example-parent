package com.xiaofei.springbootbackendkafka.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaofei.springbootbackendkafka.constants.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Component(KafkaConstants.TOPIC_TEST)
@Slf4j
public class MessageHandler {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

//    @KafkaListener(topics = KafkaConstants.TOPIC_TEST, containerFactory = "ackContainerFactory")
    public void handleMessage(ConsumerRecords<String, String> records, Acknowledgment acknowledgment) {
        Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
        while (iterator.hasNext()) {
            ConsumerRecord<String, String> record = iterator.next();
            String message = record.value();
            // 获取当前重试次数
            int retryCount = getRetryCount(record);

            try {
                log.info("收到消息: topic = {}, partition = {}, offset = {}, retryCount = {}, message = {}",
                        record.topic(), record.partition(), record.offset(), retryCount, message);

                // 处理消息的业务逻辑
                processMessage(message);

                // 处理成功，确认消息
                acknowledgment.acknowledge();
                log.info("消息处理成功，已提交offset");

            } catch (Exception e) {
                log.error("消息处理失败: topic = {}, partition = {}, offset = {}, retryCount = {}",
                        record.topic(), record.partition(), record.offset(), retryCount, e);
                handleFailedMessage(record, retryCount, e);
                // 确认原消息，因为消息已经被转发到重试队列或死信队列
                acknowledgment.acknowledge();
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
        Header retryHeader = headers.lastHeader(KafkaConstants.HEADER_RETRY_COUNT);
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
        if (retryCount < KafkaConstants.MAX_RETRY_COUNT) {
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
                KafkaConstants.TOPIC_TEST,  // 仍然发送到原主题
                record.partition(),
                record.key(),
                record.value()
        );

        // 添加重试次数到header
        retryRecord.headers().add(
                KafkaConstants.HEADER_RETRY_COUNT,
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
            kafkaTemplate.send(KafkaConstants.TOPIC_DLQ, dlqMessageJson).get(10, TimeUnit.SECONDS);
            log.info("消息已发送到死信队列");
        } catch (Exception ex) {
            log.error("发送到死信队列失败", ex);
        }
    }

    private void processMessage(String message) throws InterruptedException {
        // 业务处理逻辑
//        Thread.sleep(1000);
        int i = 1/0;
    }
}
