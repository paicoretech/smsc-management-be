package com.smsc.management.app.broadcast.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class BroadcastRecordsResponse {
    public record BroadcastReader(
            BroadcastDTO broadcast,
            @JsonProperty("file_data") BroadcastFile fileData,
            BroadcastStatistics statistics,
            @JsonProperty("first_record_mapping") Map<String, Object> firstRecordMapping,
            @JsonProperty("message_preview") String messagePreview
    ) {}

    public record BroadcastStatistics(
            @JsonProperty("total_message") long totalMessage,
            long pending,
            long enqueue,
            long sent,
            long failed,
            long duplicated,
            long invalid
    ){}

        public record BroadcastViewer(
                        @JsonProperty("broadcast_id") int broadcastId,
                        String name,
                        BroadcastStatus status,
                        @JsonProperty("total_message") int totalMessage,
                        @JsonProperty("start_datetime") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDateTime,
                        @JsonProperty("max_execution_datetime") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime maxExecutionDateTime,
                        String comment,
                        @JsonProperty("created_by_id") Integer createdById,
                        @JsonProperty("created_by_username") String createdByUsername)
            {
        }

    public record FailureReasonSummary(String comment, Long count) {}
}
