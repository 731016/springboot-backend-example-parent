package com.xiaofei.springbootbackendkafka;

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
                "com.xiaofei.springbootbackendkafka", // 自己
                "com.xiaofei.springbootinit",              // 把 example 模块的包也扫进来
                "com.xiaofei.springbootbackendwebsocket"   // WebSocket 模块，用于实时数据推送
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan({
        "com.xiaofei.springbootbackendkafka.mapper",
        "com.xiaofei.springbootinit.mapper"
})
public class KafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }

}
