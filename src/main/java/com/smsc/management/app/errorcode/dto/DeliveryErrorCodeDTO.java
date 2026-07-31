package com.smsc.management.app.errorcode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeliveryErrorCodeDTO {
	private int id;
	
	@NotBlank(message = "Error code cannot be empty")
	@NotNull(message = "Error code cannot be null")
	@Pattern(regexp = "^\\d*$", message = "Error code should only be in number format")
	@JsonProperty("code")
	private String code;
	
	@NotBlank(message = "Error description cannot be empty")
	@NotNull(message = "Error description cannot be null")
	@JsonProperty("description")
	private String description;

}
