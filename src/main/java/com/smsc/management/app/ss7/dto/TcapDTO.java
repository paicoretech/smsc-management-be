package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.app.ss7.validator.ValidTimeouts;
import com.smsc.management.utils.CustomStringDeserializer;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ValidTimeouts
public class TcapDTO {
    private int id;

    @JsonProperty("network_id")
    private int  networkId;

    @JsonProperty("external_id")
    @JsonDeserialize(using = CustomStringDeserializer.class)
    private String externalId;

    @JsonProperty("ssn_list")
    private String ssnList;

    @JsonProperty("preview_mode")
    private boolean previewMode;

    @JsonProperty("dialog_idle_timeout")
    private int dialogIdleTimeout;

    @JsonProperty("invoke_timeout")
    private int invokeTimeout;

    @JsonProperty("dialog_id_range_start")
    private int dialogIdRangeStart;

    @JsonProperty("dialog_id_range_end")
    private int dialogIdRangeEnd;

    @JsonProperty("max_dialogs")
    private int maxDialogs;

    @JsonProperty("do_not_send_protocol_version")
    private boolean doNotSendProtocolVersion;

    @JsonProperty("swap_tcap_id_enabled")
    private boolean swapTcapIdEnabled;

    @JsonProperty("sls_range_id")
    private String slsRangeId;

    @JsonProperty("exr_delay_thr1")
    private float exrDelayThr1;

    @JsonProperty("exr_delay_thr2")
    private float exrDelayThr2;

    @JsonProperty("exr_delay_thr3")
    private float exrDelayThr3;

    @JsonProperty("exr_back_to_normal_delay_thr1")
    private float exrBackToNormalDelayThr1;

    @JsonProperty("exr_back_to_normal_delay_thr2")
    private float exrBackToNormalDelayThr2;

    @JsonProperty("exr_back_to_normal_delay_thr3")
    private float exrBackToNormalDelayThr3;

    @JsonProperty("memory_monitor_thr1")
    private float memoryMonitorThr1;

    @JsonProperty("memory_monitor_thr2")
    private float memoryMonitorThr2;

    @JsonProperty("memory_monitor_thr3")
    private float memoryMonitorThr3;

    @JsonProperty("mem_back_to_normal_delay_thr1")
    private float memBackToNormalDelayThr1;

    @JsonProperty("mem_back_to_normal_delay_thr2")
    private float memBackToNormalDelayThr2;

    @JsonProperty("mem_back_to_normal_delay_thr3")
    private float memBackToNormalDelayThr3;

    @JsonProperty("blocking_incoming_tcap_messages")
    private boolean blockingIncomingTcapMessages;
}
