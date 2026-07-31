package com.smsc.management.app.gateway.model.entity;

import com.smsc.management.app.catalog.model.entity.BindStatuses;
import com.smsc.management.app.catalog.model.entity.BindsTypes;
import com.smsc.management.app.catalog.model.entity.EncodingType;
import com.smsc.management.app.catalog.model.entity.InterfazVersions;
import com.smsc.management.app.catalog.model.entity.NpiCatalog;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.catalog.model.entity.TonCatalog;
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

/*
 * The sequence will be assigned only even numbers, it is updated in the DataInitializer.java
 */
@Entity
@Table(name = "gateways")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Gateways extends EntityBase {
	@Id
	@Column(name="network_id")
	private int networkId;
	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private SequenceNetworksId sequenceNetworksId;

	@Column(name = "external_id", unique = true)
	private String externalId;

	@Column(name="name",columnDefinition = "text NOT NULL")
	private String name;
	
	@Column(name="system_id",columnDefinition = "text NOT NULL")
	private String systemId;
	
	@Column(name="password",columnDefinition = "text NOT NULL")
	private String password;
	
	@Column(name="ip",columnDefinition = "text NOT NULL")
	private String ip;
	
	@Column(name="port",columnDefinition = "int NOT NULL")
	private int port;

	@Column(name="bind_type",columnDefinition = "text NOT NULL")
	private String bindType;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="bind_type", insertable=false, updatable=false)
	private BindsTypes bindsTypes;
	
	@Column(name="system_type",columnDefinition = "text NOT NULL")
	private String systemType;
	
	@Column(name="interface_version",columnDefinition = "text NOT NULL DEFAULT 'IF_34'")
	private String interfaceVersion;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="interface_version", insertable=false, updatable=false)
	private InterfazVersions interfazVersion;
	
	@Column(name="sessions_number", columnDefinition = "int NOT NULL DEFAULT 1")
	private int sessionsNumber;
		
	@Column(name = "address_ton", columnDefinition = "int NOT NULL DEFAULT 0")
	private int addressTon;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="address_ton", insertable=false, updatable=false)
	private TonCatalog tonCatalog;
	
	@Column(name = "address_npi",columnDefinition = "int NOT NULL DEFAULT 0")
	private int addressNpi;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="address_npi", insertable=false, updatable=false)
	private NpiCatalog npiCatalog;
	
	@Column(name="address_range", columnDefinition = "text NOT NULL DEFAULT '^[0-9a-zA-Z]*'")
	private String addressRange;
	
	@Column(columnDefinition = "int DEFAULT 0")
	private int enabled; // account state
	
	@Column(name="enquire_link_period", columnDefinition = "int DEFAULT 30000")
	private int enquireLinkPeriod;
	
	@Column(name="request_dlr", columnDefinition = "int DEFAULT 2")
	private int requestDlr;
	
	@Column(name="no_retry_error_code")
	private String noRetryErrorCode;
	
	@Column(name="retry_alternate_destination_error_code")
	private String retryAlternateDestinationErrorCode;
	
	@Column(name="bind_timeout", columnDefinition = "int DEFAULT 5000")
	private int bindTimeout;
	
	@Column(name="bind_retry_period", columnDefinition = "int DEFAULT 10000")
	private int bindRetryPeriod;
	
	@Column(name="pdu_timeout", columnDefinition = "int DEFAULT 5000")
	private int pduTimeout;
	
	@Column(name="pdu_degree", columnDefinition = "int DEFAULT 1 not null")
	private int pduDegree;
	
	@Column(name="thread_pool_size", columnDefinition = "int DEFAULT 100 not null")
	private int threadPoolSize;
	
	@Column(name="mno_id", columnDefinition = "int Not null")
	private int mnoId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="mno_id", insertable=false, updatable=false)
	private OperatorMno operatorMnoId;
	
	@Column(name="status", columnDefinition = "text DEFAULT 'CLOSED'")
	private String status;
	
	//realtime columns
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="status", insertable=false, updatable=false)
	private BindStatuses bindStatus;
	
	@Column(name="active_sessions_numbers")
	private int activeSessionsNumbers;
	
	@Column(name="protocol")
	private String protocol;
	
	@Column(name="auto_retry_error_code", columnDefinition = "text DEFAULT ''")
	private String autoRetryErrorCode;

	@Column(name = "encoding_iso88591")
	private int encodingIso88591 = 3;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_iso88591", insertable=false, updatable=false)
	private EncodingType encodingTypeIso88591;

	@Column(name = "encoding_gsm7")
	private int encodingGsm7 = 0;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_gsm7", insertable=false, updatable=false)
	private EncodingType encodingType;

	@Column(name = "encoding_ucs2")
	private int encodingUcs2 = 2;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_ucs2", insertable=false, updatable=false)
	private EncodingType encodingTypeUcs2;
	
	@Column(name = "split_message", columnDefinition = "boolean default false")
	private boolean splitMessage;
	
	@Column(name = "split_smpp_type", columnDefinition = "text default 'TLV'")
	private String splitSmppType;

	@Column(name = "authentication_types")
	private String authenticationTypes;

	@Column(name = "header_security_name")
	private String headerSecurityName;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "passwd")
	private String passwd;

	@Column(name = "token")
	private String token;

	@Column(name = "messages_per_second_high", columnDefinition = "integer default 0")
	private Integer messagesPerSecondHigh;

	@Column(name = "messages_per_second_medium", columnDefinition = "integer default 0")
	private Integer messagesPerSecondMedium;

	@Column(name = "messages_per_second_low", columnDefinition = "integer default 0")
	private Integer messagesPerSecondLow;

	@Column(name = "messages_per_second", columnDefinition = "integer default 0")
	private Integer messagesPerSecond;

	@Column(name = "tls_enabled", columnDefinition = "boolean NOT NULL DEFAULT false")
	private boolean tlsEnabled;
}							
