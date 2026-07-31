package com.smsc.management.app.ss7.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "map")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "map_id_seq", sequenceName = "map_id_seq", allocationSize = 1)
public class Map {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "map_id_seq")
    private int id;

    @Column(name = "network_id", unique = true)
    private int networkId;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", insertable = false, updatable = false)
    private Ss7Gateways ss7Gateways;

    @Column(name = "sri_service_op_code")
    private int sriServiceOpCode;

    @Column(name = "forward_sm_service_op_code")
    private int forwardSmServiceOpCode;
}
