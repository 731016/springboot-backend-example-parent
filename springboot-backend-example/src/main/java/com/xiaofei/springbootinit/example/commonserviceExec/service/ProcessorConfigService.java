package com.xiaofei.springbootinit.example.commonserviceExec.service;


import com.xiaofei.springbootinit.example.commonserviceExec.model.entity.ServiceProcessorConfig;

import java.util.List;

/**
 * @author tuaofei
 * @description 处理器配置服务
 * @date 2024/12/24
 */
public interface ProcessorConfigService {

    List<ServiceProcessorConfig> getConfigs(String serviceName, String methodName);
}
