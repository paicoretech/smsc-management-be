package com.smsc.management.app.provider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratedServiceProviderSecurityTokenDTO {

    @JsonProperty("header_name")
    private String headerName;

    @JsonProperty("authentication_type")
    private String authenticationType;

    @JsonProperty("token")
    private String token;
}