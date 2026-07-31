package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.utils.CustomStringDeserializer;
import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class M3uaDTO {
	private int id;
	
	@JsonProperty("network_id")
	private int  networkId;

	@JsonProperty("external_id")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private String externalId;
	
	@JsonProperty("connect_delay")
	private int connectDelay;
	
	@JsonProperty("max_sequence_number")
	private int maxSequenceNumber = 256;

	@JsonProperty("max_for_route")
	private int maxForRoute = 2;
	
	@JsonProperty("thread_count")
	private int threadCount;
	
	@JsonProperty("routing_label_format")
	private String routingLabelFormat = "ITU";
	
	@JsonProperty("heart_beat_time")
	private int heartBeatTime;
	
	@JsonProperty("routing_key_management_enabled")
	private boolean routingKeyManagementEnabled;
	
	@JsonProperty("use_lowest_bit_for_link")
	private boolean useLowestBitForLink;
	
	@JsonProperty("cc_delay_threshold_1")
	private float ccDelayThreshold1;
	
	@JsonProperty("cc_delay_threshold_2")
	private float ccDelayThreshold2;
	
	@JsonProperty("cc_delay_threshold_3")
	private float ccDelayThreshold3;
	
	@JsonProperty("cc_delay_back_to_normal_threshold_1")
	private float ccDelayBackToNormalThreshold1;
	
	@JsonProperty("cc_delay_back_to_normal_threshold_2")
	private float ccDelayBackToNormalThreshold2;
	
	@JsonProperty("cc_delay_back_to_normal_threshold_3")
	private float ccDelayBackToNormalThreshold3;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
