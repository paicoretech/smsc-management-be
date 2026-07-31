package com.smsc.management.app.broadcast.model.entity;

import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.report.utils.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "broadcast_file_id_seq", sequenceName = "broadcast_file_id_seq", allocationSize = 1)
public class BroadcastFile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "broadcast_file_id_seq")
    private int id;

    private String filename;

    @Column(name = "size_bytes", columnDefinition = "bigint DEFAULT 0")
    private long sizeBytes;

    @Column(name = "total_rows")
    private int totalRows;

    @Enumerated(EnumType.STRING)
    private BroadcastStatus status = BroadcastStatus.CREATING;

    @Column(columnDefinition = "text DEFAULT 'BROADCAST'")
    private String type = FileType.BROADCAST.getName();

    @Column(name = "columns", columnDefinition = "text")
    private String columns;

    @Column(name = "has_header", columnDefinition = "boolean DEFAULT false")
    private boolean hasHeader = true;

    @Column(name = "delimiter")
    private String delimiter = ",";

    private String token;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
