package com.xiaofei.springbootinit.example.kafka.constants;

/**
 * <p>
 * kafka 常量池
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-01-07 14:52
 */
public interface KafkaConstants {
    /**
     * 默认分区大小
     */
    Integer DEFAULT_PARTITION_NUM = 3;

    /**
     * Topic 名称
     */
    String TOPIC_TEST = "test";

    /**
     * 死信队列主题
     */
    String TOPIC_DLQ = "test-dlq";

    /**
     * 重试次数的header key
     */
    String HEADER_RETRY_COUNT = "retry-count";

    /**
     * 最大重试次数
     */
    int MAX_RETRY_COUNT = 3;

//    以上为测试

    /**
     * 数据采集服务 topic
     */
    String RAW_DATA_TOPIC = "raw-data";
    String RAW_DATA_TOPIC_DLQ = "raw-data-dlq";
    String RAW_DATA_HEADER_RETRY_COUNT = "raw-data-retry-count";
    int RAW_DATA_MAX_RETRY_COUNT = 3;
    String RAW_DATA_GROUP_ID = "raw-data-process-group";
}
