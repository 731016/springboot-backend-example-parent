package com.xiaofei.springbootbackendwebsocket;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.binarywang.spring.starter.wxjava.mp.config.WxMpAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {WxMpAutoConfiguration.class, DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
@ComponentScan(
        basePackages = {
                "com.xiaofei.springbootbackendwebsocket", // 自己
                "com.xiaofei.springbootinit"              // 把 example 模块的包也扫进来
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class WebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }

}
