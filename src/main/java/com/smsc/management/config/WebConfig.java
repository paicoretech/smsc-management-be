package com.smsc.management.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final Executor webMvcTaskExecutor;

    public WebConfig(@Qualifier("webMvcTaskExecutor") Executor webMvcTaskExecutor) {
        this.webMvcTaskExecutor = webMvcTaskExecutor;
    }
	
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor((ThreadPoolTaskExecutor) webMvcTaskExecutor);
    }
}