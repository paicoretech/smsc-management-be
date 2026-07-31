package com.smsc.management.app.credit.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import com.smsc.management.app.credit.dto.UsedCreditByInstanceDTO;
import com.smsc.management.app.credit.mapper.CreditSalesMapper;
import com.smsc.management.app.credit.model.repository.CreditSalesHistoryRepository;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ProcessCreditServiceTest {

    @Mock
    CreditSalesHistoryRepository creditSalesRepo;

    @Mock
    ServiceProviderRepository serviceProviderRepo;

    @Mock
    UtilsBase utilsBase;

    @Mock
    CreditSalesMapper creditSalesMapper;

    @Mock
    HandlerCreditByServiceProvider handlerCreditSp;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    ProcessCreditService processCreditService;

    @Test
    void testUpdateCreditBalanceNotFoundTest() {
        when(serviceProviderRepo.findByNetworkIdAndEnabledNot(anyInt(), anyInt())).thenReturn(null);
        ApiResponse response = processCreditService.updateCreditBalance(any(), anyInt());
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    void testUpdateCreditBalanceCreditNotAddedTest() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setNetworkId(1);
        when(serviceProviderRepo.findByNetworkIdAndEnabledNot(anyInt(), anyInt())).thenReturn(serviceProvider);
        CreditSalesHistoryDTO creditSales = new CreditSalesHistoryDTO();
        creditSales.setCredit(2_000_000L);
        creditSales.setDescription("Lots of data");
        ApiResponse response = processCreditService.updateCreditBalance(creditSales, 1);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    void updateCreditBalanceByExternalIdTest() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setNetworkId(1);
        serviceProvider.setExternalId("001");
        when(serviceProviderRepo.findByExternalIdAndEnabledNot(anyString(), anyInt())).thenReturn(serviceProvider);
        CreditSalesHistoryDTO creditSales = new CreditSalesHistoryDTO();
        creditSales.setCredit(2_000_000L);
        creditSales.setDescription("Lots of data");
        ApiResponse response = processCreditService.updateCreditBalance(creditSales, serviceProvider.getExternalId());
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    void testUpdateCreditBalanceCreditAnyExceptionTest() {
        when(serviceProviderRepo.findByNetworkIdAndEnabledNot(anyInt(), anyInt())).thenThrow(new RuntimeException());
        ApiResponse response = processCreditService.updateCreditBalance(any(), anyInt());
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void getRatingServiceProviderNotNullTest() {
        getRatingServiceTest("{\"tps\":2000}", "{\"tps\":2000}");
    }

    @Test
    void getRatingServiceProviderEmptyBalanceTest() {
        getRatingServiceTest("{}", "");
    }

    @Test
    void getRatingServiceProviderNullBalanceTest() {
        getRatingServiceTest("{}", null);
    }

    @Test
    void getRatingServiceProviderAnyExceptionTest() {
        when(utilsBase.getInRedis(anyString(), anyString())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = processCreditService.getRating("SystemId");
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void updateEmptyMassiveUsedCreditsTest() {
        List<UsedCreditByInstanceDTO> usedCreditList = new ArrayList<>();
        ApiResponse response = processCreditService.updateMassiveUsedCredits(usedCreditList, "SystemId2");
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    void updateMassiveUsedCreditsAnyExceptionTest() {
        ApiResponse response = processCreditService.updateMassiveUsedCredits(null, "SystemId2");
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    private void getRatingServiceTest(String firstValue, String secondValue) {
        when(utilsBase.getInRedis(GeneralSmscConstants.SERVICE_PROVIDERS_HASH_NAME, "SystemId"))
                .thenReturn(firstValue)
                .thenReturn(secondValue);
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setHasAvailableCredit(Boolean.TRUE);
        ApiResponse response = processCreditService.getRating("SystemId");
        assertNotNull(response);
        assertEquals(200, response.status());
    }
}

