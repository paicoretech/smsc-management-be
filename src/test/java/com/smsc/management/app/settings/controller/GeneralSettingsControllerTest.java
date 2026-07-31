package com.smsc.management.app.settings.controller;

import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeneralSettingsControllerTest extends BaseIntegrationTest {

    @Autowired
    private GeneralSettingsController generalSettingsController;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listGeneralSettingsTest() {
        ResponseEntity<ApiResponse> response = generalSettingsController.listGeneralSettings();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(GeneralSettingsSmppHttpDTO.class, response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTest() {
        int newValidityPeriod = 120;
        GeneralSettingsSmppHttpDTO generalSettingsSmppHttpDTO = new GeneralSettingsSmppHttpDTO();
        generalSettingsSmppHttpDTO.setValidityPeriod(newValidityPeriod);
        ResponseEntity<ApiResponse> response = generalSettingsController.update(generalSettingsSmppHttpDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(GeneralSettingsSmppHttpDTO.class, response.getBody().data());
        var dataResponse = (GeneralSettingsSmppHttpDTO) response.getBody().data();
        assertEquals(newValidityPeriod, dataResponse.getValidityPeriod());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void smscRetryTest() {
        ResponseEntity<ApiResponse> response = generalSettingsController.smscRetry();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(GeneralSmscRetryDTO.class, response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSmscRetryTest() {
        GeneralSmscRetryDTO generalSmscRetryDTO = new GeneralSmscRetryDTO();
        int newMaxDueDelay = 500;
        int newRetryDelayMultiplier = 4;
        int newFirstRetryDelay = 10;
        generalSmscRetryDTO.setMaxDueDelay(newMaxDueDelay);
        generalSmscRetryDTO.setRetryDelayMultiplier(newRetryDelayMultiplier);
        generalSmscRetryDTO.setFirstRetryDelay(newFirstRetryDelay);
        ResponseEntity<ApiResponse> response = generalSettingsController.updateSmscRetry(generalSmscRetryDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(GeneralSmscRetryDTO.class, response.getBody().data());
        var dataResponse = (GeneralSmscRetryDTO) response.getBody().data();
        assertEquals(newMaxDueDelay, dataResponse.getMaxDueDelay());
        assertEquals(newRetryDelayMultiplier, dataResponse.getRetryDelayMultiplier());
        assertEquals(newFirstRetryDelay, dataResponse.getFirstRetryDelay());
    }
}