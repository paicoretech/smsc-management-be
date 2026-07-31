package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.utils.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SccpLongMessageRulesDTO {
	private int id;

	@JsonProperty("first_point_code")
	private int firstPointCode;

	@JsonProperty("last_point_code")
	private int lastPointCode;

	@JsonProperty("long_message_rule_type")
	private String longMessageRuleType;

	@JsonProperty("sccp_sap_id")
	private int sccpSapId;

	@Override
	public String toString() {
		return Converter.valueAsString(this);
	}
}
