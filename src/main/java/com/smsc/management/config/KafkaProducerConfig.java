package com.smsc.management.config;

import com.smsc.management.utils.AppProperties;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Generated
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {
    private final AppProperties appProperties;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, appProperties.getKafkaBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, appProperties.getKafkaProducerLingerMs()); // timeout to producer messages
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, appProperties.getKafkaProducerBatchSize()); // 32 KB
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, appProperties.getKafkaProducerReconnectBackoffMs());
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, appProperties.getKafkaProducerReconnectBackoffMaxMs());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, appProperties.getKafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "smsc");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, appProperties.getKafkaConsumerMaxPollRecords());
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, (1024 * 1024 * 1024)); // 1 GB
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 5000); // 5 seconds
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, appProperties.getKafkaConsumerReconnectBackoffMs());
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, appProperties.getKafkaConsumerReconnectBackoffMaxMs());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, appProperties.getKafkaConsumerSessionTimeoutMs());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, appProperties.getKafkaConsumerHeartbeatIntervalMs());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(appProperties.getKafkaListenerConcurrency());
        factory.getContainerProperties().setShutdownTimeout(10000);
        factory.getContainerProperties().setPollTimeout(1000);
        return factory;
    }
}
