package com.smsc.management.app.diameter.model.entity;

import com.smsc.management.exception.ResourceNotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diameter_local_peer")
@SequenceGenerator(name = "diameter_local_peer_id_seq", sequenceName = "diameter_local_peer_id_seq", allocationSize = 1)
public class DiameterLocalPeer {
    @Id
    @GeneratedValue(generator = "diameter_local_peer_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String uri;
    private String ipAddresses;
    private String realm;
    private Integer vendorId;
    private String productName;
    private Integer firmwareVersion;

    @OneToMany(mappedBy = "diameterLocalPeer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiameterApplication> applications = new HashSet<>();

    @OneToOne(mappedBy = "localPeer")
    @JoinColumn(insertable = false, updatable = false)
    private DiameterGateway diameterGateway;
    @Column(name = "diameter_gateway_id")
    private Integer diameterGatewayId;

    public void addApplication(DiameterApplication application) {
        applications.add(application);
        application.setDiameterLocalPeer(this);
    }

    public DiameterApplication removeApplicationById(Integer id) {
        return applications.stream().filter(application -> application.getId().equals(id)).findFirst()
                .map(application -> {
                    applications.remove(application);
                    application.setDiameterLocalPeer(null);
                    return application;
                }).orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public void setDiameterGateway(DiameterGateway diameterGateway) {
        this.diameterGateway = diameterGateway;
        diameterGateway.setLocalPeer(this);
    }
}
