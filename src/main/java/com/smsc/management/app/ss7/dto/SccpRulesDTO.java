package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SccpRulesDTO {
	private int id;
	private String name;
	private String mask;
	
	@JsonProperty("address_indicator")
	private int addressIndicator;
	
	@JsonProperty("point_code")
	private int pointCode;
	
	@JsonProperty("subsystem_number")
	private int subsystemNumber;
	
	@JsonProperty("gt_indicator")
	private String gtIndicator;
	
	@JsonProperty("translation_type")
	private int translationType;
	
	@JsonProperty("numbering_plan_id")
	private int numberingPlanId;

	@JsonProperty("numbering_plan_name")
	private String numberingPlanName;
	
	@JsonProperty("nature_of_address_id")
	private int natureOfAddressId;

	@JsonProperty("nature_of_address_name")
	private String natureOfAddressName;
	
	@JsonProperty("global_tittle_digits")
	private String globalTittleDigits;
	
	@JsonProperty("rule_type_id")
	private int ruleTypeId;

	@JsonProperty("rule_type_name")
	private String ruleTypeName;
	
	@JsonProperty("primary_address_id")
	private Integer primaryAddressId;

	@JsonProperty("primary_address_name")
	private String primaryAddressName;

	@JsonProperty("secondary_address_id")
	private Integer secondaryAddressId;

	@JsonProperty("secondary_address_name")
	private String secondaryAddressName;
	
	@JsonProperty("load_sharing_algorithm_id")
	private int loadSharingAlgorithmId;

	@JsonProperty("load_sharing_algorithm_name")
	private String loadSharingAlgorithmName;
	
	@JsonProperty("origination_type_id")
	private int originationTypeId;

	@JsonProperty("origination_type_name")
	private String originationTypeName;
	
	@JsonProperty("new_calling_party_address")
	private String newCallingPartyAddress;
	
	@JsonProperty("calling_address_indicator")
	private Integer callingAddressIndicator;
	
	@JsonProperty("calling_point_code")
	private Integer callingPointCode;
	
	@JsonProperty("calling_subsystem_number")
	private Integer callingSubsystemNumber;
	
	@JsonProperty("calling_translator_type")
	private Integer callingTranslatorType;
	
	@JsonProperty("calling_numbering_plan_id")
	private int callingNumberingPlanId;

	@JsonProperty("calling_numbering_plan_name")
	private String callingNumberingPlanName;
	
	@JsonProperty("calling_nature_of_address_id")
	private int callingNatureOfAddressId;

	@JsonProperty("calling_nature_of_address_name")
	private String callingNatureOfAddressName;
	
	@JsonProperty("calling_gt_indicator")
	private String callingGtIndicator;
	
	@JsonProperty("calling_global_tittle_digits")
	private String callingGlobalTittleDigits;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}

	public SccpRulesDTO(int id, String name, String mask, int addressIndicator, int pointCode, int subsystemNumber,
						String gtIndicator, int translationType, int numberingPlanId, String numberingPlanName,
						int natureOfAddressId, String natureOfAddressName, String globalTittleDigits,
						int ruleTypeId, String ruleTypeName, Integer primaryAddressId, String primaryAddressName,
						Integer secondaryAddressId, String secondaryAddressName, int loadSharingAlgorithmId,
						String loadSharingAlgorithmName, int originationTypeId, String originationTypeName,
						String newCallingPartyAddress, Integer callingAddressIndicator, Integer callingPointCode,
						Integer callingSubsystemNumber, Integer callingTranslatorType, int callingNumberingPlanId,
						String callingNumberingPlanName, int callingNatureOfAddressId, String callingNatureOfAddressName,
						String callingGtIndicator, String callingGlobalTittleDigits) {
		this.id = id;
		this.name = name;
		this.mask = mask;
		this.addressIndicator = addressIndicator;
		this.pointCode = pointCode;
		this.subsystemNumber = subsystemNumber;
		this.gtIndicator = gtIndicator;
		this.translationType = translationType;
		this.numberingPlanId = numberingPlanId;
		this.numberingPlanName = numberingPlanName;
		this.natureOfAddressId = natureOfAddressId;
		this.natureOfAddressName = natureOfAddressName;
		this.globalTittleDigits = globalTittleDigits;
		this.ruleTypeId = ruleTypeId;
		this.ruleTypeName = ruleTypeName;
		this.primaryAddressId = primaryAddressId;
		this.primaryAddressName = primaryAddressName;
		this.secondaryAddressId = secondaryAddressId;
		this.secondaryAddressName = secondaryAddressName;
		this.loadSharingAlgorithmId = loadSharingAlgorithmId;
		this.loadSharingAlgorithmName = loadSharingAlgorithmName;
		this.originationTypeId = originationTypeId;
		this.originationTypeName = originationTypeName;
		this.newCallingPartyAddress = newCallingPartyAddress;
		this.callingAddressIndicator = callingAddressIndicator;
		this.callingPointCode = callingPointCode;
		this.callingSubsystemNumber = callingSubsystemNumber;
		this.callingTranslatorType = callingTranslatorType;
		this.callingNumberingPlanId = callingNumberingPlanId;
		this.callingNumberingPlanName = callingNumberingPlanName;
		this.callingNatureOfAddressId = callingNatureOfAddressId;
		this.callingNatureOfAddressName = callingNatureOfAddressName;
		this.callingGtIndicator = callingGtIndicator;
		this.callingGlobalTittleDigits = callingGlobalTittleDigits;
	}
}
