package com.smsc.management.app.diameter.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "diameter_application")
@SequenceGenerator(name = "diameter_application_id_seq", sequenceName = "diameter_application_id_seq", allocationSize = 1)
public class DiameterApplication {
    @Id
    @GeneratedValue(generator = "diameter_application_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private Integer vendorId;
    private Integer authApplId;
    private Integer acctApplId;

    @JsonBackReference
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(insertable = false, updatable = false)
    private DiameterRealm diameterRealm;
    @Column(name = "diameter_realm_id")
    private Integer diameterRealmId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "diameter_local_peer_id", insertable = false, updatable = false)
    private DiameterLocalPeer diameterLocalPeer;
    @Column(name = "diameter_local_peer_id")
    private Integer diameterLocalPeerId;
}
