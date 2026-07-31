package com.smsc.management.app.diameter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RealmDTO {
	private Integer id;

	@JsonProperty("diameter_gateway_id")
	private Integer diameterGatewayId;
	
	@NotNull(message = "name cannot be null")
	@NotBlank(message = "name cannot be empty")
	private String name;

	@NotNull(message = "URI cannot be null")
	@NotBlank(message = "URI cannot be empty")
	private String uri;

	@NotNull(message = "Peers cannot be null")
	@NotBlank(message = "Peers cannot be empty")
	private String peers;
	
	@NotNull(message = "local_action cannot be null")
	@Pattern(regexp = "^(LOCAL|RELAY|PROXY|REDIRECT)$", message = "local_action must be LOCAL or RELAY or PROXY or REDIRECT")
	@JsonProperty("local_action")
	private String localAction;

	private boolean dynamic;
	
	@NotNull(message = "exp_time cannot be null")
	@JsonProperty("exp_time")
	private int expTime;

	private ApplicationDTO application;
	private Integer applicationId;

	private boolean delete;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
