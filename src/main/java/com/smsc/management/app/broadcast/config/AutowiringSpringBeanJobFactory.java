package com.smsc.management.app.broadcast.config;

import com.paicbd.smsc.utils.Generated;
import lombok.NonNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Generated
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        this.beanFactory = context.getAutowireCapableBeanFactory();
    }

    @NonNull
    @Override
    protected Object createJobInstance(@NonNull final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        this.beanFactory.autowireBean(job);
        return job;
    }
}

