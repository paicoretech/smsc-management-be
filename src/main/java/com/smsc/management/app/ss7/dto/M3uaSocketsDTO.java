package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class M3uaSocketsDTO {
	private int id;
	
	private String name;
	
	private String state;
	
	@Min(value = 0, message = "enabled should not be less than 0")
	@Max(value = 1, message = "enabled should not be greater than 1")
	@JsonProperty("enabled")
	private int enabled; // account state
	
	@Pattern(regexp = "^(Client|Server)$", message = "Socket type must be Client or Server")
	@NotBlank(message = "Socket type cannot be empty")
	@JsonProperty("socket_type")
	private String socketType;
	
	@Pattern(regexp = "^(SCTP|TCP)$", message = "transport type must be SCTP or TCP")
	@NotBlank(message = "transport type cannot be empty")
	@JsonProperty("transport_type")
	private String transportType;
	
	@JsonProperty("host_address")
	private String hostAddress;
	
	@JsonProperty("host_port")
	private int hostPort;
	
	@JsonProperty("extra_address")
	private String extraAddress;
	
	@JsonProperty("max_concurrent_connections")
	private int maxConcurrentConnections;

	@JsonProperty("ss7_m3ua_id")
	private int ss7M3uaId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
