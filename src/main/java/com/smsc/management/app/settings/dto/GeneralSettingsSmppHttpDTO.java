package com.smsc.management.app.settings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeneralSettingsSmppHttpDTO {
	private int id = 1;
	
	@JsonProperty("validity_period")
	private int validityPeriod = 60;
	
	@JsonProperty("max_validity_period")
	private int maxValidityPeriod = 240;
	
	@JsonProperty("source_addr_ton")
	private int sourceAddrTon = 1;
	
	@JsonProperty("source_addr_npi")
	private int sourceAddrNpi = 1;
	
	@JsonProperty("dest_addr_ton")
	private int destAddrTon = 1;
	
	@JsonProperty("dest_addr_npi")
	private int destAddrNpi = 1;
	
	@JsonProperty("encoding_iso88591")
	private int encodingIso88591 = 3;
	
	@JsonProperty("encoding_gsm7")
	private int encodingGsm7 = 0;
	
	@JsonProperty("encoding_ucs2")
	private int encodingUcs2 = 2;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
