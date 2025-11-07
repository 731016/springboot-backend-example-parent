package com.xiaofei.springbootinit.example.commonserviceExec.utils;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.commonserviceExec.model.dto.CommonServiceRequest;
import com.xiaofei.springbootinit.example.commonserviceExec.model.entity.ServiceProcessorConfig;
import com.xiaofei.springbootinit.example.commonserviceExec.service.ProcessorConfigService;
import com.xiaofei.springbootinit.example.commonserviceExec.service.ServiceProcessor;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Component
@Slf4j
public class ServiceUtils implements ApplicationContextAware {


    @Autowired
    private static ThreadPoolTaskExecutor taskExecutor;


    @Autowired
    private static TransactionTemplate transactionTemplate;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 通用服务方法执行
     *
     * @param request 请求参数
     * @return 执行结果
     */
    @SuppressWarnings("unchecked")
    public static <T> BaseResponse<T> commonExec(CommonServiceRequest<?> request) {
        return transactionTemplate.execute(status -> {
            try {
                String serviceName = request.getServiceName();
                String methodName = request.getMethodName();

                // 获取处理器配置
                ProcessorConfigService configService = applicationContext.getBean(ProcessorConfigService.class);
                List<ServiceProcessorConfig> configs = configService.getConfigs(serviceName, methodName);

                // 1. 获取服务实例
                Object service = applicationContext.getBean(serviceName);

                // 2. 获取方法
                Method method = service.getClass().getMethod(methodName,
                        CommonServiceRequest.class, BaseResponse.class);

                // 3. 创建响应对象
                BaseResponse<T> response = new BaseResponse<>(ErrorCode.SUCCESS);

                // 执行前置处理器
                executeProcessors(configs, "before", serviceName, methodName, request, response);

                // 4. 执行方法
                Object result = method.invoke(service, request, response);

                // 执行后置处理器
                executeProcessors(configs, "after", serviceName, methodName, request, response);

                return ResultUtils.success((T) result);
            } catch (Exception e) {
                log.error("Service execution error", e);
                status.setRollbackOnly();
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        });
    }

    public static void executeProcessors(List<ServiceProcessorConfig> configs, String timing,
                                         String serviceName, String methodName,
                                         CommonServiceRequest<?> request,
                                         BaseResponse<?> response) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (ServiceProcessorConfig config : configs) {
            if (!config.getTiming().equals(timing)) {
                continue;
            }

            ServiceProcessor processor = applicationContext.getBean(config.getProcessorName(), ServiceProcessor.class);

            if (config.getIsAsync()) {
                // 异步执行
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 在新的事务中执行处理器
                        transactionTemplate.execute(processorStatus -> {
                            try {
                                processor.process(serviceName, methodName, request, response);
                                return null;
                            } catch (Exception e) {
                                if (!config.getIgnoreError()) {
                                    // 如果不忽略异常，设置事务回滚并抛出异常
                                    processorStatus.setRollbackOnly();
                                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
                                }
                                // 如果忽略异常，只记录日志
                                log.error("Async processor execution error", e);
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        if (!config.getIgnoreError()) {
                            // 外层异常处理，如果不忽略异常则抛出
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
                        }
                        log.error("Async processor execution error", e);
                    }
                }, taskExecutor);

                futures.add(future);
            } else {
                // 同步执行
                try {
                    processor.process(serviceName, methodName, request, response);
                } catch (Exception e) {
                    if (!config.getIgnoreError()) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
                    }
                    log.error("Sync processor execution error", e);
                }
            }
        }

        // 等待所有异步任务完成
        if (!futures.isEmpty()) {
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
    }

}
