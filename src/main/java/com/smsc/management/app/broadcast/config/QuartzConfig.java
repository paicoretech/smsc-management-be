package com.smsc.management.app.broadcast.config;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {
    private final ApplicationContext applicationContext;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factoryBean) {
        return factoryBean.getScheduler();
    }
}
