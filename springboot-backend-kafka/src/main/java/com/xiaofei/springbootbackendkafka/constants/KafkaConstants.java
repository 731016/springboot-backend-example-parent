package com.xiaofei.springbootbackendkafka.constants;

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
    /**
     * raw-data 消费组 ID 默认值；实际以配置 app.kafka.raw-data.group-id 为准，可覆盖以实现多点位/多组消费（见 docs/KafkaConsumerDesign.md）。
     */
    String RAW_DATA_GROUP_ID = "raw-data-process-group";

    /**
     * 数据类型：1-正常数据，2-非统计数据，3-超过上下限
     */
    Integer DATA_TYPE_NORMAL = 1;
    Integer DATA_TYPE_NON_STATISTICAL = 2;
    Integer DATA_TYPE_MORE_THAN_LIMIT = 3;
}
