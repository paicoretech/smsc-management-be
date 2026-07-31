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
public class RedisCustomParamMatcherDTO {
    @JsonProperty("property_name")
    private String propertyName;

    @JsonProperty("value_matcher")
    private Object valueMatcher;
}
