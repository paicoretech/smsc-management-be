package com.smsc.management.app.broadcast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastParamsDTO {

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
}
