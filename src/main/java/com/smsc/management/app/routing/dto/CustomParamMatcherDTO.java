package com.smsc.management.app.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomParamMatcherDTO {
    @JsonProperty("property_name")
    private String propertyName;

    @JsonProperty("value_matcher")
    private String valueMatcher;
}
