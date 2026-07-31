package com.smsc.management.app.settings.controller;

import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HandlerStatusControllerTest extends BaseIntegrationTest {
    @Autowired
    HandlerStatusController handlerStatusController;

    @Test
    void handleStatus() {
        assertDoesNotThrow(() -> handlerStatusController.handleStatus());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void handlerServerHttp() {
        var response = handlerStatusController.handlerServerHttp("application_name", "STARTED");
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void handlerStatusAllServerHttp() {
        var response = handlerStatusController.handlerStatusAllServerHttp("STARTED");
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getHttpServerConfig() {
        var response = handlerStatusController.getHttpServerConfig();
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}