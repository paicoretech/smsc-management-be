package com.smsc.management.app.ss7.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sccp")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_id_seq", sequenceName = "sccp_id_seq", allocationSize = 1)
public class Sccp {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_id_seq")
	private int id;
	
	@Column(name="network_id", unique = true)
	private int  networkId;

	@Column(name = "external_id", unique = true)
	private String externalId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private Ss7Gateways ss7Gateway;
	
	@Column(name = "z_margin_xudt_message")
	private int zMarginXudtMessage;
	
	@Column(name = "remove_spc")
	private boolean removeSpc;
	
	@Column(name = "sst_timer_duration_min")
	private int sstTimerDurationMin;
	
	@Column(name = "sst_timer_duration_max")
	private int sstTimerDurationMax;

	@Column(name = "sst_timer_duration_increase_factor", columnDefinition = "numeric")
	private float sstTimerDurationIncreaseFactor;
	
	@Column(name = "max_data_message")
	private int maxDataMessage;
	
	@Column(name = "period_of_logging")
	private int periodOfLogging;
	
	@Column(name = "reassembly_timer_delay")
	private int reassemblyTimerDelay;
	
	@Column(name = "preview_mode")
	private boolean previewMode;
	
	@Column(name = "sccp_protocol_version")
	private String sccpProtocolVersion;
	
	@Column(name = "congestion_control_timer_a")
	private int congestionControlTimerA;
	
	@Column(name = "congestion_control_timer_d")
	private int congestionControlTimerD;
	
	@Column(name = "congestion_control_algorithm")
	private String congestionControlAlgorithm;
	
	@Column(name = "congestion_control")
	private boolean congestionControl;

	@Column(name = "rsp_prohibited_by_default", columnDefinition = "boolean NOT NULL DEFAULT false")
	private boolean rspProhibitedByDefault;
}
