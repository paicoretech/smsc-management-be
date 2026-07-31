package com.smsc.management.app.gateway.controller;

import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.gateway.dto.ParseGatewaysDTO;
import com.smsc.management.app.gateway.mapper.GatewaysMapper;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewaysControllerTest extends BaseIntegrationTest {

    @Autowired
    private GatewaysController gatewaysController;

    @Autowired
    private OperatorMnoRepository operatorMnoRepository;
    @Autowired
    private GatewaysRepository gatewaysRepository;

    @Disabled
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listGateway() {
        ResponseEntity<ApiResponse> response = gatewaysController.listGateway();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertEquals(0, result.size());
    }

    private OperatorMno seedMno() {
        String randomName = Long.toString(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
        OperatorMno mno = new OperatorMno(null, randomName, false, false, true);
        return operatorMnoRepository.saveAndFlush(mno);
    }

    private String uniqueExternalId() {
        return Long.toString(System.nanoTime());
    }

    @Disabled
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getGatewayByExtIdTest() {
        var mno = seedMno();
        String externalId = uniqueExternalId();

        GatewaysDTO gatewaysDTO = getMockGatewayDto(mno.getId(), externalId);

        ResponseEntity<ApiResponse> createResp = gatewaysController.create(gatewaysDTO);
        assertNotNull(createResp);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        assertInstanceOf(ApiResponse.class, createResp.getBody());
        assertInstanceOf(ParseGatewaysDTO.class, createResp.getBody().data());
        assertEquals(externalId, ((ParseGatewaysDTO) createResp.getBody().data()).getExternalId());

        ResponseEntity<ApiResponse> response = gatewaysController.getGatewayByExternalId(externalId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(ParseGatewaysDTO.class, response.getBody().data());
        assertEquals(externalId, ((ParseGatewaysDTO) response.getBody().data()).getExternalId());
    }

    @Disabled
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createGateway() {
        var mno = seedMno();
        String externalId = uniqueExternalId();
        GatewaysDTO gatewaysDTO = getMockGatewayDto(mno.getId(), externalId);
        ResponseEntity<ApiResponse> response = gatewaysController.create(gatewaysDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(ParseGatewaysDTO.class, response.getBody().data());
        ParseGatewaysDTO result = (ParseGatewaysDTO) response.getBody().data();
        assertEquals("This is a Test Gateway", result.getName());
        assertEquals(2, result.getRequestDlr());
        Assertions.assertFalse(result.isSplitMessage());
        assertEquals(5000, result.getBindRetryPeriod());
        assertEquals("TRANSCEIVER", result.getBindType());
    }

    @Disabled
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateGatewayByExternalIdTest() {
        var mno = seedMno();
        String externalId = uniqueExternalId();
        GatewaysDTO gatewaysDTO = getMockGatewayDto(mno.getId(), externalId);
        ResponseEntity<ApiResponse> response = gatewaysController.create(gatewaysDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GatewaysMapper mapper = Mappers.getMapper(GatewaysMapper.class);
        Iterable<Gateways> allCurrentGws = gatewaysRepository.findAll();
        assertNotNull(allCurrentGws);
        assertTrue(allCurrentGws.iterator().hasNext());

        Gateways firstGw = allCurrentGws.iterator().next();
        GatewaysDTO dtoBasedInFirstGateway = mapper.toGatewaysDTO(firstGw);
        dtoBasedInFirstGateway.setExternalId("lin1");

        ResponseEntity<ApiResponse> firstUpdateResponse = gatewaysController.update(dtoBasedInFirstGateway, firstGw.getNetworkId());
        assertNotNull(firstUpdateResponse);
        assertEquals(HttpStatus.OK, firstUpdateResponse.getStatusCode());
        assertInstanceOf(ApiResponse.class, firstUpdateResponse.getBody());

        dtoBasedInFirstGateway.setBindTimeout(5000);
        ResponseEntity<ApiResponse> secondUpdateResponse = gatewaysController.updateByExternalId(dtoBasedInFirstGateway, "lin1");
        assertNotNull(secondUpdateResponse);
        assertEquals(HttpStatus.OK, secondUpdateResponse.getStatusCode());
        assertInstanceOf(ApiResponse.class, secondUpdateResponse.getBody());
    }

    private GatewaysDTO getMockGatewayDto(Integer mnoId, String externalId) {
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setNetworkId(1);
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
        gatewaysDTO.setProtocol("SMPP");
        gatewaysDTO.setAutoRetryErrorCode("AutoRetry");
        gatewaysDTO.setEncodingIso88591(3);
        gatewaysDTO.setEncodingGsm7(0);
        gatewaysDTO.setEncodingUcs2(2);
        gatewaysDTO.setSplitMessage(Boolean.FALSE);
        gatewaysDTO.setSplitSmppType("TLV");
        return gatewaysDTO;
    }
}
