package com.smsc.management.app.broadcast.model.entity;

import com.paicbd.smsc.utils.BroadcastMessageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "broadcast_devices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_broadcast_msg_dest",
                        columnNames = {
                                "broadcast_id",
                                "message",
                                "destination_addr"
                        }
                )
        })
@IdClass(BroadcastDevicesId.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BroadcastDevices {
    @Id
    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Id
    @Column(name = "broadcast_id", nullable = false)
    private int broadcastId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="broadcast_id", insertable=false, updatable=false)
    private Broadcast broadcast;

    @Column(name = "source_addr")
    private String sourceAddr;

    @Column(name = "destination_addr")
    private String destinationAddr;

    @Column(name = "message", columnDefinition = "text NOT NULL DEFAULT ''")
    private String message;

    @Column(name = "column_mapping_data_str", columnDefinition = "text NOT NULL DEFAULT '{}'")
    private String columnMappingDataStr;

    @Column(name = "status", columnDefinition = "int NOT NULL DEFAULT 1")
    private int status = BroadcastMessageStatus.PENDING.getValue();

    @Column(name = "enqueue_at")
    private LocalDateTime enqueueAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "comment", columnDefinition = "text NOT NULL DEFAULT ''")
    private String comment = "";

    @Column(name = "dest_protocol", columnDefinition = "text NOT NULL DEFAULT ''")
    String destProtocol;

	@Column(name = "dest_network_id", columnDefinition = "int NOT NULL DEFAULT 0")
	Integer destNetworkId;

	@Column(name = "dest_network_type", columnDefinition = "text NOT NULL DEFAULT ''")
	String destNetworkType;

	@Column(name = "dest_name", columnDefinition = "text NOT NULL DEFAULT ''")
	String destName;

    @Column(name = "count_duplicated")
    int countDuplicated = 0;
}
