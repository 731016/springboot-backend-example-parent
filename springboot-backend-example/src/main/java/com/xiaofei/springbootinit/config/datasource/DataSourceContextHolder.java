package com.xiaofei.springbootinit.config.datasource;

/**
 * @author tuaofei
 * @description 数据源上下文
 * @date 2024/12/24
 */
public class DataSourceContextHolder {
    /**
     * 使用ThreadLocal存储当前线程的数据源key
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源
     *
     * @param dataSourceType 数据源类型
     */
    public static void setDataSource(DataSourceType dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType.name());
    }

    /**
     * 获取数据源
     *
     * @return 数据源key
     */
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除数据源
     */
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
}
