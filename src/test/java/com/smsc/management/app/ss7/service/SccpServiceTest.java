package com.smsc.management.app.ss7.service;

import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SccpServiceTest extends BaseIntegrationTest {
    @Autowired
    private SccpService sccpService;

    @Test
    void createGtIndicator() {
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", -1, 0, 0, -1, 0, 0));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 20, 0, 0, -1, 0, 0));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 80, 0, 0, -1, 0, 0));

        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 2, 0, 0, 0, 0, 0));

        String gtIndicator = sccpService.createGtIndicator("", 0, 0, 0, 0, 0, 0);
        Assertions.assertEquals("GT0000", gtIndicator);

        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 4, 0, 0, -1, 0, 0));
        gtIndicator = sccpService.createGtIndicator("", 4, 0, 0, 0, 0, 0);
        Assertions.assertEquals("GT0001", gtIndicator);

        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 8, 0, 0, 0, 0, -1));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 8, 0, 0, 0, 0, 256));
        gtIndicator = sccpService.createGtIndicator("", 8, 0, 0, 0, 0, 0);
        Assertions.assertEquals("GT0010", gtIndicator);

        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 12, 0, 0, 0, 0, -1));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 12, 0, 0, 0, 0, 256));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 12, 0, 0, 0, -1, 1));
        gtIndicator = sccpService.createGtIndicator("", 12, 0, 0, 0, 0, 0);
        Assertions.assertEquals("GT0011", gtIndicator);

        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 16, 0, 0, 0, 0, -1));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 16, 0, 0, 0, 0, 256));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 16, 0, 0, 0, -1, 1));
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.createGtIndicator("", 16, 0, 0, -1, 1, 1));
        gtIndicator = sccpService.createGtIndicator("", 16, 0, 0, 0, 0, 0);
        Assertions.assertEquals("GT0100", gtIndicator);
    }

    @Test
    void validateAddressRulesTest() {
        SccpRulesDTO sccpRulesMock = new SccpRulesDTO();
        sccpRulesMock.setPrimaryAddressId(null);
        sccpRulesMock.setSecondaryAddressId(null);
        Assertions.assertThrows(SmscBackendException.class, () -> sccpService.validateAddressRules(sccpRulesMock));
    }
}