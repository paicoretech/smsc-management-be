package com.smsc.management.app.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRulesDestinationDTO {
	private int id;
	
	@NotNull(message = "Priority cannot be null")
	private int priority;
	
	@NotNull(message = "Network id cannot be null")
	@JsonProperty("network_id")
	private int networkId;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("routing_rules_id")
	private int routingRulesId;
	
	// 0-> Update, 1-> Delete, 2-> new (only for the update method)
	private int action;
	
	@JsonProperty("network_type")
	private String networkType;
}
