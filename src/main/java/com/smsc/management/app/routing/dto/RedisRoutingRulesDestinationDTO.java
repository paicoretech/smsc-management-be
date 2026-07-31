package com.smsc.management.app.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedisRoutingRulesDestinationDTO {
	private int priority;
	
	@JsonProperty("network_id")
	private int networkId;
	
	@JsonProperty("dest_protocol")
	private String destProtocol;
	
	@JsonProperty("network_type")
	private String networkType;
}
