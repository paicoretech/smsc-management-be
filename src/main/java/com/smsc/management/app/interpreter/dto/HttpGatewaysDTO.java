package com.smsc.management.app.interpreter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class HttpGatewaysDTO {
    @JsonProperty("network_id")
    private int networkId;

    private String name;
}
