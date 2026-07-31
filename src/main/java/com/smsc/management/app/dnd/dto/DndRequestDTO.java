package com.smsc.management.app.dnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.utils.DndType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DndRequestDTO {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull(message = "DND Value is required")
    @JsonProperty("dnd_value")
    private String dndValue;

    @NotNull(message = "DND Type is required.")
    @JsonProperty("dnd_type")
    private DndType dndType;
}
