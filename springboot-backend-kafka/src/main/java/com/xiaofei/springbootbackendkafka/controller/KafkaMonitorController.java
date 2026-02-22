package com.xiaofei.springbootbackendkafka.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendkafka.service.KafkaMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 查看 Kafka 分区、消费者、生产者相关运行情况（topic 列表、分区数、消费者组、offset、lag）。
 */
@RestController
@RequestMapping("/kafka/admin")
@Slf4j
@Api(tags = "Kafka 运行情况")
public class KafkaMonitorController {

    private final KafkaMonitorService kafkaMonitorService;

    public KafkaMonitorController(KafkaMonitorService kafkaMonitorService) {
        this.kafkaMonitorService = kafkaMonitorService;
    }

    @GetMapping("/topics")
    @ApiOperation(value = "查看所有 Topic 及分区")
    public BaseResponse<List<KafkaMonitorService.TopicInfo>> listTopics() {
        try {
            return ResultUtils.success(kafkaMonitorService.listTopics());
        } catch (ExecutionException | InterruptedException e) {
            log.warn("listTopics failed", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取 topic 列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/consumer-groups")
    @ApiOperation(value = "查看所有消费者组及状态")
    public BaseResponse<List<KafkaMonitorService.ConsumerGroupSummary>> listConsumerGroups() {
        try {
            return ResultUtils.success(kafkaMonitorService.listConsumerGroups());
        } catch (ExecutionException | InterruptedException e) {
            log.warn("listConsumerGroups failed", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取消费者组列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/consumer-groups/detail")
    @ApiOperation(value = "查看指定消费者组详情（含各分区 offset 与 lag）")
    public BaseResponse<KafkaMonitorService.ConsumerGroupDetail> getConsumerGroupDetail(
            @RequestParam String groupId) {
        try {
            KafkaMonitorService.ConsumerGroupDetail detail = kafkaMonitorService.getConsumerGroupDetail(groupId);
            if (detail == null) {
                return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "消费者组不存在: " + groupId);
            }
            return ResultUtils.success(detail);
        } catch (ExecutionException | InterruptedException e) {
            log.warn("getConsumerGroupDetail failed, groupId={}", groupId, e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取消费者组详情失败: " + e.getMessage());
        }
    }
}
