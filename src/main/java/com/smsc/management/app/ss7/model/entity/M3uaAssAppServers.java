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
@Table(name = "m3ua_ass_app_servers", uniqueConstraints = {@UniqueConstraint(columnNames = {"asp_id", "application_server_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "m3ua_ass_app_servers_id_seq", sequenceName = "m3ua_ass_app_servers_id_seq", allocationSize = 1)
public class M3uaAssAppServers {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "m3ua_ass_app_servers_id_seq")
	private int id;
	
	@Column(name="asp_id")
	private int aspId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="asp_id", insertable=false, updatable=false)
	private M3uaAssociations mu3aAssociations;
	
	@Column(name="application_server_id")
	private int applicationServerId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="application_server_id", insertable=false, updatable=false)
	private M3uaApplicationServer m3uaAppServer;

}
