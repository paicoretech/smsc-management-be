package com.smsc.management.app.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.app.gateway.validation.MessagePriorityRequired;
import com.smsc.management.regex.ValidRegex;

import com.smsc.management.utils.CustomStringDeserializer;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@MessagePriorityRequired
@Setter
@Getter
public class GatewaysDTO {
	@JsonIgnore
	private final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty("network_id")
	private int networkId;

	@JsonProperty("external_id")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private String externalId;
	
	@NotBlank(message = "Name cannot be empty")
	@NotNull(message = "Name cannot be null")
	@JsonProperty("name")
	private String name;
	
	@NotBlank(message = "Name cannot be empty")
	@NotNull(message = "Name cannot be null")
	@Pattern(regexp = "[^,\\'}{\\]\\[\\)\\(\\\"\\\\]+", message = "The following characters are not supported for networkId: ,'}{][)(\"\\")
	@JsonProperty("system_id")
	private String systemId;
	
	@NotNull(message = "Name cannot be null")
	private String password;
	
	@NotBlank(message = "Name cannot be empty")
	@NotNull(message = "Name cannot be null")
	private String ip;
	
	@NotNull(message = "Name cannot be null")
	private int port;
	
	@JsonProperty("bind_type")
	private String bindType;
	
	@Size(max = 13, message = "System type must be between 1 and 13 characters")
	@JsonProperty("system_type")
	private String systemType;
	
	@JsonProperty("interface_version")
	private String interfaceVersion;

	@Min(value = 1, message = "sessionsNumber should not be less than 1")
	@Max(value = 50, message = "sessionsNumber should not be greater than 50")
	@JsonProperty("sessions_number")
	private int sessionsNumber;
		
	@NotNull(message = "Address TON cannot be null")
	@Min(value = 0, message = "addressTon should not be less than 0")
	@JsonProperty("address_ton")
	private int addressTon;
	
	@NotNull(message = "Address NPI cannot be null")
	@Min(value = 0, message = "addressNpi should not be less than 0")
	@JsonProperty("address_npi")
	private int addressNpi;
	
	@ValidRegex
	@JsonProperty("address_range")
	private String addressRange;
	
	@Min(value = 0, message = "enabled should not be less than 0")
	@Max(value = 2, message = "enabled should not be greater than 2")
	@JsonProperty("enabled")
	private int enabled; // account state

	@JsonProperty("enquire_link_period")
	private int enquireLinkPeriod;

	@JsonProperty("request_dlr")
	private int requestDlr;
	
	@JsonProperty("no_retry_error_code")
	private String noRetryErrorCode;
	
	@JsonProperty("retry_alternate_destination_error_code")
	private String retryAlternateDestinationErrorCode;
	
	@JsonProperty("bind_timeout")
	private int bindTimeout;
	
	@JsonProperty("bind_retry_period")
	private int bindRetryPeriod;
	
	@JsonProperty("pdu_timeout")
	private int pduTimeout;
	
	@Min(value = 1, message = "pdu degree should not be less than 1")
	@Max(value = 1000, message = "pdu degree should not be greater than 1000")
	@JsonProperty("pdu_degree")
	private int pduDegree;
	
	@Min(value = 100, message = "thread pool size should not be less than 100")
	@JsonProperty("thread_pool_size")
	private int threadPoolSize;
	
	@JsonProperty("mno_id")
	private int mnoId;
	
	@Pattern(regexp = "^(SMPP|HTTP)$", message = "Protocol must be SMPP or HTTP")
	@NotBlank(message = "Protocol cannot be empty")
	@JsonProperty("protocol")
	private String protocol;
	
	@NotNull(message = "Auto retry error code cannot be null")
	@JsonProperty("auto_retry_error_code")
	private String autoRetryErrorCode;

	@JsonProperty("encoding_iso88591")
	private int encodingIso88591 = 3;

	@JsonProperty("encoding_gsm7")
	private int encodingGsm7 = 0;

	@JsonProperty("encoding_ucs2")
	private int encodingUcs2 = 2;
	
	@JsonProperty("split_message")
	private boolean splitMessage = false;
	
	@Pattern(regexp = "^(TLV|UDH)$", message = "Split smpp type must be TLV or UDH")
	@JsonProperty("split_smpp_type")
	private String splitSmppType = "TLV";

	@Pattern(regexp = "^(Basic|Bearer|Api-key|Undefined)$", message = "authentication types must be Undefined or Basic or Bearer or Api-key")
	@JsonProperty("authentication_types")
	private String authenticationTypes;

	@JsonProperty("header_security_name")
	private String headerSecurityName = "Authorization";

	@JsonProperty("user_name")
	private String userName;

	@JsonProperty("passwd")
	private String passwd;

	@NotNull(message = "token cannot be null")
	@JsonProperty("token")
	private String token;

	@JsonProperty("messages_per_second_high")
	private int messagesPerSecondHigh;

	@JsonProperty("messages_per_second_medium")
	private int messagesPerSecondMedium;

	@JsonProperty("messages_per_second_low")
	private int messagesPerSecondLow;

	@JsonProperty("messages_per_second")
	private int messagesPerSecond;

	@JsonProperty("tls_enabled")
	private boolean tlsEnabled = false;

	public void setMessagesPerSecond() {
		this.messagesPerSecond = this.messagesPerSecondHigh + this.messagesPerSecondMedium + this.messagesPerSecondLow;
	}
	
	public void setAddressRange(String addressRange) {
		this.addressRange = "";
		if (addressRange != null && !addressRange.isBlank()) {
			this.addressRange = addressRange;
		}
	}

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
