package com.smsc.management.app.diameter.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "diameter_parameters")
@SequenceGenerator(name = "diameter_parameters_id_seq", sequenceName = "diameter_parameters_id_seq", allocationSize = 1)
public class DiameterParameters {
    @Id
    @GeneratedValue(generator = "diameter_parameters_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    private boolean acceptUndefinedPeer;
    private boolean duplicateProtection;
    private Integer duplicateTimer;
    private Integer duplicateSize;
    private boolean useUriAsFqdn;
    private Integer queueSize;
    private Integer messageTimeOut;
    private Integer stopTimeOut;
    private Integer ceaTimeOut;
    private Integer iacTimeOut;
    private Integer dwaTimeOut;
    private Integer dpaTimeOut;
    private Integer recTimeOut;
    private Integer peerFsmThreadCount;

    private boolean singleLocalPeer;
    private Long sessionTimeOut;
    private Long bindDelay;

    private Integer requestTableSize;
    private Integer requestTableClearSize;

    @JsonBackReference
    @OneToOne(mappedBy = "parameters", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(insertable = false, updatable = false)
    private DiameterGateway diameterGateway;
    @Column(name = "diameter_gateway_id")
    private Integer diameterGatewayId;

    public void setDiameterGateway(DiameterGateway diameterGateway) {
        this.diameterGateway = diameterGateway;
        diameterGateway.setParameters(this);
    }
}
