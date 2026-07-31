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
@Table(name = "m3ua_sockets")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_sockets_id_seq", sequenceName = "m3ua_sockets_id_seq", allocationSize = 1)
public class M3uaSockets {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_sockets_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(columnDefinition = "int DEFAULT 0")
	private int enabled; // account state
	
	private String state;
	
	@Column(name="socket_type")
	private String socketType;
	
	@Column(name="transport_type")
	private String transportType;
	
	@Column(name="host_address")
	private String hostAddress;
	
	@Column(name="host_port")
	private int hostPort;
	
	@Column(name="extra_address")
	private String extraAddress;
	
	@Column(name="max_concurrent_connections")
	private int maxConcurrentConnections;
	
	@Column(name="ss7_m3ua_id")
	private int ss7M3uaId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="ss7_m3ua_id", insertable=false, updatable=false)
	private M3ua ss7Mu3a;
}
