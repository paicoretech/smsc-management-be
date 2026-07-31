package com.smsc.management.app.provider.controller;

import com.smsc.management.app.catalog.model.repository.BalanceTypeRepository;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.provider.dto.ParseServiceProviderDTO;
import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceProviderControllerTest extends BaseIntegrationTest {
    @Autowired
    private ServiceProviderController serviceProviderController;

    @Autowired
    private BalanceTypeRepository balanceTypeRepository;

    private ServiceProviderDTO createAndGetServiceProvider() {
        ServiceProviderDTO serviceProviderDTO = createAndGetServiceProvider("1");
        serviceProviderDTO.setSystemId("System20");
        return serviceProviderDTO;
    }

    private ServiceProviderDTO createAndGetServiceProvider(String externalId) {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setNetworkId(5);
        serviceProviderDTO.setExternalId(externalId);
        serviceProviderDTO.setName("ServiceProvider");
        serviceProviderDTO.setSystemId("SystemId");
        serviceProviderDTO.setPassword("PassW0rD");
        serviceProviderDTO.setSystemType("SystemType");
        serviceProviderDTO.setInterfaceVersion("IF_34");
        serviceProviderDTO.setSessionsNumber(20);
        serviceProviderDTO.setAddressTon(2);
        serviceProviderDTO.setAddressNpi(1);
        serviceProviderDTO.setAddressRange("");
        serviceProviderDTO.setBalanceType("POSTPAID");
        serviceProviderDTO.setBalance(1_000_000L);
        serviceProviderDTO.setTps(5);
        serviceProviderDTO.setValidity(5);
        serviceProviderDTO.setEnabled(1);
        serviceProviderDTO.setEnquireLinkPeriod(500);
        serviceProviderDTO.setPduTimeout(2500);
        serviceProviderDTO.setProtocol("SMPP");
        serviceProviderDTO.setContactName("ContactName");
        serviceProviderDTO.setEmail("service_provider@domain.com");
        serviceProviderDTO.setPhoneNumber("00016544");
        serviceProviderDTO.setCallbackUrl("http://127.0.0.1/callback/");
        serviceProviderDTO.setAuthenticationTypes("Bearer");
        serviceProviderDTO.setHeaderSecurityName("Authorization");
        serviceProviderDTO.setUserName("UserName");
        serviceProviderDTO.setPasswd("PaSsWd");
        serviceProviderDTO.setBindType("TRANSCEIVER");
        serviceProviderDTO.setToken("AizA000eTokenTesting00");
        serviceProviderDTO.setMessagePriority("High");
        List<CallbackHeaderHttpDTO> callbakHeaderList = new ArrayList<>();
        CallbackHeaderHttpDTO headerHttpDTO = new CallbackHeaderHttpDTO();
        headerHttpDTO.setHeaderName("Content-Type");
        headerHttpDTO.setHeaderValue("application/json");
        callbakHeaderList.add(headerHttpDTO);
        serviceProviderDTO.setCallbackHeadersHttp(callbakHeaderList);
        return  serviceProviderDTO;
    }

    private ServiceProviderDTO getUpdatedServiceProvider(ServiceProviderDTO foundServiceProvider) {
        foundServiceProvider.setName("This is the updated Service Provider");
        foundServiceProvider.setProtocol("HTTP");
        foundServiceProvider.setEmail("another_email@anotherDomain.com");
        foundServiceProvider.setBalance(200_000_000L);
        foundServiceProvider.setBindType("TRANSCEIVER");
        foundServiceProvider.setAuthenticationTypes("Bearer");
        foundServiceProvider.setSecurityAuthenticationType("Bearer");
        foundServiceProvider.setBearerTokenExpirationSeconds(3600L);
        foundServiceProvider.setHeaderSecurityName("Authorization");
        return foundServiceProvider;
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void listServiceProviders() {
        serviceProviderController.createProvider(createAndGetServiceProvider());
        ResponseEntity<ApiResponse> response = serviceProviderController.listServiceProvider();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(List.class, response.getBody().data());
        List<?> result = (List<?>) response.getBody().data();
        assertFalse(result.isEmpty());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getServiceProviderByExtIdTest() {
        serviceProviderController.createProvider(createAndGetServiceProvider("10"));
        ResponseEntity<ApiResponse> response = serviceProviderController.getServiceProviderByExternalId("10");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(ParseServiceProviderDTO.class, response.getBody().data());
        assertEquals("10",((ParseServiceProviderDTO)response.getBody().data()).getExternalId());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getNonExistingServiceProviderByExtIdTest() {
        serviceProviderController.createProvider(createAndGetServiceProvider());
        ResponseEntity<ApiResponse> response = serviceProviderController.getServiceProviderByExternalId("10");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createDuplicatedExternalIdSPTest() {
        serviceProviderController.createProvider(createAndGetServiceProvider());
        ServiceProviderDTO serviceProviderDTO = createAndGetServiceProvider("1");
        assertThrows(DataIntegrityViolationException.class, () -> serviceProviderController.createProvider(serviceProviderDTO));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createServiceProvider() {

        ServiceProviderDTO serviceProviderDTO = createAndGetServiceProvider();
        assertNull(serviceProviderDTO.getSmppServerId());
        ResponseEntity<ApiResponse> response = serviceProviderController.createProvider(serviceProviderDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(ParseServiceProviderDTO.class, response.getBody().data());
        ParseServiceProviderDTO result = (ParseServiceProviderDTO) response.getBody().data();
        assertEquals("ServiceProvider", result.getName());

        assertEquals(2500, result.getPduTimeout());
        assertEquals("SMPP", result.getProtocol());
        assertEquals(1, result.getSmppServerId());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateServiceProviderTest() {
       updateTest(Boolean.TRUE);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateServiceProviderByExtIdTest() {
        updateTest(Boolean.FALSE);
    }

    private void updateTest(boolean updateByPk) {
        ServiceProviderDTO serviceProviderDTO = updateByPk ? createAndGetServiceProvider() : createAndGetServiceProvider("10");
        serviceProviderDTO.setSmppServerId(1);
        ResponseEntity<ApiResponse> response = serviceProviderController.createProvider(serviceProviderDTO);
        ParseServiceProviderDTO result = (ParseServiceProviderDTO) Objects.requireNonNull(response.getBody()).data();

        ServiceProviderDTO updatedServiceProvider = getUpdatedServiceProvider(serviceProviderDTO);
        ResponseEntity<ApiResponse> updateResponse = updateByPk ?
                serviceProviderController.updateProvider(updatedServiceProvider, result.getNetworkId()) :
                serviceProviderController.updateByExternalId(updatedServiceProvider, result.getExternalId());
        assertNotNull(updateResponse);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertInstanceOf(ApiResponse.class, updateResponse.getBody());
        assertInstanceOf(ParseServiceProviderDTO.class, updateResponse.getBody().data());
        ParseServiceProviderDTO updateResult = (ParseServiceProviderDTO) updateResponse.getBody().data();

        assertEquals("This is the updated Service Provider", updateResult.getName());
        assertEquals("HTTP", updateResult.getProtocol());
        assertEquals("another_email@anotherDomain.com", updateResult.getEmail());
        assertEquals(0L, updateResult.getBalance());
        assertEquals(1, updateResult.getSmppServerId());
    }
}
