package com.smsc.management.app.headers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class CallbackHeaderHttpDTO {
	@JsonProperty("header_name")
	private String headerName;
	
	@JsonProperty("header_value")
	private String headerValue;
}
