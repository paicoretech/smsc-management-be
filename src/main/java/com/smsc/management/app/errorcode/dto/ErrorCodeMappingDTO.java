package com.smsc.management.app.errorcode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ErrorCodeMappingDTO {
	private int id;
	
	@NotNull(message = "Error code cannot be null")
	@JsonProperty("error_code_id")
	private int errorCodeId;
	
	@NotNull(message = "Delivery error code cannot be null")
	@JsonProperty("delivery_error_code_id")
	private int deliveryErrorCodeId;
	
	@NotNull(message = "Delivery status cannot be null")
	@JsonProperty("delivery_status_id")
	private String deliveryStatusId;
}
