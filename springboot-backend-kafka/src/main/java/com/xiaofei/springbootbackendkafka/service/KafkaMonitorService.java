package com.xiaofei.springbootbackendkafka.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 查看 Kafka 分区、消费者组、offset 与 lag 等运行情况。
 */
@Service
@Slf4j
public class KafkaMonitorService {

    private final AdminClient adminClient;

    public KafkaMonitorService(AdminClient kafkaAdminClient) {
        this.adminClient = kafkaAdminClient;
    }

    /**
     * 所有 topic 及其分区数
     */
    @SuppressWarnings("deprecation")
    public List<TopicInfo> listTopics() throws ExecutionException, InterruptedException {
        Set<String> names = adminClient.listTopics().names().get();
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, TopicDescription> desc = adminClient.describeTopics(names).all().get();
        return desc.values().stream()
                .map(t -> new TopicInfo(t.name(), t.partitions().size(), t.partitions().stream()
                        .map(p -> new TopicInfo.PartitionInfo(p.partition(), p.leader() != null ? p.leader().id() : -1))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    /**
     * 所有消费者组及其状态、成员数
     */
    public List<ConsumerGroupSummary> listConsumerGroups() throws ExecutionException, InterruptedException {
        Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get();
        if (listings.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = listings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        Map<String, ConsumerGroupDescription> desc = adminClient.describeConsumerGroups(ids).all().get();
        return desc.values().stream()
                .map(g -> new ConsumerGroupSummary(
                        g.groupId(),
                        g.state().toString(),
                        g.members().size(),
                        g.members().stream()
                                .map(m -> new MemberInfo(m.consumerId(), m.host(), m.assignment().topicPartitions().size()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    /**
     * 指定消费者组的详情：各 partition 的当前 offset 与 lag（需 Kafka 2.5+ listOffsets）
     */
    public ConsumerGroupDetail getConsumerGroupDetail(String groupId) throws ExecutionException, InterruptedException {
        ConsumerGroupDescription desc = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                .all().get().get(groupId);
        if (desc == null) {
            return null;
        }
        Map<TopicPartition, OffsetAndMetadata> offsets = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
        List<PartitionOffsetInfo> partitionOffsets = new ArrayList<>();
        long totalLag = 0;

        if (!offsets.isEmpty()) {
            Map<TopicPartition, OffsetSpec> toRequest = offsets.keySet().stream()
                    .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets = adminClient.listOffsets(toRequest).all().get();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> e : offsets.entrySet()) {
                TopicPartition tp = e.getKey();
                long current = e.getValue().offset();
                ListOffsetsResult.ListOffsetsResultInfo endInfo = endOffsets.get(tp);
                long end = endInfo != null ? endInfo.offset() : current;
                long lag = Math.max(0, end - current);
                totalLag += lag;
                partitionOffsets.add(new PartitionOffsetInfo(tp.topic(), tp.partition(), current, end, lag));
            }
        }

        return new ConsumerGroupDetail(
                groupId,
                desc.state().toString(),
                desc.members().stream()
                        .map(m -> new MemberInfo(m.consumerId(), m.host(), m.assignment().topicPartitions().size()))
                        .collect(Collectors.toList()),
                partitionOffsets,
                totalLag
        );
    }

    // --- DTOs ---

    @Data
    public static class TopicInfo {
        private final String topic;
        private final int partitionCount;
        private final List<PartitionInfo> partitions;

        @Data
        public static class PartitionInfo {
            private final int partition;
            private final int leaderId;
        }
    }

    @Data
    public static class ConsumerGroupSummary {
        private final String groupId;
        private final String state;
        private final int memberCount;
        private final List<MemberInfo> members;
    }

    @Data
    public static class MemberInfo {
        private final String consumerId;
        private final String host;
        private final int assignedPartitions;
    }

    @Data
    public static class ConsumerGroupDetail {
        private final String groupId;
        private final String state;
        private final List<MemberInfo> members;
        private final List<PartitionOffsetInfo> partitionOffsets;
        private final long totalLag;
    }

    @Data
    public static class PartitionOffsetInfo {
        private final String topic;
        private final int partition;
        private final long currentOffset;
        private final long endOffset;
        private final long lag;
    }
}
