package com.xiaofei.springbootinit.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * MyBatis Plus 配置
 *
 * @author
 */
@Configuration
@MapperScan({
        "com.xiaofei.springbootinit.mapper",
        "com.xiaofei.springbootinit.example.interfaceaop.mapper",
        "com.xiaofei.springbootinit.example.commonserviceExec.mapper",
        "com.xiaofei.springbootinit.example.quartz.mapper",
        "com.xiaofei.springbootinit.example.kafka.mapper",
        "com.xiaofei.springbootinit.example.redis.mapper"
})
public class MyBatisPlusConfig {

    @Resource(name = "dynamicDataSource")
    private DataSource dynamicDataSource;

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dynamicDataSource);
    }

    /**
     * 拦截器配置
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}