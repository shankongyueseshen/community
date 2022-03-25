package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//这个注解表示是否需要登陆才可以访问
//注解作用在方法上
@Target(ElementType.METHOD)
//声明注解有效时机时长，程序运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
