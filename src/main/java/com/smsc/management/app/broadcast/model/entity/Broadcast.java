package com.smsc.management.app.broadcast.model.entity;

import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.utils.EntityBase;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "broadcast_id_seq", sequenceName = "broadcast_id_seq", allocationSize = 1)
public class Broadcast extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "broadcast_id_seq")
    private int id;

    @Column(unique = true)
    private String name;

    private String description;

    @Column(name = "network_id", nullable = false)
    private int networkId;

    @Enumerated(EnumType.STRING)
    private BroadcastStatus status = BroadcastStatus.CREATING;

    @Column(name = "file_id")
    private Integer fileId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private BroadcastFile file;

    @Column(name = "column_mapping", columnDefinition = "text")
    private String columnMapping;

    @Column(name = "message_template", columnDefinition = "text")
    private String messageTemplate;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "start_datetime")
    private LocalDateTime startDateTime;

    @Column(name = "max_execution_datetime")
    private LocalDateTime maxExecutionDateTime;

    @Column(name = "request_dlr", columnDefinition = "bool DEFAULT false")
    private boolean requestDlr;

    @Column(name = "first_record_mapping", columnDefinition = "text")
    private String firstRecordMapping;

    @Column(columnDefinition = "text NOT NULL DEFAULT ''")
    private String comment = "";

    @Column(name = "source_addr_ton")
    private Integer sourceAddrTon;

    @Column(name = "source_addr_npi")
    private Integer sourceAddrNpi;

    @Column(name = "dest_addr_ton")
    private Integer destAddrTon;

    @Column(name = "dest_addr_npi")
    private Integer destAddrNpi;

    @Column(name = "data_coding")
    private Integer dataCoding;

    @Column(name = "is_immediate", columnDefinition = "bool DEFAULT false")
    private Boolean isImmediate;
}
