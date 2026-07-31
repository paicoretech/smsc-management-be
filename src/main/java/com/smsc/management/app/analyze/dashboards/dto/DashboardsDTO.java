package com.smsc.management.app.analyze.dashboards.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardsDTO {
    private long total;

    @JsonProperty("sms_failed")
    private long smsFailed;

    @JsonProperty("sms_delivery")
    private long smsDelivery;

    @JsonProperty("sms_failed_rate")
    private double smsFailedRate;

    @JsonProperty("sms_delivery_rate")
    private double smsDeliveryRate;

    private List<Map<String, Object>> data;
}
