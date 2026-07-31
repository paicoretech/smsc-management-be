package com.smsc.management.utils;

import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppPropertiesTest extends BaseIntegrationTest {
    @Autowired
    AppProperties appProperties;

    @Test
    void getSecretKey() {
        assertNotNull(appProperties.getSecretKey());
    }

    @Test
    void getJwtExpiration() {
        assertDoesNotThrow(() -> appProperties.getJwtExpiration());
    }
}