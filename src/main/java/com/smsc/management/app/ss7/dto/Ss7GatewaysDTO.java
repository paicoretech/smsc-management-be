package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.app.gateway.validation.MessagePriorityRequired;
import com.smsc.management.utils.CustomStringDeserializer;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;


@MessagePriorityRequired
@Getter
@Setter
public class Ss7GatewaysDTO {
	@JsonProperty("network_id")
	private int networkId;

	@JsonProperty("external_id")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private String externalId;

	private String name;
	
	private int enabled; // account state
	
	private String status;
	
	private String protocol = "SS7";
	
	@JsonProperty("mno_id")
	private int mnoId;
	
	@JsonProperty("global_title")
	@Pattern(regexp = "^\\d*$", message = "The global title must contain only numbers.")
	private String globalTitle;
	
	@JsonProperty("global_title_indicator")
	private String globalTitleIndicator;

	@JsonProperty("messages_per_second_high")
	private int messagesPerSecondHigh;

	@JsonProperty("messages_per_second_medium")
	private int messagesPerSecondMedium;

	@JsonProperty("messages_per_second_low")
	private int messagesPerSecondLow;

	@JsonProperty("messages_per_second")
	private int messagesPerSecond;
	
	public void setMessagesPerSecond() {
		this.messagesPerSecond = this.messagesPerSecondHigh + this.messagesPerSecondMedium + this.messagesPerSecondLow;
	}
	
	@Min(value = 0, message = "translation type should not be less than 0")
	@Max(value = 255, message = "translation type should not be grater than 255")
	@JsonProperty("translation_type")
	private int translationType;
	
	@JsonProperty("smsc_ssn")
	private int smscSsn;
	
	@JsonProperty("hlr_ssn")
	private int hlrSsn;
	
	@JsonProperty("msc_ssn")
	private int mscSsn;
	
	@Min(value = 1, message = "map version should not be less than 1")
	@Max(value = 3, message = "map version should not be grater than 3")
	@JsonProperty("map_version")
	private int mapVersion;
	
	@JsonProperty("split_message")
	private boolean splitMessage = false;

    @JsonProperty("api_enabled")
    private boolean apiEnabled;

    @JsonProperty("app_token")
    private String appToken;

    @JsonProperty("home_routing")
    private boolean homeRouting = false;

    @JsonProperty("hss_update_enabled")
    private boolean hssUpdateEnabled;

    @JsonProperty("allowed_traffic")
    private boolean allowedTraffic;

    @JsonProperty("allowed_ussi")
    private boolean allowedUssi;

	@JsonProperty("sip_network_id")
	private Integer sipNetworkId;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
