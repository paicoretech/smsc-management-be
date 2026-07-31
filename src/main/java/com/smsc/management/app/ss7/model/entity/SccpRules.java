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
@Table(name = "sccp_rules")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "sccp_rules_id_seq", sequenceName = "sccp_rules_id_seq", allocationSize = 1)
public class SccpRules {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccp_rules_id_seq")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	private String mask;
	
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
	
	@Column(name = "global_tittle_digits")
	private String globalTittleDigits;
	
	@Column(name = "rule_type_id")
	private int ruleTypeId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="rule_type_id", insertable=false, updatable=false)
	private RuleType ruleType;
	
	@Column(name = "primary_address_id")
	private Integer primaryAddressId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="primary_address_id", referencedColumnName = "id", insertable=false, updatable=false)
	private SccpAddresses primaryAddress;
	
	@Column(name = "secondary_address_id")
	private Integer secondaryAddressId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="secondary_address_id", referencedColumnName = "id", insertable=false, updatable=false)
	private SccpAddresses secondaryAddress;
	
	@Column(name = "load_sharing_algorithm_id")
	private int loadSharingAlgorithmId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="load_sharing_algorithm_id", insertable=false, updatable=false)
	private LoadSharingAlgorithm loadSharingAlgorithm;
	
	@Column(name = "origination_type_id")
	private int originationTypeId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="origination_type_id", insertable=false, updatable=false)
	private OriginationType originationType;
	
	@Column(name = "new_calling_party_address")
	private String newCallingPartyAddress;
	
	@Column(name = "calling_address_indicator")
	private Integer callingAddressIndicator;
	
	@Column(name = "calling_point_code")
	private Integer callingPointCode;
	
	@Column(name = "calling_subsystem_number")
	private Integer callingSubsystemNumber;
	
	@Column(name = "calling_translator_type")
	private Integer callingTranslatorType;
	
	@Column(name = "calling_numbering_plan_id")
	private int callingNumberingPlanId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="calling_numbering_plan_id", insertable=false, updatable=false)
	private NumberingPlan callingNumberingPlan;
	
	@Column(name = "calling_nature_of_address_id")
	private int callingNatureOfAddressId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="calling_nature_of_address_id", insertable=false, updatable=false)
	private NatureOfAddress callingNatureOfAddress;
	
	@Column(name = "calling_gt_indicator")
	private String callingGtIndicator;
	
	@Column(name = "calling_global_tittle_digits")
	private String callingGlobalTittleDigits;
}
