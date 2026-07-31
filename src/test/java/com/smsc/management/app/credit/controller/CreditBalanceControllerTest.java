package com.smsc.management.app.credit.controller;

import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.credit.custom.HandlerServiceProvider;
import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import com.smsc.management.app.credit.dto.UsedCreditByInstanceDTO;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import com.smsc.management.app.provider.mapper.ServiceProviderMapper;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.GlobalRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreditBalanceControllerTest extends BaseIntegrationTest {

    @Autowired
    CreditBalanceController creditBalanceController;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    ServiceProviderMapper serviceProviderMapper;

    @Autowired
    SequenceNetworksIdRepository sequenceNetworksIdRepository;

    @Autowired
    HandlerCreditByServiceProvider handlerBalance;

    @Autowired
    HandlerServiceProvider handlerSp;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "credit_sales_history");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "callback_header_http");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "service_provider");
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void getCurrentRatingTest() {
        GlobalRecords.SystemIdInputParameter inputParameter = new GlobalRecords.SystemIdInputParameter("SystemId");
        ResponseEntity<ApiResponse> response = creditBalanceController.currentRating(inputParameter);
        assertNotNull(response);
        assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void sellCreditTestByNetworkId() {
        sellCredit(Boolean.FALSE);
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void sellCreditTestByExternalId() {
        sellCredit(Boolean.TRUE);
    }

    void sellCredit(boolean byExternalId) {
        SequenceNetworksId sequenceNetworksId = new SequenceNetworksId();
        sequenceNetworksId.setNetworkType("NET");
        sequenceNetworksIdRepository.save(sequenceNetworksId);
        serviceProviderRepository.save(createAndGetServiceProvider(sequenceNetworksId.getId()));
        CreditSalesHistoryDTO creditSalesHistoryDTO = new CreditSalesHistoryDTO();
        creditSalesHistoryDTO.setId(1);
        creditSalesHistoryDTO.setNetworkId(sequenceNetworksId.getId());
        creditSalesHistoryDTO.setCredit(1_000_000L);
        creditSalesHistoryDTO.setDescription("");
        creditSalesHistoryDTO.setCreatedAt(LocalDateTime.now());
        creditSalesHistoryDTO.setUpdatedAt(LocalDateTime.now());
        creditSalesHistoryDTO.setCreatedBy("admin");
        ResponseEntity<ApiResponse> response = byExternalId ?
                creditBalanceController.sellCreditByExternalId(creditSalesHistoryDTO, "0001") :
                creditBalanceController.sellCredit(creditSalesHistoryDTO, sequenceNetworksId.getId());
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void usedCreditFromInstanceTest() {
        SequenceNetworksId sequenceNetworksId = new SequenceNetworksId();
        sequenceNetworksId.setNetworkType("GW");
        sequenceNetworksIdRepository.save(sequenceNetworksId);
        serviceProviderRepository.save(createAndGetServiceProvider(sequenceNetworksId.getId()));

        UsedCreditByInstanceDTO usedCreditByInstanceDTO = new UsedCreditByInstanceDTO();
        usedCreditByInstanceDTO.setNetworkId(1);
        usedCreditByInstanceDTO.setCreditUsed(1);
        List<UsedCreditByInstanceDTO> usedCreditList = new ArrayList<>();
        usedCreditList.add(usedCreditByInstanceDTO);

        ResponseEntity<ApiResponse> response = creditBalanceController.usedCreditFromInstance(usedCreditList, "smpp-module");
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
    }

    private ServiceProvider createAndGetServiceProvider(int networkId) {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setNetworkId(networkId);
        serviceProviderDTO.setName("ServiceProvider");
        serviceProviderDTO.setExternalId("0001");
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
        serviceProviderDTO.setEnabled(0);
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
        serviceProviderDTO.setToken("AizA000eTokenTesting00");
        serviceProviderDTO.setBindType("TRANSCEIVER");
        serviceProviderDTO.setMessagePriority("High");
        List<CallbackHeaderHttpDTO> callbakHeaderList = new ArrayList<>();
        CallbackHeaderHttpDTO headerHttpDTO = new CallbackHeaderHttpDTO();
        headerHttpDTO.setHeaderName("Content-Type");
        headerHttpDTO.setHeaderValue("application/json");
        callbakHeaderList.add(headerHttpDTO);
        serviceProviderDTO.setCallbackHeadersHttp(callbakHeaderList);
        serviceProviderDTO.setSmppServerId(1);
        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(serviceProviderDTO);
        RedisServiceProviderDTO redisServiceProviderDTO = serviceProviderMapper.toServiceProviderDTO(serviceProvider);
        handlerSp.addToCache(serviceProviderDTO.getNetworkId(), redisServiceProviderDTO);
        handlerBalance.addNewHandlerBalance(serviceProviderDTO.getNetworkId(), serviceProviderDTO.getBalance());
        return serviceProviderRepository.save(serviceProvider);
    }
}
