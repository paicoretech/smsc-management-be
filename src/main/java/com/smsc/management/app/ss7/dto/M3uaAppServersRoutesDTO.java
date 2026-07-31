package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class M3uaAppServersRoutesDTO {
	@JsonProperty("route_id")
	private int routeId;

	@JsonProperty("application_server_id")
	private int applicationServerId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
