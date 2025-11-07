package com.xiaofei.springbootinit.aop;

import com.xiaofei.springbootinit.annotation.DataSource;
import com.xiaofei.springbootinit.config.datasource.DataSourceContextHolder;
import com.xiaofei.springbootinit.config.datasource.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author tuaofei
 * @description 数据源切换切面
 * @date 2024/12/24
 */
@Slf4j
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(com.xiaofei.springbootinit.annotation.DataSource)")
    public void apiLogPointcut() {
    }

    @Around("apiLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        DataSource dataSource = signature.getMethod().getAnnotation(DataSource.class);
        if (dataSource != null) {
            DataSourceType dataSourceType = dataSource.value();
            log.info("切换数据源到: {}", dataSourceType);
            DataSourceContextHolder.setDataSource(dataSourceType);
        }

        try {
            return point.proceed();
        } finally {
            log.info("清除数据源配置");
            DataSourceContextHolder.clearDataSource();
        }
    }
}
