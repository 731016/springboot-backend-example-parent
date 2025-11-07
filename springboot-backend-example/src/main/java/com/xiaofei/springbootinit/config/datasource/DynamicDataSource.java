package com.xiaofei.springbootinit.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author tuaofei
 * @description 动态数据源实现
 * @date 2024/12/24
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * 构造方法，初始化动态数据源
     *
     * @param defaultTargetDataSource 默认数据源
     * @param targetDataSources       目标数据源集合
     */
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        // 设置默认数据源
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        // 设置数据源集合
        super.setTargetDataSources(targetDataSources);
        // 执行afterPropertiesSet方法，完成属性初始化
        super.afterPropertiesSet();
    }

    /**
     * 获取当前数据源的key
     *
     * @return 数据源key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        // 从ThreadLocal中获取当前数据源的key
        return DataSourceContextHolder.getDataSource();
    }
}