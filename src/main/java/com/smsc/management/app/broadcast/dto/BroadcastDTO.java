package com.smsc.management.app.broadcast.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class BroadcastDTO {
    private int id;

    @NotBlank(message = "name must not be empty")
    private String name;

    @NotNull(message = "name must not be null")
    private String description;

    @JsonProperty("network_id")
    private int networkId;

    private BroadcastStatus status = BroadcastStatus.CREATING;

    @JsonProperty("file_id")
    private int fileId;

    @JsonProperty("request_dlr")
    private boolean requestDlr = false;

    @JsonProperty("column_mapping")
    private Map<String, String> columnMapping;

    @JsonProperty("first_record_mapping")
    private String firstRecordMapping;

    @JsonProperty("message_template")
    private String messageTemplate;

    @JsonProperty("sender_id")
    private String senderId;

    @NotNull(message = "start_datetime must not be null")
    @JsonProperty("start_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDateTime;

    @NotNull(message = "max_execution_datetime must not be null")
    @JsonProperty("max_execution_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime maxExecutionDateTime;

    private String comment;

    @JsonProperty("source_addr_ton")
    private Integer sourceAddrTon;

    @JsonProperty("source_addr_npi")
    private Integer sourceAddrNpi;

    @JsonProperty("dest_addr_ton")
    private Integer destAddrTon;

    @JsonProperty("dest_addr_npi")
    private Integer destAddrNpi;

    @JsonProperty("data_coding")
    private Integer dataCoding;

    @JsonProperty("is_immediate")
    private Boolean isImmediate;

    @JsonProperty("created_by_id")
    private Integer createdById;

    @JsonProperty("available_sender_ids")
    private List<String> availableSenderIds;

    @Override
    public String toString() {
        return Converter.valueAsString(this);
    }
}
