package com.smsc.management.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {
    WebConfig webConfig;
    @MockBean
    Executor webMvcTaskExecutor;

    @BeforeEach
    void setUp() {
        this.webConfig = new WebConfig(webMvcTaskExecutor);
    }

    @Test
    void addCorsMappings() {
        CorsRegistry registry = new CorsRegistry();
        assertDoesNotThrow(() -> this.webConfig.addCorsMappings(registry));
    }

    @Test
    void configureAsyncSupport() {
        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
        assertDoesNotThrow(() -> this.webConfig.configureAsyncSupport(configurer));
    }
}