package com.smsc.management.app.errorcode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class RedisErrorCodeMappingDTO {
	@JsonProperty("error_code")
	private int errorCode;

	@JsonProperty("delivery_error_code")
	private int deliveryErrorCode;
	
	@JsonProperty("delivery_status")
	private String deliveryStatus;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
