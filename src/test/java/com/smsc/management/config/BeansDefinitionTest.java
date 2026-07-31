package com.smsc.management.config;

import com.smsc.management.integration.BaseIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.paicbd.smsc.utils.RedisManager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class BeansDefinitionTest extends BaseIntegrationTest {
    @Autowired
    BeansDefinition beansDefinition;

    @Autowired
    ScheduledExecutorService scheduledBroadcastExecutor;

    @Test
    void webMvcTaskExecutor() {
        assertNotNull(beansDefinition.webMvcTaskExecutor());
    }

    @Test
    void redisManager() {
        RedisManager redisManager = beansDefinition.redisManager();
        assertNotNull(redisManager);
    }

    @Test
    void scheduledBroadcastExecutorTest() throws ExecutionException, InterruptedException, TimeoutException {
        Future<String> future = scheduledBroadcastExecutor.submit(() -> {
            task();
            return Thread.currentThread().getName();
        });

        String threadName = future.get(1, TimeUnit.SECONDS);
        assertThat(threadName).startsWith("broadcast-worker-");

        scheduledBroadcastExecutor.shutdown();
    }

    private void task() throws InterruptedException {
        log.info("starting task...");
        Thread.sleep(100);
    }
}