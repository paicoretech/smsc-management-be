package com.smsc.management.app.ss7.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.sequence.SequenceNetworksId;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "ss7_gateways")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Ss7Gateways extends EntityBase {
	@Id
	@Column(name="network_id")
	private int networkId;
	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private SequenceNetworksId sequenceNetworksId;

	@Column(name = "external_id", unique = true)
	private String externalId;
	
	@Column(name="name", columnDefinition = "text NOT NULL", unique = true)
	private String name;
	
	@Column(columnDefinition = "int DEFAULT 1")
	private int enabled; // account state
	
	@Column(name="status", columnDefinition = "text default 'STARTED'")
	private String status;
	
	@Column(name="protocol", columnDefinition = "text default 'SS7'")
	private String protocol;
	
	@Column(name="mno_id", columnDefinition = "int Not null")
	private int mnoId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="mno_id", insertable=false, updatable=false)
	private OperatorMno operatorMnoId;
	
	@Column(name = "global_title", columnDefinition = "text default ''")
	private String globalTitle;
	
	@Column(name = "global_title_indicator", columnDefinition = "text default '0100'")
	private String globalTitleIndicator;

	@Column(name = "messages_per_second_high", columnDefinition = "integer default 0")
	private Integer messagesPerSecondHigh;

	@Column(name = "messages_per_second_medium", columnDefinition = "integer default 0")
	private Integer messagesPerSecondMedium;

	@Column(name = "messages_per_second_low", columnDefinition = "integer default 0")
	private Integer messagesPerSecondLow;

	@Column(name = "messages_per_second", columnDefinition = "integer default 0")
	private Integer messagesPerSecond;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="global_title_indicator", insertable=false, updatable=false)
	private GlobalTitleIndicator gtIndicators;
	
	@Column(name = "translation_type", columnDefinition = "integer default 0")
	private int translationType;
	
	@Column(name = "smsc_ssn", columnDefinition = "integer default 8")
	private int smscSsn;
	
	@Column(name = "hlr_ssn", columnDefinition = "integer default 6")
	private int hlrSsn;
	
	@Column(name = "msc_ssn", columnDefinition = "integer default 8")
	private int mscSsn;
	
	@Column(name = "map_version", columnDefinition = "integer default 3")
	private int mapVersion;
	
	@Column(name = "split_message", columnDefinition = "boolean default false")
	private boolean splitMessage;

    @Column(name = "api_enabled", columnDefinition = "boolean default false", nullable = false)
    private boolean apiEnabled;

    @Column(name = "app_token", columnDefinition = "text")
    private String appToken;

    @Column(name = "home_routing", columnDefinition = "boolean default false")
    private boolean homeRouting;

    @Column(name = "hss_update_enabled", columnDefinition = "boolean default true", nullable = false)
    private boolean hssUpdateEnabled;

    @Column(name = "allowed_traffic", columnDefinition = "boolean default false", nullable = false)
    private boolean allowedTraffic;

    @Column(name = "allowed_ussi", columnDefinition = "boolean default false", nullable = false)
    private boolean allowedUssi;
}
