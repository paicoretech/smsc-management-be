package com.smsc.management.app.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRulesActionAdvancedDTO {
    @NotNull(message = "Map version should be not null")
    @JsonProperty("map_version")
    private Integer mapVersion = -1;

    @NotNull(message = "Operation code SRI should be not null")
    @JsonProperty("operation_code_sri")
    private Integer operationCodeSri = -1;

    @NotNull(message = "Operation code MT should be not null")
    @JsonProperty("operation_code_mt")
    private Integer operationCodeMt = -1;

    @NotNull(message = "SSN smsc SRI should be not null")
    @JsonProperty("ssn_smsc_sri")
    private Integer ssnSmscSri = -1;

    @NotNull(message = "SSN hlr SRI should be not null")
    @JsonProperty("ssn_hlr_sri")
    private Integer ssnHlrSri = -1;

    @NotNull(message = "SSN msc MT should be not null")
    @JsonProperty("ssn_msc_mt")
    private Integer ssnMscMt = -1;

    @NotNull(message = "SSN smsc MT should be not null")
    @JsonProperty("ssn_smsc_mt")
    private Integer ssnSmscMt = -1;

    @NotNull(message = "SCCP source addres SRI should be not null")
    @JsonProperty("sccp_source_address_sri")
    private String sccpSourceAddressSri = "";

    @NotNull(message = "SCCP source address MT should be not null")
    @JsonProperty("sccp_source_address_mt")
    private String sccpSourceAddressMt = "";

    @NotNull(message = "SCCP destination address MT should be not null")
    @JsonProperty("sccp_destination_address_mt")
    private String sccpDestinationAddressMt = "";

    @NotNull(message = "SCCP destination address SRI should be not null")
    @JsonProperty("sccp_destination_address_sri")
    private String sccpDestinationAddressSri = "";

    @NotNull(message = "Custom map layer source address SRI should be not null")
    @JsonProperty("custom_map_layer_source_address_sri")
    private String customMapLayerSourceAddressSri = "";

    @NotNull(message = "Custom map layer source address MT should be not null")
    @JsonProperty("custom_map_layer_source_address_mt")
    private String customMapLayerSourceAddressMt = "";

    @NotNull(message = "Custom map layer originator address MT should be not null")
    @JsonProperty("custom_map_layer_service_centre_address_oa")
    private String customMapLayerServiceCentreAddressOa = "";

    @JsonProperty("priority_flag_sri")
    private boolean priorityFlagSri = true;

    @NotNull(message = "Application context MT should be not null")
    @JsonProperty("application_context_mt")
    private String applicationContextMt = "";
}
