package com.smsc.management.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class AppProperties {
    @Value("${cluster.nodes}")
    private List<String> redisNodes;

    @Value("${cluster.threadPool.maxTotal}")
    private int redisMaxTotal;

    @Value("${cluster.threadPool.maxIdle}")
    private int redisMaxIdle;

    @Value("${cluster.threadPool.minIdle}")
    private int redisMinIdle;

    @Value("${cluster.threadPool.blockWhenExhausted}")
    private boolean redisBlockWhenExhausted;

    @Value("${cluster.connection.timeout:0}")
    private int redisConnectionTimeout;

    @Value("${cluster.so.timeout:0}")
    private int redisSoTimeout;

    @Value("${cluster.maxAttempts:0}")
    private int redisMaxAttempts;

    @Value("${cluster.connection.password:}")
    private String redisPassword;

    @Value("${cluster.connection.user:}")
    private String redisUser;

    @Value("${cluster.standalone.enabled:false}")
    private boolean redisStandaloneEnabled;

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${server.balancePeriod.store}")
    private int balancePeriodToStore;

    @Value("${websocket.header.name}")
    private String websocketHeaderName;

    @Value("${websocket.header.value}")
    private String websocketHeaderValue;

    @Value("${server.apiKeyHeader.name}")
    private String apiKeyHeaderName;

    @Value("${server.apiKey.value}")
    private String apiKeyHeaderValue;

    @Value("${app.root.user}")
    private String rootUser;

    @Value("${app.root.password}")
    private String rootPassword;

    @Value("${loadCsv.batchSize}")
    private int loadCsvBatchSize;

    @Value("${processing.batchSize}")
    private int processingBatchSize;

    @Value("${report.batchSize}")
    private int reportBatchSize;

    @Value("${file.download.broadcast.dir}")
    private String reportBroadcastDir;

    @Value("${file.upload.broadcast.dir}")
    private String uploadBroadcastDir;

    @Value("${file.upload.dir}")
    private String uploadFileDir;

    @Value("${broadcast.threadpool.size:10}")
    private int broadcastThreadPoolSize;

    @Value("${broadcast.valid.number.regex:^\\+?\\d+$}")
    private String broadcastValidNumberRegex;

    @Value("${broadcast.default-max-execution.seconds:3600}")
    private int broadcastDefaultMaxExecutionSeconds;

    @Value("${app.failed.login.attempts}")
    private int failedLoginAttempts;

    @Value("${app.clean.failed.login.attempts.hours}")
    private int failedLoginAttemptHours;

    @Value("${scylla.contact.points}")
    private String scyllaContactPoints;

    @Value("${scylla.datacenter}")
    private String scyllaDatacenter;

    @Value("${scylla.user}")
    private String scyllaUser;

    @Value("${scylla.password}")
    private String scyllaPassword;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.listener.concurrency}")
    private int kafkaListenerConcurrency;

    @Value("${kafka.producer.linger-ms:60000}")
    private int kafkaProducerLingerMs;

    @Value("${kafka.producer.batch-size:32768}")
    private int kafkaProducerBatchSize;

    @Value("${kafka.consumer.max-poll-records:1000}")
    private int kafkaConsumerMaxPollRecords;

    @Value("${dnd.msisdn.regex:^[0-9]+$}")
    private String msisdnRegex;

    @Value("${jwt.service.provider.secret}")
    private String serviceProviderJwtSecret;

    @Value("${jwt.service.provider.iss}")
    private String serviceProviderJwtIss;

    @Value("${jwt.service.provider.clock-skew-seconds}")
    private long serviceProviderJwtClockSkewSeconds;

    @Value("${spring.kafka.consumer.reconnect.backoff.ms:1000}")
    private long kafkaConsumerReconnectBackoffMs;

    @Value("${spring.kafka.consumer.reconnect.backoff.max.ms:10000}")
    private long kafkaConsumerReconnectBackoffMaxMs;

    @Value("${spring.kafka.consumer.session.timeout.ms:30000}")
    private int kafkaConsumerSessionTimeoutMs;

    @Value("${spring.kafka.consumer.heartbeat.interval.ms:10000}")
    private int kafkaConsumerHeartbeatIntervalMs;

    @Value("${spring.kafka.producer.reconnect.backoff.ms:1000}")
    private long kafkaProducerReconnectBackoffMs;

    @Value("${spring.kafka.producer.reconnect.backoff.max.ms:10000}")
    private long kafkaProducerReconnectBackoffMaxMs;
}
