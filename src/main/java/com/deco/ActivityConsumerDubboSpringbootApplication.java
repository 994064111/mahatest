package com.deco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;



@ImportResource(locations = {"classpath:dubbo/consumer.xml"})
@SpringBootApplication
@ComponentScan("com.*")
@EnableHystrix
@EntityScan(basePackages = "com.deco.entity")
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ServletComponentScan
public class ActivityConsumerDubboSpringbootApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(ActivityConsumerDubboSpringbootApplication.class, args);

		System.out.println("======================activity消费者启动成功=======================");

	}
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder){
		System.out.println("======================activity消费者启动成功=======================");
		return applicationBuilder.sources(ActivityConsumerDubboSpringbootApplication.class);
	}

}