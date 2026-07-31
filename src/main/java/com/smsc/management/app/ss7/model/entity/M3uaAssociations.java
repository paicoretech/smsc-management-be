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
@Table(name = "m3ua_associations")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_associations_id_seq", sequenceName = "m3ua_associations_id_seq", allocationSize = 1)
public class M3uaAssociations {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_associations_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(columnDefinition = "int DEFAULT 0")
	private int enabled; // account state
	
	private String state;
	
	private String peer;
	
	@Column(name="peer_port")
	private int peerPort;
	
	@Column(name="m3ua_heartbeat")
	private boolean m3uaHeartbeat;
	
	@Column(name="m3ua_socket_id")
	private int m3uaSocketId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="m3ua_socket_id", insertable=false, updatable=false)
	private M3uaSockets mu3aSocket;
	
	@Column(name="asp_name")
	private String aspName;
}
