package com.smsc.management.app.report.model.entity;

import com.smsc.management.app.report.utils.FileExtension;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.utils.StaticMethods;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Entity
@Table(name = "report_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "report_file_id_seq", sequenceName = "report_file_id_seq", allocationSize = 1)
public class ReportFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_file_id_seq")
    private int id;

    private String filename;

    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.CREATING;

    @Column(columnDefinition = "text DEFAULT 'CDRS'")
    private String type = FileType.CDRS.getName();

    @Enumerated(EnumType.STRING)
    private FileExtension extension = FileExtension.CSV;

    private String token;

    private String path;

    @CreatedBy
    @Column(name = "created_by_id", updatable = false)
    private Integer createdById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id" , insertable = false, updatable = false)
    private Users user;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        this.createdById = StaticMethods.getCurrentUserId();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
