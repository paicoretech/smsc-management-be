package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SccpMtp3DestinationsDTO {
	private int id;
	
	private String name;
	
	@JsonProperty("first_point_code")
	private int firstPointCode;
	
	@JsonProperty("last_point_code")
	private int lastPointCode;
	
	@JsonProperty("first_sls")
	private int firstSls;
	
	@JsonProperty("last_sls")
	private int lastSls;
	
	@JsonProperty("sls_mask")
	private int slsMask;
	
	@JsonProperty("sccp_sap_id")
	private int sccpSapId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
