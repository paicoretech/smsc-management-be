package com.smsc.management.app.broadcast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.utils.Generated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Generated
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastTestDTO {
    @NotNull(message = "Service provider cannot be null")
    @JsonProperty("network_id")
    private int networkId;

    @NotNull(message = "Message template cannot be null")
    @JsonProperty("message_template")
    private String messageTemplate;

    @JsonProperty("source_addr")
    private String sourceAddr;

    @JsonProperty("destination_addr")
    private String destinationAddr;

    @NotNull(message = "Column mapping cannot be null")
    @JsonProperty("column_mapping")
    private Map<String, String> columnMapping = new HashMap<>();

    @NotNull(message = "Column mapping data cannot be null")
    @JsonProperty("column_mapping_data")
    private Map<String, Object> columnMappingData = new HashMap<>();

    @JsonProperty("source_addr_ton")
    private Integer sourceAddrTon;

    @JsonProperty("source_addr_npi")
    private Integer sourceAddrNpi;

    @JsonProperty("dest_addr_ton")
    private Integer destAddrTon;

    @JsonProperty("dest_addr_npi")
    private Integer destAddrNpi;

    @JsonProperty("data_coding")
    private Integer dataCoding;

    @JsonProperty("request_dlr")
    private boolean requestDlr = false;
}
