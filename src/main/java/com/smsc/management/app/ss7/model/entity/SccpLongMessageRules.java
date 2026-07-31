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
@Table(name = "sccp_long_message_rules")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_long_message_rules_id_seq", sequenceName = "sccp_long_message_rules_id_seq", allocationSize = 1)
public class SccpLongMessageRules {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_long_message_rules_id_seq")
	private Integer id;

	@Column(name = "first_point_code")
	private int firstPointCode;

	@Column(name = "last_point_code")
	private int lastPointCode;

	@Column(name = "long_message_rule_type")
	private String longMessageRuleType;

	@Column(name="sccp_sap_id")
	private int sccpSapId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="sccp_sap_id", insertable=false, updatable=false)
	private SccpServiceAccessPoints sccpSap;
}
