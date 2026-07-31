package com.smsc.management.app.ss7.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class M3uaApplicationServerDTO {
	private int id;
	
	private String name;
	
	private String state;
	
	private String functionality;
	
	@Pattern(regexp = "^(SE|DE)$", message = "exchange must be SE or DE")
	@NotBlank(message = "exchange cannot be empty")
	private String exchange;
	
	@JsonProperty("routing_context")
	private int routingContext;
	
	@JsonProperty("network_appearance")
	private Integer networkAppearance;
	
	@JsonProperty("traffic_mode_id")
	private int trafficModeId;
	
	@JsonProperty("minimum_asp_for_loadshare")
	private int minimumAspForLoadshare;
	
	@NotEmpty(message = "Asp factories cannot be empty")
	@Size(max = 128, message = "The amount of ASP exceeded must be at most 128")
	@JsonProperty("asp_factories")
	private List<Integer> aspFactories;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}

	public M3uaApplicationServerDTO(int id, String name, String state, String functionality, String exchange,
			int routingContext, Integer networkAppearance, int trafficModeId, int minimumAspForLoadShare) {
		this.id = id;
		this.name = name;
		this.state = state;
		this.functionality = functionality;
		this.exchange = exchange;
		this.routingContext = routingContext;
		this.networkAppearance = networkAppearance;
		this.trafficModeId = trafficModeId;
		this.minimumAspForLoadshare = minimumAspForLoadShare;
	}
}
