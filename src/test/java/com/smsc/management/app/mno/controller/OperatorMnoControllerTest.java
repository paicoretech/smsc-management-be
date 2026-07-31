package com.smsc.management.app.mno.controller;

import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorMnoControllerTest extends BaseIntegrationTest {
    @Autowired
    private OperatorMnoController operatorMnoController;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listOperator() {
        ResponseEntity<ApiResponse> response = operatorMnoController.listOperators();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertFalse(result.isEmpty());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createOperator() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("Test");
        operatorMNODTO.setTlvMessageReceiptId(true);
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> response = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(OperatorMNODTO.class, response.getBody().data());
        OperatorMNODTO result = (OperatorMNODTO) response.getBody().data();
        assertEquals("Test", result.getName());
        assertTrue(result.isTlvMessageReceiptId());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteOperator() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("Test2");
        operatorMNODTO.setTlvMessageReceiptId(true);
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> responseCreate = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(responseCreate);
        assertEquals(HttpStatus.OK, responseCreate.getStatusCode());
        assertInstanceOf(ApiResponse.class, responseCreate.getBody());
        assertInstanceOf(OperatorMNODTO.class, responseCreate.getBody().data());
        OperatorMNODTO resultCreate = (OperatorMNODTO) responseCreate.getBody().data();

        ResponseEntity<ApiResponse> response = operatorMnoController.deleteOperator(resultCreate.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateOperator() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("Test");
        operatorMNODTO.setTlvMessageReceiptId(true);
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> response = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(OperatorMNODTO.class, response.getBody().data());
        OperatorMNODTO result = (OperatorMNODTO) response.getBody().data();

        OperatorMNODTO updateOperatorMNODTO = new OperatorMNODTO();
        updateOperatorMNODTO.setName("Test2");
        updateOperatorMNODTO.setTlvMessageReceiptId(false);
        updateOperatorMNODTO.setTlvMessageReceiptId(false);

        ResponseEntity<ApiResponse> responseUpdate = operatorMnoController.updateOperator(updateOperatorMNODTO, result.getId());
        assertNotNull(responseUpdate);
        assertEquals(HttpStatus.OK, responseUpdate.getStatusCode());
        assertInstanceOf(ApiResponse.class, responseUpdate.getBody());
        assertInstanceOf(OperatorMNODTO.class, responseUpdate.getBody().data());
        OperatorMNODTO resultUpdate = (OperatorMNODTO) responseUpdate.getBody().data();
        assertEquals("Test2", resultUpdate.getName());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void dataIntegrityViolationException() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("Test");
        operatorMNODTO.setTlvMessageReceiptId(true);
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> response = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(OperatorMNODTO.class, response.getBody().data());
    }
}