package com.xiaofei.springbootinit.example.commonserviceExec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaofei.springbootinit.example.commonserviceExec.mapper.ServiceProcessorConfigMapper;
import com.xiaofei.springbootinit.example.commonserviceExec.model.entity.ServiceProcessorConfig;
import com.xiaofei.springbootinit.example.commonserviceExec.service.ProcessorConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Service
public class ProcessorConfigServiceImpl implements ProcessorConfigService {

    @Resource
    private ServiceProcessorConfigMapper configMapper;

    @Override
    public List<ServiceProcessorConfig> getConfigs(String serviceName, String methodName) {
        return configMapper.selectList(new QueryWrapper<ServiceProcessorConfig>()
                .eq("serviceName", serviceName)
                .eq("methodName", methodName)
                .eq("status", true));
    }
}
