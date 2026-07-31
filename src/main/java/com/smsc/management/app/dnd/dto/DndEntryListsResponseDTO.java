package com.smsc.management.app.dnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.dnd.utils.DndStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DndEntryListsResponseDTO {
    private Integer id;
    private String comment;
    private String name;

    @JsonProperty("dnd_value")
    private String dndValue;

    @JsonProperty("dnd_type")
    private String dndType;

    private DndStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by_id")
    private Integer createdById;

    @JsonProperty("updated_by_id")
    private Integer updatedById;
}