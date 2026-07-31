package com.smsc.management.app.diameter.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "diameter_realm")
@SequenceGenerator(name = "diameter_realm_id_seq", sequenceName = "diameter_realm_id_seq", allocationSize = 1)
public class DiameterRealm {
    @Id
    @GeneratedValue(generator = "diameter_realm_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private String uri;
    private String peers;
    private String localAction;
    private boolean dynamic;
    private int expTime;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "diameter_application_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DiameterApplication application;
    @Column(name = "diameter_application_id")
    private Integer applicationId;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "diameter_gateway_id", insertable = false, updatable = false)
    private DiameterGateway diameterGateway;
    @Column(name = "diameter_gateway_id")
    private Integer diameterGatewayId;
}
