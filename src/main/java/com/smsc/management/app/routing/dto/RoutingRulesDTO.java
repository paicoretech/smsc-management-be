package com.smsc.management.app.routing.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.regex.ValidRegex;

import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRulesDTO {
	private int id;
	
	@NotNull(message = "Origin Network Id cannot be null")
	@JsonProperty("origin_network_id")
	private int  originNetworkId;
	
	@JsonProperty("origin_name")
	private String originName;
	
	@ValidRegex
	@NotNull(message = "Regex source addr cannot be null")
	@JsonProperty("regex_source_addr")
	private String  regexSourceAddr;
	
	@ValidRegex
	@NotNull(message = "Regex source addr ton cannot be null")
	@JsonProperty("regex_source_addr_ton")
	private String  regexSourceAddrTon;
	
	@ValidRegex
	@NotNull(message = "Regex source addr npi cannot be null")
	@JsonProperty("regex_source_addr_npi")
	private String  regexSourceAddrNpi;

	@ValidRegex
	@NotNull(message = "Regex destination addr cannot be null")
	@JsonProperty("regex_destination_addr")
	private String  regexDestinationAddr;

	@ValidRegex
	@NotNull(message = "Regex destination addr ton cannot be null")
	@JsonProperty("regex_dest_addr_ton")
	private String  regexDestAddrTon;
	
	@ValidRegex
	@NotNull(message = "Regex destination addr npi cannot be null")
	@JsonProperty("regex_dest_addr_npi")
	private String  regexDestAddrNpi;
	
	@ValidRegex
	@NotNull(message = "Regex imsi digits mask cannot be null")
	@JsonProperty("regex_imsi_digits_mask")
	private String  regexImsiDigitsMask;
	
	@ValidRegex
	@NotNull(message = "Regex network node number cannot be null")
	@JsonProperty("regex_network_node_number")
	private String  regexNetworkNodeNumber;
	
	@ValidRegex
	@NotNull(message = "Regex calling party address cannot be null")
	@JsonProperty("regex_calling_party_address")
	private String  regexCallingPartyAddress;
	
	@JsonProperty("is_sri_response")
	private boolean isSriResponse = false;

	@ValidRegex
	@NotNull(message = "Regex short message cannot be null")
	@JsonProperty("regex_short_message")
	private String  regexShortMessage;
	
	// actions
	@NotNull(message = "Destination cannot be null")
	@JsonProperty("destination")
	private List<RoutingRulesDestinationDTO> destination;
	
	@NotNull(message = "New source addr cannot be null")
	@JsonProperty("new_source_addr")
	private String  newSourceAddr;
	
	@NotNull(message = "New source addr ton cannot be null")
	@JsonProperty("new_source_addr_ton")
	private int  newSourceAddrTon;

	@NotNull(message = "New source addr npi cannot be null")
	@JsonProperty("new_source_addr_npi")
	private int  newSourceAddrNpi;
	
	@NotNull(message = "New destination addr cannot be null")
	@JsonProperty("new_destination_addr")
	private String  newDestinationAddr;
	
	@NotNull(message = "New destination addr ton cannot be null")
	@JsonProperty("new_dest_addr_ton")
	private int  newDestAddrTon;

	@NotNull(message = "New destination addr npi cannot be null")
	@JsonProperty("new_dest_addr_npi")
	private int  newDestAddrNpi;
	
	@NotNull(message = "Add source addr prefix cannot be null")
	@JsonProperty("add_source_addr_prefix")
	private String  addSourceAddrPrefix;
	
	@NotNull(message = "Add dest addr prefix cannot be null")
	@JsonProperty("add_dest_addr_prefix")
	private String  addDestAddrPrefix;
	
	@NotNull(message = "Remove source addr prefix cannot be null")
	@JsonProperty("remove_source_addr_prefix")
	private String  removeSourceAddrPrefix;
	
	@NotNull(message = "Remove dest addr prefix cannot be null")
	@JsonProperty("remove_dest_addr_prefix")
	private String  removeDestAddrPrefix;
	
	@NotNull(message = "New gt sccp addr cannot be null")
    @Pattern(
            regexp = "^(\\d*\\{\\{[^}]+}})*\\d*$",
            message = "Must be either a number or an expression like {{value}}"
    )
	@JsonProperty("new_gt_sccp_addr")
	private String  newGtSccpAddr;
	
	@NotNull(message = "Drop map sri cannot be null")
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
	private boolean checkSriResponse = false;

	@NotNull(message = "New short message cannot be null")
	@JsonProperty("new_short_message")
	private String  newShortMessage;

	@JsonProperty("custom_params_matcher")
	private List<CustomParamMatcherDTO> customParamMatcher;

	@JsonProperty("diameter_charging")
	private boolean diameterCharging = false;

	@JsonProperty("apply_for_refund")
	private boolean applyForRefund = false;

	@JsonProperty("action_advanced")
	private RoutingRulesActionAdvancedDTO actionAdvanced;

	// It is necessary to not include the destination field.
	public RoutingRulesDTO(int id, @NotNull(message = "Origin Network Id cannot be null") int originNetworkId,
			String originName, @NotNull(message = "Regex source addr cannot be null") String regexSourceAddr,
			@NotNull(message = "Regex source addr ton cannot be null") String regexSourceAddrTon,
			@NotNull(message = "Regex source addr npi cannot be null") String regexSourceAddrNpi,
			@NotNull(message = "Regex destination addr cannot be null") String regexDestinationAddr,
			@NotNull(message = "Regex destination addr ton cannot be null") String regexDestAddrTon,
			@NotNull(message = "Regex destination addr npi cannot be null") String regexDestAddrNpi,
			@NotNull(message = "Regex imsi digits mask cannot be null") String regexImsiDigitsMask,
			@NotNull(message = "Regex network node number cannot be null") String regexNetworkNodeNumber,
			@NotNull(message = "Regex calling party address cannot be null") String regexCallingPartyAddress,
			boolean isSriResponse, String regexShortMessage,
			@NotNull(message = "New source addr cannot be null") String newSourceAddr,
			@NotNull(message = "New source addr ton cannot be null") int newSourceAddrTon,
			@NotNull(message = "New source addr npi cannot be null") int newSourceAddrNpi,
			@NotNull(message = "New destination addr cannot be null") String newDestinationAddr,
			@NotNull(message = "New destination addr ton cannot be null") int newDestAddrTon,
			@NotNull(message = "New destination addr npi cannot be null") int newDestAddrNpi,
			@NotNull(message = "Add source addr prefix cannot be null") String addSourceAddrPrefix,
			@NotNull(message = "Add dest addr prefix cannot be null") String addDestAddrPrefix,
			@NotNull(message = "Remove source addr prefix cannot be null") String removeSourceAddrPrefix,
			@NotNull(message = "Remove dest addr prefix cannot be null") String removeDestAddrPrefix,
			@NotNull(message = "New gt sccp addr mt cannot be null") String newGtSccpAddr,
			@NotNull(message = "Drop map sri cannot be null") boolean dropMapSri, Integer networkIdToMapSri,
			Integer networkIdToPermanentFailure, boolean dropTempFailure, Integer networkIdTempFailure, boolean checkSriResponse, String  newShortMessage, boolean diameterCharging, boolean  applyForRefund) {
		this.id = id;
		this.originNetworkId = originNetworkId;
		this.originName = originName;
		this.regexSourceAddr = regexSourceAddr;
		this.regexSourceAddrTon = regexSourceAddrTon;
		this.regexSourceAddrNpi = regexSourceAddrNpi;
		this.regexDestinationAddr = regexDestinationAddr;
		this.regexDestAddrTon = regexDestAddrTon;
		this.regexDestAddrNpi = regexDestAddrNpi;
		this.regexImsiDigitsMask = regexImsiDigitsMask;
		this.regexNetworkNodeNumber = regexNetworkNodeNumber;
		this.regexCallingPartyAddress = regexCallingPartyAddress;
		this.isSriResponse = isSriResponse ;
		this.regexShortMessage = regexShortMessage;
		this.newSourceAddr = newSourceAddr;
		this.newSourceAddrTon = newSourceAddrTon;
		this.newSourceAddrNpi = newSourceAddrNpi;
		this.newDestinationAddr = newDestinationAddr;
		this.newDestAddrTon = newDestAddrTon;
		this.newDestAddrNpi = newDestAddrNpi;
		this.addSourceAddrPrefix = addSourceAddrPrefix;
		this.addDestAddrPrefix = addDestAddrPrefix;
		this.removeSourceAddrPrefix = removeSourceAddrPrefix;
		this.removeDestAddrPrefix = removeDestAddrPrefix;
		this.newGtSccpAddr = newGtSccpAddr;
		this.dropMapSri = dropMapSri;
		this.networkIdToMapSri = networkIdToMapSri;
		this.networkIdToPermanentFailure = networkIdToPermanentFailure;
		this.dropTempFailure = dropTempFailure;
		this.networkIdTempFailure = networkIdTempFailure;
		this.checkSriResponse = checkSriResponse;
		this.newShortMessage = newShortMessage;
		this.diameterCharging = diameterCharging;
		this.applyForRefund = applyForRefund;
	}
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
