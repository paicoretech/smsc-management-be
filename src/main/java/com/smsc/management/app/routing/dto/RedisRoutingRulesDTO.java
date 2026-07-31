package com.smsc.management.app.routing.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class RedisRoutingRulesDTO {
	@JsonProperty("id")
	private int id;
	
	@JsonProperty("origin_network_id")
	private int  originNetworkId;
	
	@JsonProperty("regex_source_addr")
	private String  regexSourceAddr;
	
	@JsonProperty("regex_source_addr_ton")
	private String  regexSourceAddrTon;
	
	@JsonProperty("regex_source_addr_npi")
	private String  regexSourceAddrNpi;

	@JsonProperty("regex_destination_addr")
	private String  regexDestinationAddr;

	@JsonProperty("regex_dest_addr_ton")
	private String  regexDestAddrTon;
	
	@JsonProperty("regex_dest_addr_npi")
	private String  regexDestAddrNpi;
	
	@JsonProperty("regex_imsi_digits_mask")
	private String  regexImsiDigitsMask;
	
	@JsonProperty("regex_network_node_number")
	private String  regexNetworkNodeNumber;
	
	@JsonProperty("regex_calling_party_address")
	private String  regexCallingPartyAddress;
	
	@JsonProperty("is_sri_response")
	private boolean isSriResponse;

	@JsonProperty("regex_short_message")
	private String  regexShortMessage;
	
	// actions
	
	@JsonProperty("destination")
	private List<RedisRoutingRulesDestinationDTO> destination;
	
	@JsonProperty("new_source_addr")
	private String  newSourceAddr;
	
	@JsonProperty("new_source_addr_ton")
	private int  newSourceAddrTon;

	@JsonProperty("new_source_addr_npi")
	private int  newSourceAddrNpi;
	
	@JsonProperty("new_destination_addr")
	private String  newDestinationAddr;
	
	@JsonProperty("new_dest_addr_ton")
	private int  newDestAddrTon;

	@JsonProperty("new_dest_addr_npi")
	private int  newDestAddrNpi;
	
	@JsonProperty("add_source_addr_prefix")
	private String  addSourceAddrPrefix;
	
	@JsonProperty("add_dest_addr_prefix")
	private String  addDestAddrPrefix;
	
	@JsonProperty("remove_source_addr_prefix")
	private String  removeSourceAddrPrefix;
	
	@JsonProperty("remove_dest_addr_prefix")
	private String  removeDestAddrPrefix;
	
	@JsonProperty("new_gt_sccp_addr")
	private String  newGtSccpAddr;
	
	@JsonProperty("drop_map_sri")
	private boolean  dropMapSri;
	
	@JsonProperty("network_id_to_map_sri")
	private Integer  networkIdToMapSri;
	
	@JsonProperty("network_id_to_permanent_failure")
	private Integer  networkIdToPermanentFailure;
	
	@JsonProperty("drop_temp_failure")
	private boolean  dropTempFailure;
	
	@JsonProperty("network_id_temp_failure")
	private Integer  networkIdTempFailure;
	
	@JsonProperty("check_sri_response")
	private boolean checkSriResponse;
	
	@JsonProperty("origin_protocol")
	private String originProtocol;
	
	@JsonProperty("origin_network_type")
	private String originNetworkType;
	
	@JsonProperty("has_filter_rules")
	private boolean hasFilterRules = true;
	
	@JsonProperty("has_action_rules")
	private boolean hasActionRules = true;

	@JsonProperty("new_short_message")
	private String  newShortMessage;

	@JsonProperty("custom_params_matcher")
	private List<RedisCustomParamMatcherDTO> customParamMatcher = new ArrayList<>();

	@JsonProperty("diameter_charging")
	private boolean diameterCharging = false;

	@JsonProperty("apply_for_refund")
	private boolean applyForRefund = false;

	@JsonProperty("auto_map_version")
	private Boolean autoMapVersion = true;

	@JsonProperty("has_action_advanced_rules")
	private boolean hasActionAdvancedRules = false;

	@JsonProperty("action_advanced")
	private RoutingRulesActionAdvancedDTO actionAdvanced;
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
