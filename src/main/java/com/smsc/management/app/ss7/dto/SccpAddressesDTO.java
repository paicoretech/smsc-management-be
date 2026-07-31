package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smsc.management.utils.StaticMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SccpAddressesDTO {
	@JsonIgnore
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private int id;
	
	private String name;
	
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

	private String digits;
	
	@JsonProperty("ss7_sccp_id")
	private int ss7SccpId;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
