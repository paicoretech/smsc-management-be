package com.smsc.management.app.sip.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;

class ObjectSipServiceTest extends BaseIntegrationTest {

    @Autowired
    private ObjectSipService objectSipService;

    @MockBean
    private SipGatewaysRepository sipGatewaysRepository;

    @MockBean
    private UtilsBase utilsBase;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void refreshSipGatewaysInRedis() {

            SipGateways s = Mockito.mock(SipGateways.class);
            Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(s);
            Mockito.when(s.getEnabled()).thenReturn(Constants.ACTIVE_ENABLED_STATUS);
            Mockito.when(s.getNetworkId()).thenReturn(1);
            Mockito.when(s.getIpAddress()).thenReturn("10.0.0.1");
            Mockito.when(s.getPort()).thenReturn(5060);
            Mockito.when(s.getTransport()).thenReturn("UDP");
            Mockito.when(s.getGlobalTitle()).thenReturn("12345");
            Mockito.when(s.getMessagesPerSecond()).thenReturn(100);
            Mockito.when(s.getMessagesPerSecondHigh()).thenReturn(70);
            Mockito.when(s.getMessagesPerSecondMedium()).thenReturn(20);
            Mockito.when(s.getMessagesPerSecondLow()).thenReturn(10);

            Mockito.when(s.getTransactionTimeout()).thenReturn(32000);
            Mockito.when(s.getRetransmissionBaseIntervalMs()).thenReturn(500);
            Mockito.when(s.getRetransmissionMaxIntervalMs()).thenReturn(4000);
            Mockito.when(s.getNetworkTimeoutMs()).thenReturn(10000);

            Mockito.when(s.getThreadPoolSize()).thenReturn(8);
            Mockito.when(s.isRetransmissionFilter()).thenReturn(true);
            Mockito.when(s.getMaxMessageSize()).thenReturn(4096);

            Mockito.when(s.isRoutingEnableSs7()).thenReturn(true);
            Mockito.when(s.isRoutingEnableDiameter()).thenReturn(false);
            Mockito.when(s.getRoutingRegistrationTrafficSs7GatewayId()).thenReturn(10);
            Mockito.when(s.getRoutingRegistrationTrafficDiameterGatewayId()).thenReturn(null);
            Mockito.when(s.getRoutingUssiTrafficSs7GatewayId()).thenReturn(99);
            Mockito.when(s.getAutoRetryErrorCode()).thenReturn("408,480,500,503");
            Mockito.when(s.getNoRetryErrorCode()).thenReturn("400,403,404");
            Mockito.when(s.getRetryAlternateDestinationErrorCode()).thenReturn("408,503");

            Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

            ApiResponse response = objectSipService.refreshSipGatewaysInRedis(1);

            assertNotNull(response);
            assertEquals(200, response.status());
            assertEquals("success", response.message());

            Mockito.verify(utilsBase, Mockito.times(1)).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    anyString()
            );
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void refreshSipGatewaysInRedisReturnError() {
        Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(null);

        ApiResponse response = objectSipService.refreshSipGatewaysInRedis(1);

        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void refreshSipGatewaysInRedisDeletedGateways() {
        SipGateways s = Mockito.mock(SipGateways.class);

        Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(s);
        Mockito.when(s.getEnabled()).thenReturn(Constants.DELETED_ENABLED_STATUS);

        ApiResponse response = objectSipService.refreshSipGatewaysInRedis(1);

        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());

        Mockito.verify(utilsBase, Mockito.never()).storeInRedis(anyString(), anyString(), anyString());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateOrCreateJsonInRedis() throws Exception {
        SipGateways s = Mockito.mock(SipGateways.class);

            Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(s);
            Mockito.when(s.getEnabled()).thenReturn(Constants.ACTIVE_ENABLED_STATUS);

            Mockito.when(s.getNetworkId()).thenReturn(1);
            Mockito.when(s.getIpAddress()).thenReturn("10.0.0.1");
            Mockito.when(s.getPort()).thenReturn(5060);
            Mockito.when(s.getTransport()).thenReturn("UDP");
            Mockito.when(s.getMessagesPerSecond()).thenReturn(100);
            Mockito.when(s.getMessagesPerSecondHigh()).thenReturn(70);
            Mockito.when(s.getMessagesPerSecondMedium()).thenReturn(20);
            Mockito.when(s.getMessagesPerSecondLow()).thenReturn(10);

            Mockito.when(s.getTransactionTimeout()).thenReturn(32000);
            Mockito.when(s.getRetransmissionBaseIntervalMs()).thenReturn(500);
            Mockito.when(s.getRetransmissionMaxIntervalMs()).thenReturn(4000);
            Mockito.when(s.getNetworkTimeoutMs()).thenReturn(10000);

            Mockito.when(s.getThreadPoolSize()).thenReturn(8);
            Mockito.when(s.isRetransmissionFilter()).thenReturn(true);
            Mockito.when(s.getMaxMessageSize()).thenReturn(4096);

            Mockito.when(s.isRoutingEnableSs7()).thenReturn(true);
            Mockito.when(s.isRoutingEnableDiameter()).thenReturn(false);
            Mockito.when(s.getRoutingRegistrationTrafficSs7GatewayId()).thenReturn(10);
            Mockito.when(s.getRoutingRegistrationTrafficDiameterGatewayId()).thenReturn(null);
            Mockito.when(s.getRoutingUssiTrafficSs7GatewayId()).thenReturn(99);
            Mockito.when(s.getAutoRetryErrorCode()).thenReturn("408,480,500,503");
            Mockito.when(s.getNoRetryErrorCode()).thenReturn("400,403,404");
            Mockito.when(s.getRetryAlternateDestinationErrorCode()).thenReturn("408,503");

            Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

            objectSipService.updateOrCreateJsonInRedis(1);

            Mockito.verify(utilsBase, Mockito.times(1)).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    anyString()
            );
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateOrCreateJsonInRedisIncludesRetrySettings() throws Exception {
        SipGateways s = Mockito.mock(SipGateways.class);

            Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(s);
            Mockito.when(s.getEnabled()).thenReturn(Constants.ACTIVE_ENABLED_STATUS);
            Mockito.when(s.getNetworkId()).thenReturn(1);
            Mockito.when(s.getMessagesPerSecond()).thenReturn(100);
            Mockito.when(s.getMessagesPerSecondHigh()).thenReturn(70);
            Mockito.when(s.getMessagesPerSecondMedium()).thenReturn(20);
            Mockito.when(s.getMessagesPerSecondLow()).thenReturn(10);
            Mockito.when(s.getAutoRetryErrorCode()).thenReturn("408,480,500,503");
            Mockito.when(s.getNoRetryErrorCode()).thenReturn("400,403,404");
            Mockito.when(s.getRetryAlternateDestinationErrorCode()).thenReturn("408,503");

            Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

            objectSipService.updateOrCreateJsonInRedis(1);

            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"auto_retry_error_code\":\"408,480,500,503\"")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"no_retry_error_code\":\"400,403,404\"")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"retry_alternate_destination_error_code\":\"408,503\"")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"messages_per_second\":100")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"messages_per_second_high\":70")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"messages_per_second_medium\":20")
            );
            Mockito.verify(utilsBase).storeInRedis(
                    eq(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME),
                    eq("1"),
                    contains("\"messages_per_second_low\":10")
            );
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateOrCreateJsonInRedisWhenGatewayDeleted() {
        SipGateways s = Mockito.mock(SipGateways.class);

        Mockito.when(sipGatewaysRepository.findByNetworkId(anyInt())).thenReturn(s);
        Mockito.when(s.getEnabled()).thenReturn(Constants.DELETED_ENABLED_STATUS);
        assertThrows(SmscBackendException.class, () -> objectSipService.updateOrCreateJsonInRedis(1));
        Mockito.verify(utilsBase, Mockito.never()).storeInRedis(anyString(), anyString(), anyString());
    }
}
