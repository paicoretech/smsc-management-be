package com.smsc.management.app.errorcode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ParseErrorCodeMappingDTO {
	private int id;
	@JsonProperty("operator_mno_id")
	private int operatorMnoId;
	@JsonProperty("operator_mno")
	private String operatorMno;
	@JsonProperty("error_code_id")
	private int errorCodeId;
	@JsonProperty("error_code")
	private String errorCode;
	@JsonProperty("delivery_error_code_id")
	private int deliveryErrorCodeId;
	@JsonProperty("delivery_error_code")
	private String deliveryErrorCode;
	@JsonProperty("delivery_status")
	private String deliveryStatus;
}
