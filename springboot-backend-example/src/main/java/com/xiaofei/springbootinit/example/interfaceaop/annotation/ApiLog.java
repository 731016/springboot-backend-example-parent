package com.xiaofei.springbootinit.example.interfaceaop.annotation;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
import java.lang.annotation.*;

/**
 * 接口日志注解
 */
@Target(ElementType.METHOD)  // 作用在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时可见
@Documented  // 生成文档
public @interface ApiLog {
    /**
     * 接口描述
     */
    String value() default "";

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResponse() default true;
}
