package com.xiaofei.springbootinit.example.kafka.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Data
@Accessors(chain = true)
public class KafKaMsg {

    @NotBlank(message = "主题不能为空")
    private String topic;

    @NotBlank(message = "消息不能为空")
    private String msg;
}
