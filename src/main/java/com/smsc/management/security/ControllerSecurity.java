package com.smsc.management.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ControllerSecurity {
    @PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
    public void checkAdminRoles() {
        // noop
    }

    @PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR', 'CAMPAIGN_APPROVER', 'CAMPAIGN_OPERATOR', 'TECH_SUPPORT')")
    public void checkCommonRoles() {
        // noop
    }

    @PreAuthorize("hasAnyRole('ROOT', 'CAMPAIGN_APPROVER', 'CAMPAIGN_OPERATOR')")
    public void checkOperatorRoles() {
        // noop
    }

    @PreAuthorize("hasAnyRole('ROOT', 'CAMPAIGN_APPROVER')")
    public void checkApproverRoles() {
        // noop
    }

    @PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR', 'TECH_SUPPORT')")
    public void checkSupportRoles() {
        // noop
    }
}
