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
@Table(name = "m3ua")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_id_seq", sequenceName = "m3ua_id_seq", allocationSize = 1)
public class M3ua {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_id_seq")
	private int id;
	
	@Column(name="network_id", unique = true)
	private int networkId;

	@Column(name = "external_id", unique = true)
	private String externalId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private Ss7Gateways ss7Gateway;
	
	@Column(name = "connect_delay")
	private int connectDelay;
	
	@Column(name = "max_sequence_number")
	private int maxSequenceNumber = 256;
	
	@Column(name = "max_for_route")
	private int maxForRoute = 2;
	
	@Column(name = "thread_count")
	private int threadCount;
	
	@Column(name = "routing_label_format")
	private String routingLabelFormat = "ITU";
	
	@Column(name = "heart_beat_time")
	private int heartBeatTime;
	
	@Column(name = "routing_key_management_enabled")
	private boolean routingKeyManagementEnabled;
	
	@Column(name = "use_lowest_bit_for_link")
	private boolean useLowestBitForLink;
	
	@Column(name = "cc_delay_threshold_1", columnDefinition = "numeric")
	private float ccDelayThreshold1;
	
	@Column(name = "cc_delay_threshold_2", columnDefinition = "numeric")
	private float ccDelayThreshold2;
	
	@Column(name = "cc_delay_threshold_3", columnDefinition = "numeric")
	private float ccDelayThreshold3;
	
	@Column(name = "cc_delay_back_to_normal_threshold_1", columnDefinition = "numeric")
	private float ccDelayBackToNormalThreshold1;
	
	@Column(name = "cc_delay_back_to_normal_threshold_2", columnDefinition = "numeric")
	private float ccDelayBackToNormalThreshold2;
	
	@Column(name = "cc_delay_back_to_normal_threshold_3", columnDefinition = "numeric")
	private float ccDelayBackToNormalThreshold3;
}
