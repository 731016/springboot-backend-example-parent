package com.xiaofei.springbootinit.example.interfaceaop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Replayable {
    String value() default "";  // 接口描述
}