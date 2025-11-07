package com.xiaofei.springbootinit.example.commonserviceExec.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Data
public class CommonServiceRequest<T> {
    /**
     * 服务名称 (例如: "userService")
     */
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    /**
     * 方法名称
     */
    @NotBlank(message = "方法名称不能为空")
    private String methodName;

    /**
     * 方法参数
     */
    private T requestData;
}