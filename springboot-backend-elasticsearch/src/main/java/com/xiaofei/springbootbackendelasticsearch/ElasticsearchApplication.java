package com.xiaofei.springbootbackendelasticsearch;

import com.binarywang.spring.starter.wxjava.mp.config.WxMpAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {WxMpAutoConfiguration.class})
@ComponentScan(
        basePackages = {
                "com.xiaofei.springbootbackendelasticsearch", // 自己
                "com.xiaofei.springbootinit"              // 把 example 模块的包也扫进来
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableScheduling
//@MapperScan({
//        "com.xiaofei.springbootbackendelasticsearch.mapper"
//})
public class ElasticsearchApplication {

    public static void main(String[] args) {
        //todo
        // 1.实现一个用户同步到es,和用户信息查询
        // 2.日志信息同步到es，和日志信息查询，日志高亮查询，根据日期动态创建索引
        SpringApplication.run(ElasticsearchApplication.class, args);
    }

}
