package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class M3uaAssociationsDTO {
	private int id;
	
	private String name;
	
	@Min(value = 0, message = "enabled should not be less than 0")
	@Max(value = 1, message = "enabled should not be greater than 1")
	@JsonProperty("enabled")
	private int enabled; // account state
	
	private String state;
	
	private String peer;

	@JsonProperty("peer_port")
	private int peerPort;
	
	@JsonProperty("m3ua_heartbeat")
	private boolean m3uaHeartbeat;
	
	@JsonProperty("m3ua_socket_id")
	private int m3uaSocketId;
	
	@JsonProperty("asp_name")
	private String aspName;

	@JsonProperty("m3ua_socket_name")
	private String m3uaSocketName;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
