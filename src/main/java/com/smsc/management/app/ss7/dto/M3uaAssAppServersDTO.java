package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class M3uaAssAppServersDTO {
	@JsonProperty("asp_factory_id")
	private int aspFactoryId;
	
	@JsonProperty("application_server_id")
	private int applicationServerId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
