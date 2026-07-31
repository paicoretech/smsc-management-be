package com.smsc.management.app.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetworksToRoutingRulesDTO {
	@JsonProperty("network_id")
	private int networkId;
	
	private String name;
	
	private String type;
	
	private String protocol;
}
