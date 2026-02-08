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
@MapperScan({
        "com.xiaofei.springbootbackendelasticsearch.mapper",
        "com.xiaofei.springbootinit.mapper",
        "com.xiaofei.springbootinit.example.commonserviceExec.mapper"
})
public class ElasticsearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchApplication.class, args);
    }

}
