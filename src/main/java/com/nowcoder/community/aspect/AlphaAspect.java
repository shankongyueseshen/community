package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //第一个*代表方法的返回值，所有的返回值
    //包名，service下的所有组件里面的所有方法
    //(..)代表所有的参数
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }
    //在连接点开始记日志，连接点就是上面的切点
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }
    //在连接点后记日志，连接点就是上面的切点
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }
    //在返回值之后记日志
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterreturning");
    }
    //在抛异常时植入代码
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }
    //在前后都植入逻辑
    @Around("pointcut()")
    public Object around (ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("aroud before");
        Object obj=joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
