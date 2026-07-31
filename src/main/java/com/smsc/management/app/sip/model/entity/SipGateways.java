package com.smsc.management.app.sip.model.entity;

import com.smsc.management.app.catalog.model.entity.EncodingType;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.utils.EntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sip_gateways")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SipGateways extends EntityBase {

    @Id
    @Column(name = "network_id")
    private int networkId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", insertable = false, updatable = false)
    private SequenceNetworksId sequenceNetworksId;

    @Column(name = "name", columnDefinition = "text NOT NULL")
    private String name;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "status", columnDefinition = "text default 'STARTED'")
    private String status;

    @Column(name = "protocol", columnDefinition = "text default 'SIP'")
    private String protocol;

    @Column(name = "ip_address", columnDefinition = "text NOT NULL")
    private String ipAddress;

    @Column(name = "port", columnDefinition = "integer NOT NULL")
    private int port;

    @Column(name = "transport", columnDefinition = "text NOT NULL")
    private String transport; // udp/tcp/tls

    @Column(name = "messages_per_second", columnDefinition = "integer default 0")
    private Integer messagesPerSecond;

    @Column(name = "messages_per_second_high", columnDefinition = "integer default 0")
    private Integer messagesPerSecondHigh;

    @Column(name = "messages_per_second_medium", columnDefinition = "integer default 0")
    private Integer messagesPerSecondMedium;

    @Column(name = "messages_per_second_low", columnDefinition = "integer default 0")
    private Integer messagesPerSecondLow;

    @Column(name="mno_id", columnDefinition = "int Not null")
    private int mnoId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="mno_id", insertable=false, updatable=false)
    private OperatorMno operatorMnoId;

    @Column(name = "transaction_timeout", columnDefinition = "integer NOT NULL default 32000")
    private int transactionTimeout;

    @Column(name = "retransmission_base_interval_ms", columnDefinition = "integer NOT NULL default 500")
    private int retransmissionBaseIntervalMs;

    @Column(name = "retransmission_max_interval_ms", columnDefinition = "integer NOT NULL default 4000")
    private int retransmissionMaxIntervalMs;

    @Column(name = "network_timeout_ms", columnDefinition = "integer NOT NULL default 5000")
    private int networkTimeoutMs;

    @Column(name = "thread_pool_size", columnDefinition = "integer NOT NULL default 8")
    private int threadPoolSize;

    @Column(name = "retransmission_filter", columnDefinition = "boolean NOT NULL default true")
    private boolean retransmissionFilter;

    @Column(name = "max_message_size", columnDefinition = "integer NOT NULL default 1048576")
    private int maxMessageSize;

    @Column(name = "receive_udp_buffer_size", columnDefinition = "integer NOT NULL default 8388608")
    private int receiveUdpBufferSize;

    @Column(name = "send_udp_buffer_size", columnDefinition = "integer NOT NULL default 8388608")
    private int sendUdpBufferSize;

    @Column(name = "aggressive_cleanup", columnDefinition = "boolean NOT NULL default true")
    private boolean aggressiveCleanup;

    @Column(name = "routing_enable_ss7", columnDefinition = "boolean NOT NULL default false")
    private boolean routingEnableSs7;

    @Column(name = "routing_enable_diameter", columnDefinition = "boolean NOT NULL default false")
    private boolean routingEnableDiameter;

    @Column(name = "routing_registration_traffic_ss7_gateway_id", columnDefinition = "int")
    private Integer routingRegistrationTrafficSs7GatewayId;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "routing_registration_traffic_ss7_gateway_id",
            referencedColumnName = "network_id",
            insertable = false,
            updatable = false
    )
    private Ss7Gateways routingRegistrationTrafficSs7Gateway;

    @Column(name = "routing_registration_traffic_diameter_gateway_id", columnDefinition = "int")
    private Integer routingRegistrationTrafficDiameterGatewayId;

    @Column(name = "routing_ussi_traffic_ss7_gateway_id", columnDefinition = "int")
    private Integer routingUssiTrafficSs7GatewayId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "routing_ussi_traffic_ss7_gateway_id",
            referencedColumnName = "network_id",
            insertable = false,
            updatable = false
    )
    private Ss7Gateways routingUssiTrafficSs7Gateway;

    @Column(columnDefinition = "int DEFAULT 1")
    private int enabled;

    @Column(name = "register_max_expires", columnDefinition = "integer")
    private Integer registerMaxExpires;

    @Column(name = "ipsmgw_user", columnDefinition = "text")
    private String ipsmgwUser;

    @Column(name = "ipsmgw_domain", columnDefinition = "text")
    private String ipsmgwDomain;

    @Column(name = "ims_domain", columnDefinition = "text")
    private String imsDomain;

    @Column(name = "ims_ccf", columnDefinition = "text")
    private String imsCcf;

    @Column(name = "ims_ecf", columnDefinition = "text")
    private String imsEcf;

    @Column(name = "subscribe_target_host", columnDefinition = "text")
    private String subscribeTargetHost;

    @Column(name = "subscribe_target_port", columnDefinition = "integer")
    private Integer subscribeTargetPort;

    @Column(name = "subscribe_target_transport", columnDefinition = "text")
    private String subscribeTargetTransport;

    @Column(name = "local_via_host", columnDefinition = "text")
    private String localViaHost;

    @Column(name = "auto_retry_error_code", columnDefinition = "text default ''")
    private String autoRetryErrorCode;

    @Column(name = "no_retry_error_code", columnDefinition = "text")
    private String noRetryErrorCode;

    @Column(name = "retry_alternate_destination_error_code", columnDefinition = "text")
    private String retryAlternateDestinationErrorCode;

    @Column(name = "global_title", columnDefinition = "text default ''")
    private String globalTitle;

    @Column(name = "ussi_default_datacoding_id", columnDefinition = "int")
    private Integer ussiDefaultDatacodingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ussi_default_datacoding_id", referencedColumnName = "id", insertable = false, updatable = false)
    private EncodingType ussiDefaultDatacoding;

    @Column(name = "split_message", columnDefinition = "boolean default false")
    private boolean splitMessage;

}
