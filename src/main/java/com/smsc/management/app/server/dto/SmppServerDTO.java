package com.smsc.management.app.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.utils.Converter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.smsc.management.utils.Constants.DEFAULT_STATUS;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SmppServerDTO {
    private int id;

    @NotBlank(message = "Name cannot be empty")
    @NotNull(message = "Name cannot be null")
    private String name;

    private String ip;

    @Min(value = 1, message = "Invalid port")
    private int port;

    @JsonProperty("transaction_timer")
    private int transactionTimer;

    @JsonProperty("wait_for_bind")
    private int waitForBind;

    @JsonProperty("processor_degree")
    private int processorDegree;

    @JsonProperty("queue_capacity")
    private int queueCapacity;

    private String status = DEFAULT_STATUS;

    @Min(value = 0, message = "enabled should not be less than 0")
    @Max(value = 2, message = "enabled should not be greater than 2")
    private int enabled;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_default")
    private boolean isDefault = false;

    @JsonProperty("action_status")
    private String actionStatus = "";

    @JsonProperty("tls_enabled")
    private boolean tlsEnabled = false;

    @Override
    public String toString() {
        return Converter.valueAsString(this);
    }
}
