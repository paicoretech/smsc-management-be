package com.smsc.management.app.sip.controller;

import com.smsc.management.app.sip.dto.SipGatewaysDTO;
import com.smsc.management.app.sip.service.SipGatewaysService;
import com.smsc.management.app.sip.utilsTest.SipUtils;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static com.smsc.management.app.sip.utilsTest.SipUtils.checkAssertions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class SipGatewaysControllerTest extends BaseIntegrationTest {

    @Autowired
    private SipGatewaysController sipGatewaysController;

    @MockBean
    private SipGatewaysService sipGatewaysService;

    @MockBean
    private Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    private Ss7GatewaysMapper ss7GatewaysMapper;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getListSip() {
        ApiResponse api = ResponseMapping.successMessage("success", List.of(SipUtils.getSipGatewaysDTO()));
        Mockito.when(sipGatewaysService.getSipGateways()).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.listSip();

        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getListSipError() {
        ApiResponse api = ResponseMapping.exceptionMessage("FAIL", new RuntimeException("FAIL"));
        Mockito.when(sipGatewaysService.getSipGateways()).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.listSip();

        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSipByNetworkId() {
        ApiResponse api = ResponseMapping.successMessage("success", SipUtils.getSipGatewaysDTO());
        Mockito.when(sipGatewaysService.getSipGatewaysByNetworkId(anyInt())).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.getSipByNetworkId(1);

        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSipByNetworkIdNotFound() {
        Mockito.when(sipGatewaysService.getSipGatewaysByNetworkId(anyInt()))
                .thenReturn(new ApiResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "error",
                        "not found",
                        null
                ));

        ResponseEntity<ApiResponse> response = sipGatewaysController.getSipByNetworkId(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createTest() {
        SipGatewaysDTO dto = SipUtils.newSipGatewaysDTO();
        ApiResponse api = ResponseMapping.successMessage("success", dto);

        Mockito.when(sipGatewaysService.create(any(SipGatewaysDTO.class))).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.create(dto);

        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createTestError() {
        SipGatewaysDTO dto = SipUtils.newSipGatewaysDTO();
        ApiResponse api = ResponseMapping.exceptionMessage("FAIL", new RuntimeException("FAIL"));

        Mockito.when(sipGatewaysService.create(any(SipGatewaysDTO.class))).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.create(dto);

        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTest() {
        SipGatewaysDTO dto = SipUtils.getSipGatewaysDTO();
        ApiResponse api = ResponseMapping.successMessage("success", dto);

        Mockito.when(sipGatewaysService.update(anyInt(), any(SipGatewaysDTO.class))).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.update(dto, 1);

        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTestError() {
        SipGatewaysDTO dto = SipUtils.getSipGatewaysDTO();
        ApiResponse api = ResponseMapping.exceptionMessage("FAIL", new RuntimeException("FAIL"));

        Mockito.when(sipGatewaysService.update(anyInt(), any(SipGatewaysDTO.class))).thenReturn(api);

        ResponseEntity<ApiResponse> response = sipGatewaysController.update(dto, 1);

        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listSipSs7AllowedUssi() {
        Ss7Gateways gw = new Ss7Gateways();
        Ss7GatewaysDTO gwDto = new Ss7GatewaysDTO();

        Mockito.when(ss7GatewaysRepository.findByEnabledNotAndAllowedUssiTrue(anyInt()))
                .thenReturn(List.of(gw));

        Mockito.when(ss7GatewaysMapper.toDTOList(anyList()))
                .thenReturn(List.of(gwDto));

        ResponseEntity<ApiResponse> response = sipGatewaysController.listSs7AllowedUssi();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().message());
        assertNotNull(response.getBody().data());
        assertInstanceOf(List.class, response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listSipSs7AllowedUssiError() {
        Mockito.when(ss7GatewaysRepository.findByEnabledNotAndAllowedUssiTrue(anyInt()))
                .thenThrow(new RuntimeException("FAIL"));

        assertThrows(RuntimeException.class, () -> sipGatewaysController.listSs7AllowedUssi());
    }
}
