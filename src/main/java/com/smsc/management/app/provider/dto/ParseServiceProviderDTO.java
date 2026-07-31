package com.smsc.management.app.provider.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.utils.CustomStringDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ParseServiceProviderDTO {
	@JsonProperty("network_id")
	private int networkId;

	@JsonProperty("external_id")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private String externalId;

	private String name;
	@JsonProperty("system_id")

	private String systemId;

	private String password;

	@JsonProperty("system_type")
	private String systemType;

	@JsonProperty("interface_version")
	private String interfaceVersion;

	@JsonProperty("sessions_number")
	private int sessionsNumber;

	@JsonProperty("address_ton")
	private int addressTon;

	@JsonProperty("address_npi")
	private int addressNpi;

	@JsonProperty("address_range")
	private String addressRange;

	@JsonProperty("balance_type")
	private String balanceType;

	private Long balance;

	private int tps;

	private int validity;

	private int enabled; // account state

	@JsonProperty("enquire_link_period")
	private int enquireLinkPeriod;

	@JsonProperty("pdu_timeout")
	private int pduTimeout;

	@JsonProperty("status")
	private String status;

	@JsonProperty("active_sessions_numbers")
	private int activeSessionsNumbers;

	@JsonProperty("protocol")
	private String protocol;

	@JsonProperty("contact_name")
	private String contactName;

	@JsonProperty("email")
	private String email;

	@JsonProperty("phone_number")
	private String phoneNumber;

	@JsonProperty("bind_type")
	private String bindType;

	@JsonProperty("smpp_server_id")
	private Integer smppServerId;
	
	@JsonProperty("callback_url")
	private String callbackUrl;

	@JsonProperty("authentication_types")
	private String authenticationTypes;

	@JsonProperty("header_security_name")
	private String headerSecurityName;

	@JsonProperty("user_name")
	private String userName;

	@JsonProperty("passwd")
	private String passwd;

	@JsonProperty("token")
	private String token;

	@JsonProperty("message_priority")
	private String messagePriority;

	@JsonProperty("callback_headers_http")
	private List<CallbackHeaderHttpDTO> callbackHeadersHttp = new ArrayList<>();

	@JsonProperty("custom_parameters")
	private String customParameters;

	@JsonProperty("bearer_token_expiration_seconds")
	private Long bearerTokenExpirationSeconds;

	@JsonProperty("api_key_security_token")
	private String apiKeySecurityToken;

	@JsonProperty("generated_security_token")
	private GeneratedServiceProviderSecurityTokenDTO generatedSecurityToken;

	@JsonProperty("security_authentication_type")
	private String securityAuthenticationType;

	@JsonProperty(value = "basic_security_password", access = JsonProperty.Access.WRITE_ONLY)
	private String basicSecurityPassword;

	@JsonProperty("proxy_mode")
	private boolean proxyMode;

        @JsonProperty("dlr_tlv_enabled")
        private boolean dlrTlvEnabled;
}
