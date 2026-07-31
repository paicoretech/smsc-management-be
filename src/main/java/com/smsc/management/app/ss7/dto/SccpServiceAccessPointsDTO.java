package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SccpServiceAccessPointsDTO {
	private int id;
	
	private String name;
	
	@JsonProperty("origin_point_code")
	private int originPointCode;
	
	@Max(value = 3, message = "Network indicator should not be greater than 3")
	@Min(value = 0, message = "Network indicator should not be less than 0")
	@JsonProperty("network_indicator")
	private int networkIndicator;
	
	@JsonProperty("local_gt_digits")
	private String localGtDigits;
	
	@JsonProperty("ss7_sccp_id")
	private int ss7SccpId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
