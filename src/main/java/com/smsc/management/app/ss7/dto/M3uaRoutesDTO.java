package com.smsc.management.app.ss7.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class M3uaRoutesDTO {
	private int id;

	@JsonProperty("origination_point_code")
	private int originationPointCode;
	
	@JsonProperty("destination_point_code")
	private int destinationPointCode;
	
	@JsonProperty("service_indicator")
	private int serviceIndicator;
	
	@JsonProperty("traffic_mode_id")
	private int trafficModeId;

	@JsonProperty("traffic_mode_name")
	private String trafficModeName;
	
	@NotEmpty(message = "Applications servers cannot be empty")
	@Size(max = 2, message = "The amount of Applications Servers exceeded must be at most 2")
	@JsonProperty("app_servers")
	private List<Integer> appServers;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}

	public M3uaRoutesDTO(int id, int originationPointCode, int destinationPointCode, int serviceIndicator, int trafficModeId, String trafficModeName) {
		this.id = id;
		this.originationPointCode = originationPointCode;
		this.destinationPointCode = destinationPointCode;
		this.serviceIndicator = serviceIndicator;
		this.trafficModeId = trafficModeId;
		this.trafficModeName = trafficModeName;
	}
}
