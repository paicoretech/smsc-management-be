package com.smsc.management.app.broadcast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastStatusRequestDTO {
    @JsonProperty("broadcast_status")
    @Pattern(regexp = "APPROVED|REJECTED|CANCELED|DELETED", message = "Invalid status are APPROVED|REJECTED|STOPPED allowed")
    private String broadcastStatus;

    @NotNull(message = "comment cannot be null")
    private String comment;
}
