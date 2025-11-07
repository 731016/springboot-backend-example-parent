package com.xiaofei.springbootinit.example.kafka.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.kafka.model.dto.KafKaMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@RestController
@Slf4j
public class KafKaController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    /**
     * 消息发送
     *
     * @param kafKaMsg
     * @return
     */
    @PostMapping("/sendMsg")
    public BaseResponse<String> sendMsg(@Valid KafKaMsg kafKaMsg) {
        String topic = kafKaMsg.getTopic();
        String message = kafKaMsg.getMsg();
        ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(topic, message);
        StringBuilder resultMsg = new StringBuilder();
        //异步回调方式
        send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                // 消息发送失败
                log.error("消息发送失败：topic = {}, message = {}", topic, message, ex);
                resultMsg.append("消息发送失败：topic = ").append(topic).append(", message = ").append(message).append("异常信息: ").append(ex).toString();
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                // 消息发送成功
                log.info("消息发送成功：topic = {}, message = {}", topic, message);
                resultMsg.append("消息发送成功：topic = ").append(topic).append(", message = ").append(message);
                // 可以获取更多发送结果信息
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("partition = {}, offset = {}", metadata.partition(), metadata.offset());
            }
        });
        if (StringUtils.isNotBlank(resultMsg) && resultMsg.toString().contains("消息发送失败")) {
            ResultUtils.error(ErrorCode.SYSTEM_ERROR, resultMsg.toString());
        }
        return ResultUtils.success(resultMsg.toString());
    }

}
