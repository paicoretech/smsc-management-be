package com.smsc.management.app.server.model.entity;

import com.smsc.management.utils.EntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "smpp_server",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ip", "port"})
)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "smpp_server_id_seq", sequenceName = "smpp_server_id_seq", allocationSize = 1)
public class SmppServer extends EntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "smpp_server_id_seq")
    private int id;

    @Column(unique = true)
    private String name;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private int port;

    @Column(name = "transaction_timer")
    private int transactionTimer;

    @Column(name = "wait_for_bind")
    private int waitForBind;

    @Column(name = "processor_degree")
    private int processorDegree;

    @Column(name = "queue_capacity")
    private int queueCapacity;

    private String status;

    private int enabled;

    @Column(name="is_default", columnDefinition = "boolean NOT NULL DEFAULT false", updatable = false)
    private boolean isDefault;

    @Column(name = "tls_enabled", columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean tlsEnabled;

    @Column(name="action_status", columnDefinition = "text NOT NULL DEFAULT ''::text")
    private String actionStatus;
}
