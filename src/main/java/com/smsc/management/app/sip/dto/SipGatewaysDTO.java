package com.smsc.management.app.sip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.utils.CustomStringDeserializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SipGatewaysDTO {
    @JsonProperty("network_id")
    private int networkId;

    @JsonProperty("ip_address")
    @NotBlank
    private String ipAddress;

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("external_id")
    @JsonDeserialize(using = CustomStringDeserializer.class)
    private String externalId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("protocol")
    private String protocol = "SIP";

    @JsonProperty("port")
    @Min(1) @Max(65535)
    private int port;

    @JsonProperty("transport")
    @NotBlank
    private String transport;

    @JsonProperty("messages_per_second")
    private int messagesPerSecond;

    @JsonProperty("messages_per_second_high")
    @Min(0)
    private int messagesPerSecondHigh;

    @JsonProperty("messages_per_second_medium")
    @Min(0)
    private int messagesPerSecondMedium;

    @JsonProperty("messages_per_second_low")
    @Min(0)
    private int messagesPerSecondLow;

    @JsonProperty("transaction_timeout")
    @Min(1)
    private int transactionTimeout = 32000;

    @JsonProperty("retransmission_base_interval_ms")
    @Min(1)
    private int retransmissionBaseIntervalMs = 500;

    @JsonProperty("retransmission_max_interval_ms")
    @Min(1)
    private int retransmissionMaxIntervalMs = 4000;

    @JsonProperty("network_timeout_ms")
    @Min(1)
    private int networkTimeoutMs = 5000;

    @JsonProperty("thread_pool_size")
    @Min(1)
    private int threadPoolSize = 8;

    @JsonProperty("retransmission_filter")
    private boolean retransmissionFilter = true;

    @JsonProperty("max_message_size")
    @Min(1)
    private int maxMessageSize = 1048576;

    @JsonProperty("receive_udp_buffer_size")
    @Min(1)
    private int receiveUdpBufferSize = 8388608;

    @JsonProperty("send_udp_buffer_size")
    @Min(1)
    private int sendUdpBufferSize = 8388608;

    @JsonProperty("aggressive_cleanup")
    private boolean aggressiveCleanup = true;

    @JsonProperty("routing_enable_ss7")
    private boolean routingEnableSs7;

    @JsonProperty("routing_enable_diameter")
    private boolean routingEnableDiameter;

    @JsonProperty("routing_registration_traffic_ss7_gateway_id")
    private Integer routingRegistrationTrafficSs7GatewayId;

    @JsonProperty("routing_registration_traffic_diameter_gateway_id")
    private Integer routingRegistrationTrafficDiameterGatewayId;

    @JsonProperty("routing_ussi_traffic_ss7_gateway_id")
    private Integer routingUssiTrafficSs7GatewayId;

    @JsonProperty("enabled")
    private int enabled;

    @JsonProperty("register_max_expires")
    @Min(1)
    private Integer registerMaxExpires;

    @JsonProperty("ipsmgw_user")
    private String ipsmgwUser;

    @JsonProperty("ipsmgw_domain")
    private String ipsmgwDomain;

    @JsonProperty("ims_domain")
    private String imsDomain;

    @JsonProperty("ims_ccf")
    private String imsCcf;

    @JsonProperty("ims_ecf")
    private String imsEcf;

    @JsonProperty("subscribe_target_host")
    private String subscribeTargetHost;

    @JsonProperty("subscribe_target_port")
    @Min(1) @Max(65535)
    private Integer subscribeTargetPort;

    @JsonProperty("subscribe_target_transport")
    private String subscribeTargetTransport;

    @JsonProperty("local_via_host")
    private String localViaHost;

    @JsonProperty("auto_retry_error_code")
    private String autoRetryErrorCode;

    @JsonProperty("no_retry_error_code")
    private String noRetryErrorCode;

    @JsonProperty("retry_alternate_destination_error_code")
    private String retryAlternateDestinationErrorCode;

    @JsonProperty("global_title")
    @Pattern(regexp = "^\\d*$", message = "The global title must contain only numbers.")
    private String globalTitle;

    @JsonProperty("mno_id")
    private int mnoId;

    @JsonProperty("ussi_default_datacoding_id")
    private Integer ussiDefaultDatacodingId;

    @JsonProperty("split_message")
    private boolean splitMessage;

    public void setMessagesPerSecond() {
        this.messagesPerSecond = this.messagesPerSecondHigh + this.messagesPerSecondMedium + this.messagesPerSecondLow;
    }
}
