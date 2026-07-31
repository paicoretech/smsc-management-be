package com.smsc.management.app.interpreter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class InterpreterDTO {
    private int id;

    @JsonProperty("event_type")
    private String eventType;

    private String direction;

    @Pattern(regexp = "^(JSON|XML)$", message = "Event type must be input or output")
    @NotBlank(message = "body type should not be empty")
    @JsonProperty("body_type")
    private String bodyType;

    @NotBlank(message = "payload base should not be empty")
    private String template;

    @JsonProperty("gateway_id")
    private int gatewayId;

    @JsonProperty("gateway_name")
    private String gatewayName;

    @JsonProperty("use_proxy")
    private boolean useProxy = false;

    private String path;

    @JsonProperty("default_template")
    private boolean defaultTemplate = false;

    @JsonProperty("callback_headers_http")
    private List<CallbackHeaderHttpDTO> callbackHeadersHttp = new ArrayList<>();

    public void setTemplate(String template) {
        this.template = template.trim().replaceAll("[\r\n]+", "").replaceAll("\\s+", " ").replace("\n", "");
    }
}
