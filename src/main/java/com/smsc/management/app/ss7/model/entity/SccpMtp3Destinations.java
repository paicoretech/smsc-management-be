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
@Table(name = "Sccp_mtp3_destinations")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "Sccp_mtp3_destinations_id_seq", sequenceName = "Sccp_mtp3_destinations_id_seq", allocationSize = 1)
public class SccpMtp3Destinations {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Sccp_mtp3_destinations_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "first_point_code")
	private int firstPointCode;
	
	@Column(name = "last_point_code")
	private int lastPointCode;
	
	@Column(name = "first_sls")
	private int firstSls;
	
	@Column(name = "last_sls")
	private int lastSls;
	
	@Column(name = "sls_mask")
	private int slsMask;
	
	@Column(name="sccp_sap_id")
	private int sccpSapId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="sccp_sap_id", insertable=false, updatable=false)
	private SccpServiceAccessPoints sccpSap;
}
