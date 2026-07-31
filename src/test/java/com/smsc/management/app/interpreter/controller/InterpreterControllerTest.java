package com.smsc.management.app.interpreter.controller;

import com.smsc.management.app.gateway.controller.GatewaysController;
import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.gateway.dto.ParseGatewaysDTO;
import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InterpreterControllerTest extends BaseIntegrationTest {

    @Autowired
    private InterpreterController interpreterController;

    @Autowired
    private GatewaysController gatewaysController;

    @Autowired
    private OperatorMnoRepository operatorMnoRepository;

    int gatewayId = 1;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Update when interpreter exists and all data is ok then http status is OK")
    void updateWhenAllDataIsOkThenDoItSuccessfully(boolean isDefaultTemplate) {
        String randomName = new Random().nextLong() + "";
        var newMno = new OperatorMno(null, randomName, false, false, true);
        operatorMnoRepository.saveAndFlush(newMno);

        if (isDefaultTemplate) {
            int randomNumber = new Random().nextInt(10) + 1;
            String externalId = String.valueOf(randomNumber);
            gatewayId = this.createGateway(newMno.getId(), externalId);
        }

        InterpreterDTO interpreter = new InterpreterDTO();
        interpreter.setId(1);
        interpreter.setBodyType("XML");
        interpreter.setTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <dialog mapMessagesSize=\"1\" userObject=\"{{randomString}}\"> <unstructuredSSRequest_Request dataCodingScheme=\"15\" string=\"Paquete Sin Limite Activo&#xA;Saldo: $1 al 01/03/24&#xA;&#xA;1. Recargas&#xA;2. Paquetes&#xA;3. Adelantos&#xA;4. Servicios sin saldo&#xA;5. Detalle de saldo\"> <msisdn nai=\"international_number\" npi=\"ISDN\" number=\"525532368999\"/> </unstructuredSSRequest_Request> </dialog>");
        interpreter.setUseProxy(true);
        interpreter.setPath("/message");
        interpreter.setDefaultTemplate(isDefaultTemplate);

        ResponseEntity<ApiResponse>  response = interpreterController.update(1, interpreter);
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        var interpreterResponse = (InterpreterDTO) Objects.requireNonNull(apiResponse.data());

        if (isDefaultTemplate) {
            assertEquals("JSON", interpreterResponse.getBodyType()); // default template no update bodyType
        } else {
            assertEquals("XML", interpreterResponse.getBodyType());
        }
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Get interpreter list")
    void getWhenInterpreterExistsThenDoItSuccessfully() {
        ResponseEntity<ApiResponse>  response = interpreterController.get();
        ApiResponse apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        var smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        assertEquals(0, smppServerResponseList.size());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Update when interpreter not exists")
    void updateWhenInterpreterNotExistsThenHTTPStatusErrorResponse() {
        ResponseEntity<ApiResponse>  response = interpreterController.update(10, new InterpreterDTO());
        checkAssertions(response, HttpStatus.BAD_REQUEST);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Get http gateway when gateway exists then http response is successfully")
    void getHttpGatewaysWhenGatewayHttpExist() {
        ResponseEntity<ApiResponse>  response = interpreterController.getHttpGateways();
        ApiResponse apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        var httpGatewaysList = (List<?>) Objects.requireNonNull(apiResponse.data());
        assertFalse(httpGatewaysList.isEmpty());
    }

    public static void checkAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus) {
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();

        switch (httpStatus) {
            case OK -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("success", response.getBody().message());
                assertNotNull(Objects.requireNonNull(apiResponse).data());
            }
            case NOT_FOUND -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case BAD_REQUEST -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case INTERNAL_SERVER_ERROR -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNull(response.getBody().data());
                assertEquals("error", response.getBody().message());
            }
            default -> throw new IllegalStateException("Unexpected value: " + response.getStatusCode());
        }
    }

    private int createGateway(Integer mnoId, String externalId) {
        GatewaysDTO gatewaysDTO = getMockGatewayDto(mnoId, externalId);
        gatewaysController.create(gatewaysDTO);
        ResponseEntity<ApiResponse> response = gatewaysController.getGatewayByExternalId(externalId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(ParseGatewaysDTO.class, response.getBody().data());
        ParseGatewaysDTO result = (ParseGatewaysDTO) response.getBody().data();
        return result.getNetworkId();
    }

    private GatewaysDTO getMockGatewayDto(Integer mnoId, String externalId) {
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setExternalId(externalId);
        gatewaysDTO.setName("This is a Test Gateway");
        gatewaysDTO.setSystemId("GW_TEST");
        gatewaysDTO.setPassword("GwPwd58");
        gatewaysDTO.setIp("127.0.0.1");
        gatewaysDTO.setPort(2776);
        gatewaysDTO.setBindType("TRANSCEIVER");
        gatewaysDTO.setSystemType("SystemType");
        gatewaysDTO.setInterfaceVersion("IF_34");
        gatewaysDTO.setSessionsNumber(4);
        gatewaysDTO.setAddressTon(2);
        gatewaysDTO.setAddressNpi(1);
        gatewaysDTO.setAddressRange("");
        gatewaysDTO.setEnabled(1);
        gatewaysDTO.setEnquireLinkPeriod(5000);
        gatewaysDTO.setRequestDlr(2);
        gatewaysDTO.setNoRetryErrorCode("Retry");
        gatewaysDTO.setRetryAlternateDestinationErrorCode("RetryAlternate");
        gatewaysDTO.setBindTimeout(500);
        gatewaysDTO.setBindRetryPeriod(5000);
        gatewaysDTO.setPduTimeout(5000);
        gatewaysDTO.setPduDegree(500);
        gatewaysDTO.setThreadPoolSize(150);
        gatewaysDTO.setMnoId(mnoId);
        gatewaysDTO.setProtocol("HTTP");
        gatewaysDTO.setAuthenticationTypes("undefined");
        gatewaysDTO.setAutoRetryErrorCode("AutoRetry");
        gatewaysDTO.setEncodingIso88591(3);
        gatewaysDTO.setEncodingGsm7(0);
        gatewaysDTO.setEncodingUcs2(2);
        gatewaysDTO.setSplitMessage(Boolean.FALSE);
        gatewaysDTO.setSplitSmppType("TLV");
        return gatewaysDTO;
    }
}