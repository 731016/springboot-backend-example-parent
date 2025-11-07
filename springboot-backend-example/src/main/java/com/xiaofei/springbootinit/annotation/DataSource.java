package com.xiaofei.springbootinit.annotation;

import com.xiaofei.springbootinit.config.datasource.DataSourceType;

import java.lang.annotation.*;

/**
 * @author tuaofei
 * @description 数据源切换注解
 * @date 2024/12/24
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    DataSourceType value() default DataSourceType.MASTER;
}
