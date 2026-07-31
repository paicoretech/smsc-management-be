package com.smsc.management.app.sip.service;

import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

class SipUssiAssignmentValidatorTest extends BaseIntegrationTest {

    @Autowired
    private SipUssiAssignmentValidator sipUssiAssignmentValidator;

    @MockBean
    private Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    private SipGatewaysRepository sipGatewaysRepository;

    private SipGatewaysService.UssiAssignmentValidationContext context(
            Integer currentAssignedSs7NetworkId,
            Integer targetSs7NetworkId,
            Integer currentSipNetworkId
    ) {
        return new SipGatewaysService.UssiAssignmentValidationContext(
                currentAssignedSs7NetworkId,
                targetSs7NetworkId,
                currentSipNetworkId
        );
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentAndTargetAreNull() {
        assertDoesNotThrow(() -> sipUssiAssignmentValidator.validate(context(null, null, 1)));
        Mockito.verifyNoInteractions(ss7GatewaysRepository, sipGatewaysRepository);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentAssignmentIsRunningAndTargetChanges() {
        Ss7Gateways currentSs7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(5)).thenReturn(currentSs7Gateway);
        Mockito.when(currentSs7Gateway.getEnabled()).thenReturn(Constants.ENABLED);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sipUssiAssignmentValidator.validate(context(5, 10, 1))
        );

        assertEquals(
                "The currently assigned SS7 gateway is running. Stop it before changing or removing the USSI association.",
                ex.getMessage()
        );

        Mockito.verify(ss7GatewaysRepository, Mockito.never()).findByNetworkId(10);
        Mockito.verifyNoInteractions(sipGatewaysRepository);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentAssignmentIsRunningAndTargetIsRemoved() {
        Ss7Gateways currentSs7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(5)).thenReturn(currentSs7Gateway);
        Mockito.when(currentSs7Gateway.getEnabled()).thenReturn(Constants.ENABLED);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sipUssiAssignmentValidator.validate(context(5, null, 1))
        );

        assertEquals(
                "The currently assigned SS7 gateway is running. Stop it before changing or removing the USSI association.",
                ex.getMessage()
        );

        Mockito.verifyNoInteractions(sipGatewaysRepository);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenSs7GatewayDoesNotExist() {
        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));

        assertEquals("Selected SS7 gateway does not exist.", ex.getMessage());
        Mockito.verify(sipGatewaysRepository, Mockito.never()).findFirstByRoutingUssiTrafficSs7GatewayId(anyInt());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenSs7GatewayIsDeleted() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DELETED_ENABLED_STATUS);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sipUssiAssignmentValidator.validate(context(null, 10, 1))
        );

        assertEquals("Selected SS7 gateway does not exist.", ex.getMessage());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenSs7GatewayDoesNotAllowUssi() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> sipUssiAssignmentValidator.validate(context(null, 10, 1))
        );

        assertEquals("Selected SS7 gateway does not allow USSI traffic.", ex.getMessage());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenSs7GatewayHasHomeRoutingEnabled() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(true);
        Mockito.when(ss7Gateway.isHomeRouting()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));
        assertEquals("Selected SS7 gateway cannot be used for USSI because Home Routing is enabled.", ex.getMessage());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenThereIsNoCurrentOwner() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(true);
        Mockito.when(ss7Gateway.isHomeRouting()).thenReturn(false);
        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(10)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentOwnerIsSameSipGateway() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);
        SipGateways currentOwner = Mockito.mock(SipGateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(true);
        Mockito.when(ss7Gateway.isHomeRouting()).thenReturn(false);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(10)).thenReturn(Optional.of(currentOwner));
        Mockito.when(currentOwner.getNetworkId()).thenReturn(1);

        assertDoesNotThrow(() -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentOwnerIsAnotherRunningSipGateway() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);
        SipGateways currentOwner = Mockito.mock(SipGateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(true);
        Mockito.when(ss7Gateway.isHomeRouting()).thenReturn(false);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(10)).thenReturn(Optional.of(currentOwner));
        Mockito.when(currentOwner.getNetworkId()).thenReturn(2);
        Mockito.when(currentOwner.getEnabled()).thenReturn(Constants.ENABLED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));
        assertEquals("Selected SS7 gateway is already assigned to a running SIP gateway. Stop that SIP gateway before reassigning it.", ex.getMessage());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void validateWhenCurrentOwnerIsAnotherStoppedSipGateway() {
        Ss7Gateways ss7Gateway = Mockito.mock(Ss7Gateways.class);
        SipGateways currentOwner = Mockito.mock(SipGateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(10)).thenReturn(ss7Gateway);
        Mockito.when(ss7Gateway.getEnabled()).thenReturn(Constants.DISABLED);
        Mockito.when(ss7Gateway.isAllowedUssi()).thenReturn(true);
        Mockito.when(ss7Gateway.isHomeRouting()).thenReturn(false);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(10)).thenReturn(Optional.of(currentOwner));
        Mockito.when(currentOwner.getNetworkId()).thenReturn(2);
        Mockito.when(currentOwner.getEnabled()).thenReturn(Constants.DISABLED);

        assertDoesNotThrow(() -> sipUssiAssignmentValidator.validate(context(null, 10, 1)));
    }
}