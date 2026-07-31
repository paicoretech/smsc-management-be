package com.smsc.management.app.diameter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ParametersDTO {
	private Integer id;

	@JsonProperty("diameter_gateway_id")
	private Integer diameterGatewayId;
	
	@JsonProperty("accept_undefined_peer")
	private boolean acceptUndefinedPeer;
	
	@JsonProperty("duplicate_protection")
	private boolean duplicateProtection;
	
	@JsonProperty("duplicate_timer")
	private Integer duplicateTimer;
	
	@JsonProperty("duplicate_size")
	private Integer duplicateSize;

	@JsonProperty("use_uri_as_fqdn")
	private boolean useUriAsFqdn;

	@JsonProperty("queue_size")
	private Integer queueSize;
	
	@JsonProperty("message_time_out")
	private Integer messageTimeOut;
	
	@JsonProperty("stop_time_out")
	private Integer stopTimeOut;
	
	@JsonProperty("cea_time_out")
	private Integer ceaTimeOut;
	
	@JsonProperty("iac_time_out")
	private Integer iacTimeOut;
	
	@JsonProperty("dwa_time_out")
	private Integer dwaTimeOut;
	
	@JsonProperty("dpa_time_out")
	private Integer dpaTimeOut;
	
	@JsonProperty("rec_time_out")
	private Integer recTimeOut;
	
	@JsonProperty("peer_fsm_thread_count")
	private Integer peerFsmThreadCount;

	@JsonProperty("single_local_peer")
	private boolean singleLocalPeer;

	@JsonProperty("session_time_out")
	private Long sessionTimeOut;

	@JsonProperty("bind_delay")
	private Long bindDelay;

	@JsonProperty("request_table_size")
	private int requestTableSize;

	@JsonProperty("request_table_clear_size")
	private int requestTableClearSize;

	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
