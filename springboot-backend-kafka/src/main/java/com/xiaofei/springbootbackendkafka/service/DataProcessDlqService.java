package com.xiaofei.springbootbackendkafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaofei.springbootbackendkafka.constants.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @author tuaofei
 * @description 数据处理服务（Kafka消费者）-死信队列
 * @date 2024/12/27
 */
@Component(KafkaConstants.RAW_DATA_TOPIC_DLQ)
@Slf4j
public class DataProcessDlqService {

    @KafkaListener(topics = KafkaConstants.RAW_DATA_TOPIC_DLQ, groupId = KafkaConstants.RAW_DATA_GROUP_ID, containerFactory = "ackContainerFactory")
    public void processData(ConsumerRecords<String, String> records, Acknowledgment acknowledgment) {
        Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
        while (iterator.hasNext()) {
            ConsumerRecord<String, String> record = iterator.next();
            try {
                String message = record.value();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> dlqMessage = mapper.readValue(message, Map.class);

                // 打印完整的错误信息
                log.error("\n死信队列消息详情：\n" +
                                "原始主题：{}\n" +
                                "原始分区：{}\n" +
                                "原始偏移量：{}\n" +
                                "原始消息内容：{}\n" +
                                "错误信息：{}\n" +
                                "错误堆栈：\n{}\n" +
                                "失败时间：{}",
                        dlqMessage.get("originalTopic"),
                        dlqMessage.get("originalPartition"),
                        dlqMessage.get("originalOffset"),
                        dlqMessage.get("originalMessage"),
                        dlqMessage.get("error"),
                        dlqMessage.get("stackTrace"),
                        new Date((Long) dlqMessage.get("timestamp"))
                );

                // 可以将错误信息保存到数据库
                saveDLQMessage(dlqMessage);

                // 发送告警通知
                sendAlert(dlqMessage);

                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.error("处理死信队列消息失败", e);
                // 死信队列的消息处理失败，这里选择不提交offset
            }

        }
    }

    private void saveDLQMessage(Map<String, Object> dlqMessage) {
        // 示例：打印 SQL 语句
        log.info("执行SQL: INSERT INTO kafka_dlq_messages (" +
                        "original_topic, original_partition, original_offset, " +
                        "original_message, error_message, error_stack_trace, failed_time" +
                        ") VALUES ('{}', {}, {}, '{}', '{}', '{}', '{}')",
                dlqMessage.get("originalTopic"),
                dlqMessage.get("originalPartition"),
                dlqMessage.get("originalOffset"),
                dlqMessage.get("originalMessage"),
                dlqMessage.get("error"),
                dlqMessage.get("stackTrace"),
                new Date((Long) dlqMessage.get("timestamp"))
        );
    }

    private void sendAlert(Map<String, Object> dlqMessage) {
        String alertMessage = String.format(
                "Kafka消息处理失败告警\n" +
                        "主题：%s\n" +
                        "分区：%s\n" +
                        "偏移量：%s\n" +
                        "消息内容：%s\n" +
                        "错误原因：%s\n" +
                        "失败时间：%s",
                dlqMessage.get("originalTopic"),
                dlqMessage.get("originalPartition"),
                dlqMessage.get("originalOffset"),
                dlqMessage.get("originalMessage"),
                dlqMessage.get("error"),
                new Date((Long) dlqMessage.get("timestamp"))
        );

        log.error("发送告警消息：\n{}", alertMessage);
        // 这里可以调用告警接口，如：
        // - 发送邮件
        // - 发送钉钉消息
        // - 发送企业微信消息
        // - 发送短信
    }
}
