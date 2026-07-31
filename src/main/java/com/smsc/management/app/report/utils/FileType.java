package com.smsc.management.app.report.utils;

import lombok.Getter;

@Getter
public enum FileType {
    BROADCAST("broadcast"),
    LOGS("logs"),
    CDRS("cdrs"),
    SUMMARY_CDR_ACCOUNT("delivery_rate_by_account"),
    SUMMARY_CDR_ACCOUNT_TRAFFIC("traffic_by_account"),
    SUMMARY_CDR_BROADCAST("delivery_rate_by_campaign"),
    SUMMARY_CDR_DESTINATION("delivery_rate_by_destination"),
    SUMMARY_CDR_DESTINATION_TRAFFIC("traffic_by_destination"),
    SUMMARY_CDR_USER_AND_SENDER_TRAFFIC("traffic_by_user_and_by_sender"),
    CDRS_DETAILED_REPORT("detailed_report");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name();
    }
}
