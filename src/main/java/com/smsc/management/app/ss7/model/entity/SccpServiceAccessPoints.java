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
@Table(name = "sccp_service_access_points")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_service_access_points_id_seq", sequenceName = "sccp_service_access_points_id_seq", allocationSize = 1)
public class SccpServiceAccessPoints {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_service_access_points_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "origin_point_code")
	private int originPointCode;
	
	@Column(name = "network_indicator")
	private int networkIndicator;
	
	@Column(name = "local_gt_digits")
	private String localGtDigits;
	
	@Column(name="ss7_sccp_id")
	private int ss7SccpId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="ss7_sccp_id", insertable=false, updatable=false)
	private Sccp ss7Sccp;
}
