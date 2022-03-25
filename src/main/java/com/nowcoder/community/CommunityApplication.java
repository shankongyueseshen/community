package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init(){
		//解决netty启动冲突的问题
		// see netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	//自动创建容器，自动扫描某些包下的容器，启动tomcat ,
	//扫描配置类所在的包以及子包下的类，需要有注解才能扫描
	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
