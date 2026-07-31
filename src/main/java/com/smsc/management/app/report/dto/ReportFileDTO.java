package com.smsc.management.app.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smsc.management.app.report.utils.FileExtension;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.utils.StaticMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportFileDTO {

    private int id;

    @JsonProperty("file_name")
    private String filename;

    private FileStatus status;

    private String type;

    private FileExtension extension;

    private String token;

    private String path;

    @JsonProperty("created_by_id")
    private Integer createdById;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return StaticMethods.toJson(this);
    }
}
