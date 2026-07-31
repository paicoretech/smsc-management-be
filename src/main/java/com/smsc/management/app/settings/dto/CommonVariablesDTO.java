package com.smsc.management.app.settings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommonVariablesDTO {
	private String key;
	
	@NotBlank(message = "Value is required")
	private String value;
	
	@JsonProperty("data_type")
	private String dataType;
}
