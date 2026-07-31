package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class M3uaAspFactoriesDTO {
	private int id;
	
	private String name;
	
	private String state;
	
	private boolean heartbeat;

	@JsonProperty("m3ua_association_id")
	private int m3uaAssociationId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
