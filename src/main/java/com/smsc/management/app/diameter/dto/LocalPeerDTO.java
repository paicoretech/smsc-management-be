package com.smsc.management.app.diameter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class LocalPeerDTO {
	private Integer id;

	@JsonProperty("diameter_gateway_id")
	private Integer diameterGatewayId;

	@NotNull(message = "uri cannot be null")
	@NotEmpty(message = "uri cannot be empty")
	@Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d+.-]*://[a-zA-Z\\d._-]+(:\\d+)?$", message = "uri must be a valid URI format")
	private String uri;
	
	@NotNull(message = "ip_address cannot be null")
	@NotEmpty(message = "ip_address cannot be empty")
	@Pattern(regexp = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(,\\s*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})*$", message = "ip_addresses must be valid IPv4 format")
	@JsonProperty("ip_addresses")
	private String ipAddresses;

	@NotNull(message = "realm cannot be null")
	@NotEmpty(message = "realm cannot be empty")
	private String realm;

	@JsonProperty("vendor_id")
	private Integer vendorId;
	
	@NotNull(message = "product_name cannot be null")
	@NotEmpty(message = "product_name cannot be empty")
	@JsonProperty("product_name")
	private String productName;
	
	@NotNull(message = "firmware_version cannot be null")
	@JsonProperty("firmware_version")
	private Integer firmwareVersion;

	@Valid
	@JsonProperty("applications")
	private Set<ApplicationDTO> applications = new HashSet<>();

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
