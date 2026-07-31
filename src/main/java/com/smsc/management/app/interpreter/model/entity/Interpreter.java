package com.smsc.management.app.interpreter.model.entity;

import com.smsc.management.app.gateway.model.entity.Gateways;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interpreter", uniqueConstraints = {@UniqueConstraint(columnNames={"event_type", "direction", "gateway_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "interpreter_id_seq", sequenceName = "interpreter_id_seq", allocationSize = 1)
public class Interpreter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interpreter_id_seq")
    private int id;

    @Column(name="event_type")
    private String eventType;

    private String direction;

    @Column(name="body_type")
    private String bodyType;

    @Column(name="gateway_id")
    private int gatewayId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="gateway_id", insertable=false, updatable=false)
    private Gateways gateways;

    @Column(name="template", columnDefinition = "text NOT NULL DEFAULT ''")
    private String template = "";

    @Column(name="use_proxy", columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean useProxy;

    @Column(name="path")
    private String path;

    @Column(name="default_template", columnDefinition = "boolean NOT NULL DEFAULT false")
    private boolean defaultTemplate;
}
