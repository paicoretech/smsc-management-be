package com.smsc.management.app.ss7.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.paicbd.smsc.utils.HomeRoutingMode;
import com.smsc.management.utils.CustomStringDeserializer;
import com.smsc.management.utils.StaticMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class HomeRoutingDTO {

    private int id;

    @JsonProperty("network_id")
    private int networkId;

    @JsonProperty("external_id")
    @JsonDeserialize(using = CustomStringDeserializer.class)
    private String externalId;

    @JsonProperty("mode")
    private HomeRoutingMode mode = HomeRoutingMode.TRANSPARENT;

    @JsonProperty("ttl_cache")
    private int ttlCache = 300;

    @Override
    public String toString() {
        return StaticMethods.toJson(this);
    }
}