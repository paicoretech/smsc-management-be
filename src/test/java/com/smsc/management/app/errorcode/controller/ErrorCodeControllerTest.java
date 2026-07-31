package com.smsc.management.app.errorcode.controller;

import com.smsc.management.app.errorcode.dto.ErrorCodeDTO;
import com.smsc.management.app.mno.controller.OperatorMnoController;
import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorCodeControllerTest extends BaseIntegrationTest {

    @Autowired
    private ErrorCodeController errorCodeController;

    @Autowired
    private OperatorMnoController operatorMnoController;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listErrorCodeTest() {
        ResponseEntity<ApiResponse> response = errorCodeController.listErrorCode();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertEquals(0, result.size());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listErrorCodeByMNOTest() {
        ErrorCodeDTO errorCode = getErrorCodeDtoMock();
        errorCodeController.createErrorCode(errorCode);
        ResponseEntity<ApiResponse> response = errorCodeController.listErrorCodeByMNO(errorCode.getMnoId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertFalse(result.isEmpty());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createErrorCodeTest() {
        ErrorCodeDTO myNewErrorCode = getErrorCodeDtoMock();
        ResponseEntity<ApiResponse> response = errorCodeController.createErrorCode(myNewErrorCode);
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ErrorCodeDTO result = (ErrorCodeDTO) response.getBody().data();
        assertEquals("3", result.getCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateErrorCodeTest() {
        ErrorCodeDTO errorCode = getErrorCodeDtoMock();
        ErrorCodeDTO myNewErrorCodeDto = (ErrorCodeDTO) Objects.requireNonNull(errorCodeController.createErrorCode(errorCode).getBody()).data();
        myNewErrorCodeDto.setDescription("UpdatedErrorCodeDescription");
        ResponseEntity<ApiResponse> updateResponse = errorCodeController.updateErrorCode(myNewErrorCodeDto, myNewErrorCodeDto.getId());
        assertNotNull(updateResponse);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteErrorCodeTest() {
        ErrorCodeDTO errorCode = getErrorCodeDtoMock();
        ErrorCodeDTO myNewErrorCodeDto = (ErrorCodeDTO) Objects.requireNonNull(errorCodeController.createErrorCode(errorCode).getBody()).data();
        ResponseEntity<ApiResponse> deleteResponse = errorCodeController.deleteErrorCode(myNewErrorCodeDto.getId());
        assertNotNull(deleteResponse);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    private ErrorCodeDTO getErrorCodeDtoMock() {
        OperatorMNODTO operatorMno = new OperatorMNODTO();
        operatorMno.setName("MyNewTestMNO");
        operatorMno.setTlvMessageReceiptId(true);
        operatorMno.setTlvMessageReceiptId(true);
        ResponseEntity<ApiResponse> response = operatorMnoController.createOperator(operatorMno);
        OperatorMNODTO newOperatorMno = (OperatorMNODTO) Objects.requireNonNull(response.getBody()).data();
        ErrorCodeDTO myNewErrorCode = new ErrorCodeDTO();
        myNewErrorCode.setCode("3");
        myNewErrorCode.setDescription("MyNewErrorCodeDescription");
        myNewErrorCode.setMnoId(newOperatorMno.getId());
        return myNewErrorCode;
    }
}
