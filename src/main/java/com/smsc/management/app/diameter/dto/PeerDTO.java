package com.smsc.management.app.diameter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class PeerDTO {

	private Integer id;

	@JsonProperty("diameter_gateway_id")
	private Integer diameterGatewayId;

	@NotNull(message = "name cannot be null")
	@NotBlank(message = "name cannot be empty")
	private String name;

	@NotBlank(message = "URI cannot be empty")
	@NotNull(message = "URI cannot be null")
	private String uri;

	@JsonProperty("attempt_connect")
	private boolean attemptConnect;

	private Integer rating;

	private String host;

	private String applications;

	private String ip;

	@JsonProperty("port_range")
	private String portRange;

	@JsonProperty("security_ref")
	private String securityRef;

	@JsonProperty("standby_address")
	private String standbyAddresses;

	private boolean started;

	private boolean delete;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
