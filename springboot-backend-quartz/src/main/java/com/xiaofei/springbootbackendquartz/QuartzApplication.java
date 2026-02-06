package com.xiaofei.springbootbackendquartz;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.binarywang.spring.starter.wxjava.mp.config.WxMpAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {WxMpAutoConfiguration.class, DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
@ComponentScan(
        basePackages = {
                "com.xiaofei.springbootbackendquartz", // 自己
                "com.xiaofei.springbootinit"              // 把 example 模块的包也扫进来
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan({
        "com.xiaofei.springbootbackendquartz.mapper",
        "com.xiaofei.springbootinit.mapper",
        "com.xiaofei.springbootinit.example.commonserviceExec.mapper",
        "com.xiaofei.springbootinit.example.kafka.mapper"
})
public class QuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzApplication.class, args);
    }

}
