package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SccpRemoteResourcesDTO {
	private int id;
	
	@JsonProperty("remote_spc")
	private int remoteSpc;
	
	@JsonProperty("remote_spc_status")
	private String remoteSpcStatus = "ALLOWED";
	
	@JsonProperty("remote_sccp_status")
	private String remoteSccpStatus = "ALLOWED";
	
	@JsonProperty("remote_ssn")
	private int remoteSsn;
	
	@JsonProperty("remote_ssn_status")
	private String remoteSsnStatus = "ALLOWED";
	
	@JsonProperty("mark_prohibited")
	private boolean markProhibited;
	
	@JsonProperty("ss7_sccp_id")
	private int ss7SccpId;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
