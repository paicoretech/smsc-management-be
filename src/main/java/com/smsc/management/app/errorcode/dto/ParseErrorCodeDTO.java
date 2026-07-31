package com.smsc.management.app.errorcode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ParseErrorCodeDTO {
	private int id;
	@JsonProperty("code")
	private String code;
	@JsonProperty("description")
	private String description;
	@JsonProperty("mno_id")
	private int mnoId;
	@JsonProperty("mno_name")
	private String mnoName;
}
