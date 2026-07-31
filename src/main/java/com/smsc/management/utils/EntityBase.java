package com.smsc.management.utils;

import com.paicbd.smsc.utils.Generated;
import com.smsc.management.app.user.model.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;


@Slf4j
@Getter
@Setter
@Generated
@MappedSuperclass
public class EntityBase {
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by_id", updatable = false)
    private Integer createdById;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id" , insertable = false, updatable = false)
    private Users user;

    @LastModifiedBy
    @Column(name = "updated_by_id")
    private Integer updatedById;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Users userUpdate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        this.createdAt = localDateTime;
        this.updatedAt = localDateTime;
        this.createdById = StaticMethods.getCurrentUserId();
        this.updatedById = StaticMethods.getCurrentUserId();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedById = StaticMethods.getCurrentUserId();
    }
}
