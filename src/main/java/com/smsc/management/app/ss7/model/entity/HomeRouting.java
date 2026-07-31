package com.smsc.management.app.ss7.model.entity;

import com.paicbd.smsc.utils.HomeRoutingMode;
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

@Entity
@Table(name = "home_routing")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "home_routing_id_seq", sequenceName = "home_routing_id_seq", allocationSize = 1)
public class HomeRouting {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "home_routing_id_seq")
    private int id;

    @Column(name = "network_id", unique = true, nullable = false)
    private int networkId;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", insertable = false, updatable = false)
    private Ss7Gateways ss7Gateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", columnDefinition = "text default 'TRANSPARENT'", nullable = false)
    private HomeRoutingMode mode = HomeRoutingMode.TRANSPARENT;

    @Column(name = "ttl_cache", columnDefinition = "int default 300", nullable = false)
    private int ttlCache = 300;
}
