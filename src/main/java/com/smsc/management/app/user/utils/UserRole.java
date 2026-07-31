package com.smsc.management.app.user.utils;

import lombok.Getter;

@Getter
public enum UserRole {
    ROOT("ROOT"),
    ADMINISTRATOR("Administrator"),
    CAMPAIGN_OPERATOR("Campaign Operator"),
    CAMPAIGN_APPROVER("Campaign Approver"),
    TECH_SUPPORT("Technical Support"),
    ACTIONS_ADVANCED("Actions Advanced");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }
}
