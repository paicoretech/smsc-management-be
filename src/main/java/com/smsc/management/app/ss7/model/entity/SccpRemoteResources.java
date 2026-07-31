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
@Table(name = "sccp_remote_resources")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_remote_resources_id_seq", sequenceName = "sccp_remote_resources_id_seq", allocationSize = 1)
public class SccpRemoteResources {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_remote_resources_id_seq")
	private int id;
	
	@Column(name = "remote_spc")
	private int remoteSpc;
	
	@Column(name = "remote_spc_status")
	private String remoteSpcStatus = "ALLOWED";
	
	@Column(name = "remote_sccp_status")
	private String remoteSccpStatus = "ALLOWED";
	
	@Column(name = "remote_ssn")
	private int remoteSsn;
	
	@Column(name = "remote_ssn_status")
	private String remoteSsnStatus = "ALLOWED";
	
	@Column(name = "mark_prohibited")
	private boolean markProhibited;
	
	@Column(name="ss7_sccp_id")
	private int ss7SccpId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="ss7_sccp_id", insertable=false, updatable=false)
	private Sccp ss7Sccp;
}
