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
@Table(name = "m3ua_application_server")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_application_server_id_seq", sequenceName = "m3ua_application_server_id_seq", allocationSize = 1)
public class M3uaApplicationServer {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_application_server_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	private String state;
	
	private String functionality;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="functionality", insertable=false, updatable=false)
	private Functionality functionalityEntity;
	
	private String exchange;
	
	@Column(name="routing_context")
	private int routingContext;
	
	@Column(name="network_appearance")
	private Integer networkAppearance;
	
	@Column(name="traffic_mode_id")
	private int trafficModeId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="traffic_mode_id", insertable=false, updatable=false)
	private TrafficMode traficModeEntity;
	
	@Column(name="minimum_asp_for_loadshare")
	private int minimumAspForLoadshare;
}
