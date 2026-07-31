package com.smsc.management.config;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.scylla.ScyllaManager;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.paicbd.smsc.utils.RedisManager;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties properties;

    @Bean(name = "webMvcTaskExecutor")
    public Executor webMvcTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public RedisManager redisManager() {
        log.info("### Creating RedisManager bean");
        return Converter.paramsToRedisManager(
                new UtilsRecords.JedisConfigParams(properties.getRedisNodes(), properties.getRedisMaxTotal(),
                        properties.getRedisMaxIdle(), properties.getRedisMinIdle(),
                        properties.isRedisBlockWhenExhausted(), properties.getRedisConnectionTimeout(),
                        properties.getRedisSoTimeout(), properties.getRedisMaxAttempts(),
                        properties.getRedisUser(), properties.getRedisPassword(),
                        properties.isRedisStandaloneEnabled())
        );
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(properties.getReportBatchSize());
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Bean
    public ScyllaManager scyllaDbManager() {
        return new ScyllaManager(
                properties.getScyllaContactPoints(),
                properties.getScyllaDatacenter(),
                properties.getScyllaUser(),
                properties.getScyllaPassword(),
                false
        );
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledBroadcastExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "broadcast-worker-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };
        return Executors.newScheduledThreadPool(this.properties.getBroadcastThreadPoolSize(), threadFactory);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
