package com.smsc.management.app.diameter.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diameter_peer")
@SequenceGenerator(name = "diameter_peer_id_seq", sequenceName = "diameter_peer_id_seq", allocationSize = 1)
public class DiameterPeer {
    @Id
    @GeneratedValue(generator = "diameter_peer_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private String uri;
    private boolean attemptConnect;
    private int rating;
    private String host;
    private String applications;
    private String ip;
    private String portRange;
    private String securityRef;
    private String standbyAddresses;
    private boolean started;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "diameter_gateway_id", insertable = false, updatable = false)
    private DiameterGateway diameterGateway;
    @Column(name = "diameter_gateway_id")
    private Integer diameterGatewayId;
}
