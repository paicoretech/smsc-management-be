package com.smsc.management.app.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GatewaysInterpreterDTO {
    @JsonProperty("event_type")
    private String eventType;

    private String direction;

    @JsonProperty("body_type")
    private String bodyType;

    private String template;

    private String path;

    @JsonProperty("use_proxy")
    private boolean useProxy = false;

    @JsonProperty("callback_headers_http")
    private List<CallbackHeaderHttpDTO> callbackHeadersHttp = new ArrayList<>();
}
