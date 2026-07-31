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
@Table(name = "sccp_addresses")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_addresses_id_seq", sequenceName = "sccp_addresses_id_seq", allocationSize = 1)
public class SccpAddresses {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_addresses_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "address_indicator")
	private int addressIndicator;
	
	@Column(name = "point_code")
	private int pointCode;
	
	@Column(name = "subsystem_number")
	private int subsystemNumber;
	
	@Column(name = "gt_indicator")
	private String gtIndicator;
	
	@Column(name = "translation_type")
	private int translationType;
	
	@Column(name = "numbering_plan_id")
	private int numberingPlanId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="numbering_plan_id", insertable=false, updatable=false)
	private NumberingPlan numberingPlan;
	
	@Column(name = "nature_of_address_id")
	private int natureOfAddressId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="nature_of_address_id", insertable=false, updatable=false)
	private NatureOfAddress natureOfAddress;

	private String digits;
	
	@Column(name="ss7_sccp_id")
	private int ss7SccpId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="ss7_sccp_id", insertable=false, updatable=false)
	private Sccp ss7Sccp;
}
