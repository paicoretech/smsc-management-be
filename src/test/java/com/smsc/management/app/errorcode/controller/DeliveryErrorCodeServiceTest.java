package com.smsc.management.app.errorcode.controller;

import com.smsc.management.app.errorcode.dto.DeliveryErrorCodeDTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeliveryErrorCodeServiceTest extends BaseIntegrationTest {

    @Autowired
    private DeliveryErrorCodeService deliveryErrorCodeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listDeliveryErrorCodeTest() {
        ResponseEntity<ApiResponse> response = deliveryErrorCodeService.listDeliveryErrorCode();
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
        DeliveryErrorCodeDTO errorCode = getDeliveryErrorCodeDtoMock();
        ResponseEntity<ApiResponse> response = deliveryErrorCodeService.create(errorCode);
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        DeliveryErrorCodeDTO result = (DeliveryErrorCodeDTO) response.getBody().data();
        assertEquals("2", result.getCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTest() {
        DeliveryErrorCodeDTO errorCode = getDeliveryErrorCodeDtoMock();
        DeliveryErrorCodeDTO myNewDeliveryErrorCode = (DeliveryErrorCodeDTO) Objects.requireNonNull(deliveryErrorCodeService.create(errorCode).getBody()).data();
        errorCode.setDescription("MyUpdatedDescription");
        ResponseEntity<ApiResponse> updateResponse = deliveryErrorCodeService.update(errorCode, myNewDeliveryErrorCode.getId());
        assertNotNull(updateResponse);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteTest() {
        DeliveryErrorCodeDTO errorCode = getDeliveryErrorCodeDtoMock();
        DeliveryErrorCodeDTO myNewDeliveryErrorCode = (DeliveryErrorCodeDTO) Objects.requireNonNull(deliveryErrorCodeService.create(errorCode).getBody()).data();
        ResponseEntity<ApiResponse> deleteResponse = deliveryErrorCodeService.delete(myNewDeliveryErrorCode.getId());
        assertNotNull(deleteResponse);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    private DeliveryErrorCodeDTO getDeliveryErrorCodeDtoMock() {
        DeliveryErrorCodeDTO deliveryErrorCodeDTO = new DeliveryErrorCodeDTO();
        deliveryErrorCodeDTO.setCode("2");
        deliveryErrorCodeDTO.setDescription("MyCustomErrorCode");
        return deliveryErrorCodeDTO;
    }
}
