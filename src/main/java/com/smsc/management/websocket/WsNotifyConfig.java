package com.smsc.management.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
public class WsNotifyConfig {
    @Bean
    public Sinks.Many<String> sink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    @Bean
    public Flux<String> notifyRealtime(Sinks.Many<String> sink) {
        return sink.asFlux();
    }
}
