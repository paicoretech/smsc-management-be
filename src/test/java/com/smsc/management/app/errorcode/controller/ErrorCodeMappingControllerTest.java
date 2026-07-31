package com.smsc.management.app.errorcode.controller;

import com.smsc.management.app.catalog.model.entity.DeliveryStatus;
import com.smsc.management.app.catalog.model.repository.DeliveryStatusRepository;
import com.smsc.management.app.errorcode.dto.ErrorCodeMappingDTO;
import com.smsc.management.app.errorcode.model.entity.DeliveryErrorCode;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;
import com.smsc.management.app.errorcode.model.repository.DeliveryErrorCodeRepository;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeMappingControllerTest extends BaseIntegrationTest {

    @Autowired
    ErrorCodeMappingController errorCodeMappingController;

    @Autowired
    DeliveryStatusRepository deliveryStatusRepository;

    @Autowired
    OperatorMnoController operatorMnoController;

    @Autowired
    DeliveryErrorCodeRepository deliveryErrorCodeRepository;

    @Autowired
    ErrorCodeRepository errorCodeRepository;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listErrorCodeMappingTest() {
        ResponseEntity<ApiResponse> response = errorCodeMappingController.listErrorCodeMapping();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertEquals(0, result.size());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createTest() {
        ErrorCodeMappingDTO myNewErrorCodeMappingDto = getErrorCodeMappingDtoMock();
        ResponseEntity<ApiResponse> response = errorCodeMappingController.create(myNewErrorCodeMappingDto);
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ErrorCodeMappingDTO result = (ErrorCodeMappingDTO) response.getBody().data();
        assertTrue(result.getId() > 0);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTest() {
        ErrorCodeMappingDTO errorCodeMappingDto = getErrorCodeMappingDtoMock();
        ErrorCodeMappingDTO myNewErrorCodeMapping = (ErrorCodeMappingDTO) Objects.requireNonNull(errorCodeMappingController.create(errorCodeMappingDto).getBody()).data();
        DeliveryStatus newDeliveredStatus = getDeliveryStatus("UNDELIVERED");
        myNewErrorCodeMapping.setDeliveryStatusId(newDeliveredStatus.getValue());
        ResponseEntity<ApiResponse> updateResponse = errorCodeMappingController.update(myNewErrorCodeMapping, myNewErrorCodeMapping.getId());
        assertNotNull(updateResponse);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteTest() {
        ErrorCodeMappingDTO errorCodeMappingDto = getErrorCodeMappingDtoMock();
        ErrorCodeMappingDTO myNewErrorCodeMapping = (ErrorCodeMappingDTO) Objects.requireNonNull(errorCodeMappingController.create(errorCodeMappingDto).getBody()).data();
        ResponseEntity<ApiResponse> deleteResponse = errorCodeMappingController.delete(myNewErrorCodeMapping.getId());
        assertNotNull(deleteResponse);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    private ErrorCodeMappingDTO getErrorCodeMappingDtoMock() {
        DeliveryErrorCode newDeliveryErrorCode = deliveryErrorCodeRepository.save(getDeliveryErrorCodeMock());
        ErrorCode newErrorCode = errorCodeRepository.save(getErrorCodeMock());
        DeliveryStatus existingDeliveryStatus = getDeliveryStatus("DELIVERED");
        ErrorCodeMappingDTO errorCodeMappingDTO = new ErrorCodeMappingDTO();
        errorCodeMappingDTO.setDeliveryErrorCodeId(newDeliveryErrorCode.getId());
        errorCodeMappingDTO.setErrorCodeId(newErrorCode.getId());
        errorCodeMappingDTO.setDeliveryStatusId(existingDeliveryStatus.getValue());
        return errorCodeMappingDTO;
    }

    private DeliveryStatus getDeliveryStatus(String statusName) {
        return deliveryStatusRepository.findByName(statusName);
    }

    private DeliveryErrorCode getDeliveryErrorCodeMock() {
        DeliveryErrorCode deliveryErrorCode = new DeliveryErrorCode();
        deliveryErrorCode.setCode("5");
        deliveryErrorCode.setDescription("Description");
        return deliveryErrorCode;
    }

    private ErrorCode getErrorCodeMock() {
        OperatorMNODTO operatorMno = new OperatorMNODTO();
        operatorMno.setName("MyNewTestMNO");
        operatorMno.setTlvMessageReceiptId(true);
        operatorMno.setTlvMessageReceiptId(true);
        ResponseEntity<ApiResponse> response = operatorMnoController.createOperator(operatorMno);
        OperatorMNODTO newOperatorMno = (OperatorMNODTO) Objects.requireNonNull(response.getBody()).data();
        ErrorCode myNewErrorCode = new ErrorCode();
        myNewErrorCode.setCode("3");
        myNewErrorCode.setDescription("MyNewErrorCodeDescription");
        myNewErrorCode.setMnoId(newOperatorMno.getId());
        return myNewErrorCode;
    }
}
