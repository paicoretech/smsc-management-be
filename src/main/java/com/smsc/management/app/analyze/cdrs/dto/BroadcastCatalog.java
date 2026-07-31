package com.smsc.management.app.analyze.cdrs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class BroadcastCatalog {
    @JsonProperty("broadcast_id")
    private int broadcastId;

    @JsonProperty("broadcast_name")
    private String broadcastName;

    @JsonProperty("user_id")
    private int userId;

    @JsonProperty("user_name")
    private String userName;
}
