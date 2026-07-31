package com.smsc.management.integration;

import com.paicbd.smsc.scylla.ScyllaManager;
import com.smsc.management.app.provider.security.BearerTokenBlacklistService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("smsc")
                    .withUsername("smsc")
                    .withPassword("smsc")
                    .withInitScript("init.sql")
                    .withCommand("postgres", "-c", "max_connections=200");

    /*private static final CassandraContainer<?> scylla =
            new CassandraContainer<>(DockerImageName.parse("scylladb/scylla:5.2")
                    .asCompatibleSubstituteFor("cassandra"))
                    .withExposedPorts(9042)
                    .waitingFor(new LogMessageWaitStrategy()
                            .withRegEx(".*Starting listening for CQL clients.*\\s"));*/

    public static DockerComposeContainer<?> redisCluster =
            new DockerComposeContainer<>(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService("redis-node-0", 8910,
                            new LogMessageWaitStrategy().withRegEx(".*Ready to accept connections.*\\s"))
                    .withExposedService("redis-node-1", 8911,
                            new LogMessageWaitStrategy().withRegEx(".*Ready to accept connections.*\\s"))
                    .withExposedService("redis-node-2", 8912,
                            new LogMessageWaitStrategy().withRegEx(".*Ready to accept connections.*\\s"))
                    .withStartupTimeout(Duration.ofMinutes(5));

    public static ConfluentKafkaContainer kafka =
            new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0")
                    .withExposedPorts(9092, 2181);

    static {
        LocalDateTime initTime = LocalDateTime.now();
        log.info("Starting PostgreSQL container...");
        postgres.start();

        /*log.info("PostgreSQL container started successfully. Starting ScyllaDB container...");
        scylla.start();*/

        log.info("ScyllaDB container started successfully. Starting Redis cluster...");
        redisCluster.start();

        log.info("Redis cluster was started successfully. Starting Kafka container...");
        kafka.start();
        log.info("Kafka container started successfully.");


        LocalDateTime endTime = LocalDateTime.now();
        log.info("####### All containers are up and running. Initialization took {} seconds.",
                Duration.between(initTime, endTime).getSeconds());
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers); //32844
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockBean
    private ScyllaManager scyllaDbManager;

    @MockBean
    private BearerTokenBlacklistService bearerTokenBlacklistService;
}
