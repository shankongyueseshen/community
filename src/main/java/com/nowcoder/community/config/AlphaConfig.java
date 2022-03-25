package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
//表示是一个配置类的注解
    @Configuration
    public class AlphaConfig {
        //装配到bean
        @Bean
        //方法名就是bean的名字，
        public SimpleDateFormat simpleDateFormat() {
            //这个方法返回的对象将被装配到容器里
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

    }

