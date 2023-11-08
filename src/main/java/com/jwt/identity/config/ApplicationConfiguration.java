package com.jwt.identity.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

//Customer Task executor for async long running data processing
//https://www.javadevjournal.com/spring-boot/spring-async-annotation/
//https://github.com/javadevjournal/javadevjournal/tree/master/Spring-Boot/spring-async
@Configuration
@EnableAsync
public class ApplicationConfiguration {

	@Bean(name = "threadPoolTaskExecutor")
	public Executor executor1() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("CustomExecutor1::");
		executor.initialize();
		return executor;
	}
	/*
	 * @Bean(name = "threadPoolTaskExecutor2") public Executor executor2() {
	 * ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	 * executor.setCorePoolSize(4); executor.setMaxPoolSize(4);
	 * executor.setQueueCapacity(50);
	 * executor.setThreadNamePrefix("CustomExecutor2::"); executor.initialize();
	 * return executor; }
	 */

}
