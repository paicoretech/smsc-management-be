package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.utils.StaticMethods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class HomeRoutingCcMccMncDTO {

    private long id;

    @NotNull(message = "Country code cannot be null")
    @Pattern(regexp = "^[0-9]*$", message = "Country code must contain only digits")
    @JsonProperty("country_code")
    private String countryCode = "";

    @NotBlank(message = "MCC/MNC cannot be blank")
    @Pattern(regexp = "^[0-9]+$", message = "MCC/MNC must contain only digits")
    @JsonProperty("mcc_mnc")
    private String mccMnc;

    @NotNull(message = "SMSC cannot be null")
    @Pattern(regexp = "^[0-9]*$", message = "SMSC must contain only digits")
    @JsonProperty("smsc")
    private String smsc = "";

    @NotNull(message = "ss7_home_routing_id cannot be null")
    @JsonProperty("ss7_home_routing_id")
    private int ss7HomeRoutingId;

    @Override
    public String toString() {
        return StaticMethods.toJson(this);
    }
}