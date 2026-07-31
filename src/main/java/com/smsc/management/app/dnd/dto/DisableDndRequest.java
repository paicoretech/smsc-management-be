package com.smsc.management.app.dnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DisableDndRequest {
    @NotNull(message = "Parent ID is required")
    @JsonProperty("parent_id")
    private Integer parentId;
}
