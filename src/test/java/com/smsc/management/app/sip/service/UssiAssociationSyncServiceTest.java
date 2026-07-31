package com.smsc.management.app.sip.service;

import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

class UssiAssociationSyncServiceTest extends BaseIntegrationTest {

    @Autowired
    private UssiAssociationSyncService ussiAssociationSyncService;

    @MockBean
    private SipGatewaysRepository sipGatewaysRepository;

    @MockBean
    private Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    private ObjectSs7Service objectSs7Service;

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void releasePreviousStoppedOwnerIfNeededWhenTargetIsNull() {
        assertDoesNotThrow(() -> ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(null, 1));
        Mockito.verifyNoInteractions(sipGatewaysRepository);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void releasePreviousStoppedOwnerIfNeededWhenPreviousOwnerDoesNotExist() {
        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayIdAndNetworkIdNot(10, 1)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(10, 1));
        Mockito.verify(sipGatewaysRepository, Mockito.never()).save(any());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void releasePreviousStoppedOwnerIfNeededWhenPreviousOwnerIsRunning() {
        SipGateways previousOwner = Mockito.mock(SipGateways.class);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayIdAndNetworkIdNot(10, 1)).thenReturn(Optional.of(previousOwner));
        Mockito.when(previousOwner.getEnabled()).thenReturn(Constants.ENABLED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(10, 1));
        assertEquals("The selected SS7 gateway is already assigned to a running SIP gateway. Stop that SIP gateway before reassigning it.", ex.getMessage());

        Mockito.verify(previousOwner, Mockito.never()).setRoutingUssiTrafficSs7GatewayId(null);
        Mockito.verify(sipGatewaysRepository, Mockito.never()).save(previousOwner);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void releasePreviousStoppedOwnerIfNeededWhenPreviousOwnerIsStopped() {
        SipGateways previousOwner = Mockito.mock(SipGateways.class);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayIdAndNetworkIdNot(10, 1)).thenReturn(Optional.of(previousOwner));
        Mockito.when(previousOwner.getEnabled()).thenReturn(Constants.DISABLED);

        assertDoesNotThrow(() -> ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(10, 1));

        Mockito.verify(previousOwner).setRoutingUssiTrafficSs7GatewayId(null);
        Mockito.verify(sipGatewaysRepository).save(previousOwner);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void syncRedisAfterAssignmentUpdateWhenIdsAreEqual() throws Exception {
        ussiAssociationSyncService.syncRedisAfterAssignmentUpdate(10, 10);
        Mockito.verifyNoInteractions(ss7GatewaysRepository, objectSs7Service);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void syncRedisAfterAssignmentUpdateRefreshesOnlyEnabledAssociations() throws Exception {
        Ss7Gateways previousSs7 = Mockito.mock(Ss7Gateways.class);
        Ss7Gateways newSs7 = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(previousSs7);
        Mockito.when(ss7GatewaysRepository.findByNetworkId(20)).thenReturn(newSs7);

        Mockito.when(previousSs7.getEnabled()).thenReturn(Constants.ENABLED);
        Mockito.when(newSs7.getEnabled()).thenReturn(Constants.DISABLED);

        Mockito.doNothing().when(objectSs7Service).updateSs7SettingsInRedis(10);

        ussiAssociationSyncService.syncRedisAfterAssignmentUpdate(10, 20);

        Mockito.verify(objectSs7Service).updateSs7SettingsInRedis(10);
        Mockito.verify(objectSs7Service, Mockito.never()).updateSs7SettingsInRedis(20);
        Mockito.verify(objectSs7Service).sendSs7SettingsUpdateNotification(10);
        Mockito.verify(objectSs7Service, Mockito.never()).sendSs7SettingsUpdateNotification(20);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void syncRedisAfterAssignmentUpdateSkipsDeletedSs7Gateway() throws Exception {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DELETED_ENABLED_STATUS);

        ussiAssociationSyncService.syncRedisAfterAssignmentUpdate(10, null);

        Mockito.verify(objectSs7Service, Mockito.never()).updateSs7SettingsInRedis(anyInt());
        Mockito.verify(objectSs7Service, Mockito.never()).sendSs7SettingsUpdateNotification(anyInt());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void syncRedisAfterAssignmentUpdateSkipsMissingSs7Gateway() throws Exception {
        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(null);

        ussiAssociationSyncService.syncRedisAfterAssignmentUpdate(10, null);

        Mockito.verify(objectSs7Service, Mockito.never()).updateSs7SettingsInRedis(anyInt());
        Mockito.verify(objectSs7Service, Mockito.never()).sendSs7SettingsUpdateNotification(anyInt());
    }
}