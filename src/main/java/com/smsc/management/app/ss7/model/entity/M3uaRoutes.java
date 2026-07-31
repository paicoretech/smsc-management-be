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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m3ua_routes", uniqueConstraints = {@UniqueConstraint(columnNames = {"m3ua_id", "origination_point_code", "destination_point_code", "service_indicator"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_routes_id_seq", sequenceName = "m3ua_routes_id_seq", allocationSize = 1)
public class M3uaRoutes {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_routes_id_seq")
	private int id;
	
	@Column(name = "origination_point_code")
	private int originationPointCode;
	
	@Column(name = "destination_point_code")
	private int destinationPointCode;
	
	@Column(name = "service_indicator")
	private int serviceIndicator;
	
	@Column(name="traffic_mode_id")
	private int trafficModeId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="traffic_mode_id", insertable=false, updatable=false)
	private TrafficMode traficModeEntity;
	
	@Column(name = "m3ua_id")
	private int m3uaId;
}
