package com.smsc.management.app.dnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DndEntryMsisdnDTO {
    @NotNull(message = "Parent ID is required")
    @JsonProperty("parent_id")
    private Integer parentId;

    @NotEmpty(message = "MSISDN list cannot be empty")
    private List<@NotNull(message = "MSISDN is required") String> msisdns;
}
