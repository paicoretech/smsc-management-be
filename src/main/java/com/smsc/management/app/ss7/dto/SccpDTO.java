package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smsc.management.utils.CustomStringDeserializer;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SccpDTO {
	private int id;
	
	@JsonProperty("network_id")
	private int  networkId;

	@JsonProperty("external_id")
	@JsonDeserialize(using = CustomStringDeserializer.class)
	private String externalId;
	
	@JsonProperty("z_margin_xudt_message")
	private int zMarginXudtMessage;
	
	@JsonProperty("remove_spc")
	private boolean removeSpc;
	
	@JsonProperty("sst_timer_duration_min")
	private int sstTimerDurationMin;
	
	@JsonProperty("sst_timer_duration_max")
	private int sstTimerDurationMax;

	@JsonProperty("sst_timer_duration_increase_factor")
	private float sstTimerDurationIncreaseFactor;
	
	@JsonProperty("max_data_message")
	private int maxDataMessage;
	
	@JsonProperty("period_of_logging")
	private int periodOfLogging;
	
	@JsonProperty("reassembly_timer_delay")
	private int reassemblyTimerDelay;
	
	@JsonProperty("preview_mode")
	private boolean previewMode;
	
	@Pattern(regexp = "^(ITU|ANSI)$", message = "sccp protocol version must be ITU or ANSI")
	@JsonProperty("sccp_protocol_version")
	private String sccpProtocolVersion = "ITU";
	
	@JsonProperty("congestion_control_timer_a")
	private int congestionControlTimerA;
	
	@JsonProperty("congestion_control_timer_d")
	private int congestionControlTimerD;
	
	@Pattern(regexp = "^(international|levelDepended)$", message = "congestion control algorithm must be international or levelDepended")
	@NotBlank(message = "congestion control algorithm cannot be empty")
	@JsonProperty("congestion_control_algorithm")
	private String congestionControlAlgorithm;
	
	@JsonProperty("congestion_control")
	private boolean congestionControl;

	@JsonProperty("rsp_prohibited_by_default")
	private boolean rspProhibitedByDefault;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
