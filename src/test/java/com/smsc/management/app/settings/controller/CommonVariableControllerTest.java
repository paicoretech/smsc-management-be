package com.smsc.management.app.settings.controller;

import com.smsc.management.app.settings.dto.CommonVariablesDTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonVariableControllerTest extends BaseIntegrationTest {

    @Autowired
    private CommonVariableController commonVariableController;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getCommonVariableTest() {
        ResponseEntity<ApiResponse> response = commonVariableController.get();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertEquals(4, result.size());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void massiveUpdateTest() {
        var listToUpdate = getCommonVariablesDTOS();
        ResponseEntity<ApiResponse> response = commonVariableController.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

    }

    private List<CommonVariablesDTO> getCommonVariablesDTOS() {
        var commonVariablesAccountSettings = new CommonVariablesDTO();
        commonVariablesAccountSettings.setKey("SMSC_ACCOUNT_SETTINGS");
        commonVariablesAccountSettings.setDataType("json");
        commonVariablesAccountSettings.setValue("{\"max_password_length\":18,\"max_system_id_length\":20}");

        var commonVariablesLocalCharging = new CommonVariablesDTO();
        commonVariablesLocalCharging.setKey("USE_LOCAL_CHARGING");
        commonVariablesLocalCharging.setDataType("boolean");
        commonVariablesLocalCharging.setValue("false");

        var commonVariablesFake = new CommonVariablesDTO();
        commonVariablesFake.setKey("SMSC_FAKE");
        commonVariablesFake.setDataType("boolean");
        commonVariablesFake.setValue("false");

        return List.of(commonVariablesAccountSettings, commonVariablesLocalCharging, commonVariablesFake);
    }
}