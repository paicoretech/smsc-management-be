package com.smsc.management.app.diameter.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.paicbd.smsc.dto.diameter.Application;
import com.paicbd.smsc.dto.diameter.ConnectionType;
import com.paicbd.smsc.dto.diameter.DiameterConfig;
import com.paicbd.smsc.dto.diameter.LocalPeer;
import com.paicbd.smsc.dto.diameter.Network;
import com.paicbd.smsc.dto.diameter.Parameters;
import com.paicbd.smsc.dto.diameter.Peer;
import com.paicbd.smsc.dto.diameter.Realm;
import com.paicbd.smsc.dto.diameter.RequestTable;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.exception.ResourceNotFoundException;
import com.smsc.management.utils.EntityBase;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diameter_gateway")
@SequenceGenerator(name = "diameter_gateway_id_seq", sequenceName = "diameter_gateway_id_seq", allocationSize = 1)
public class DiameterGateway extends EntityBase {
    @Id
    @GeneratedValue(generator = "diameter_gateway_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name="network_id", nullable = true)
    private Integer networkId;
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", insertable = false, updatable = false)
    private SequenceNetworksId sequenceNetworksId;

    private String name;
    private boolean started;
    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;
    private String type; // OCS|GATEWAY

    @Column(name="mno_id", nullable = true)
    private Integer mnoId;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name="mno_id", insertable=false, updatable=false)
    private OperatorMno operatorMnoId;

    private String globalTitle;

    @Column(name="protocol", columnDefinition = "text default 'DIAMETER'")
    private String protocol;

    @Column(name = "split_message", columnDefinition = "boolean default false")
    private boolean splitMessage;

    @Column(name = "messages_per_second_high", columnDefinition = "integer default 0")
    private Integer messagesPerSecondHigh;

    @Column(name = "messages_per_second_medium", columnDefinition = "integer default 0")
    private Integer messagesPerSecondMedium;

    @Column(name = "messages_per_second_low", columnDefinition = "integer default 0")
    private Integer messagesPerSecondLow;

    @Column(name = "messages_per_second", columnDefinition = "integer default 0")
    private Integer messagesPerSecond;

    @JsonManagedReference
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "diameter_local_peer_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DiameterLocalPeer localPeer;
    @Column(name = "diameter_local_peer_id")
    private Integer localPeerId;

    @JsonManagedReference
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "diameter_parameters_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DiameterParameters parameters;
    @Column(name = "diameter_parameters_id")
    private Integer parametersId;

    @JsonManagedReference
    @OneToMany(mappedBy = "diameterGateway", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiameterPeer> peers = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "diameterGateway", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiameterRealm> realms = new HashSet<>();

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean deleted;

    @Column(name = "hss_update_enabled", columnDefinition = "boolean default false", nullable = false)
    private boolean hssUpdateEnabled;

    @Column(name = "allowed_traffic", columnDefinition = "boolean default true", nullable = false)
    private boolean allowedTraffic = true;

    public void addPeer(DiameterPeer peer) {
        peers.add(peer);
        peer.setDiameterGateway(this);
    }

    public void removePeer(DiameterPeer peer) {
        peers.remove(peer);
        peer.setDiameterGateway(null);
    }

    public DiameterPeer removePeerById(Integer peerId) {
        return peers.stream()
                .filter(peer -> peer.getId().equals(peerId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Peer not found with id: " + peerId));
    }

    public void addRealm(DiameterRealm realm) {
        realms.add(realm);
        realm.setDiameterGateway(this);
    }

    public void removeRealm(DiameterRealm realm) {
        realms.remove(realm);
        realm.setDiameterGateway(null);
    }

    public DiameterRealm removeRealmById(Integer realmId) {
        return realms.stream()
                .filter(peer -> peer.getId().equals(realmId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Realm not found with id: " + realmId));
    }

    public DiameterConfig toDiameterConfig() {
        Network network = prepareNetworkConfiguration();
        Parameters configParameters = prepareParametersConfiguration();
        LocalPeer configLocalPeer = prepareLocalPeerConfiguration();

        return DiameterConfig.builder()
                .id(this.id)
                .networkId(this.networkId)
                .name(this.name)
                .enabled(this.started)
                .extensionMode(this.connectionType)
                .mnoId(this.mnoId)
                .splitMessage(this.splitMessage)
                .globalTitle(this.globalTitle)
                .network(network)
                .parameters(configParameters)
                .localPeer(configLocalPeer)
                .hssUpdateEnabled(this.hssUpdateEnabled)
                .allowedTraffic(this.allowedTraffic)
                .messagesPerSecondHigh(this.messagesPerSecondHigh)
                .messagesPerSecondMedium(this.messagesPerSecondMedium)
                .messagesPerSecondLow(this.messagesPerSecondLow)
                .messagesPerSecond(this.messagesPerSecond)
                .build();
    }

    public Network prepareNetworkConfiguration() {
        List<Peer> configPeers = new ArrayList<>(peers.size());
        if (!this.peers.isEmpty()) {
            this.peers.forEach(configPeer -> {
                Peer peer = Peer.builder()
                        .id(configPeer.getId())
                        .name(configPeer.getName())
                        .uri(configPeer.getUri())
                        .attemptConnect(configPeer.isAttemptConnect())
                        .rating(configPeer.getRating())
                        .host(configPeer.getHost())
                        .applications(configPeer.getApplications())
                        .ip(configPeer.getIp())
                        .portRange(configPeer.getPortRange())
                        .securityRef(configPeer.getSecurityRef())
                        .standbyAddresses(configPeer.getStandbyAddresses())
                        .build();

                configPeers.add(peer);
            });
        }

        List<Realm> configRealms = new ArrayList<>(realms.size());
        if (!this.realms.isEmpty()) {
            this.realms.forEach(configRealm -> {
                Application realmApplication = new Application(
                        configRealm.getApplication().getVendorId(),
                        configRealm.getApplication().getAuthApplId(),
                        configRealm.getApplication().getAcctApplId());

                Realm realm = Realm.builder()
                        .id(configRealm.getId())
                        .name(configRealm.getName())
                        .uri(configRealm.getUri())
                        .peers(configRealm.getPeers())
                        .localAction(configRealm.getLocalAction())
                        .dynamic(configRealm.isDynamic())
                        .expTime(configRealm.getExpTime())
                        .application(realmApplication)
                        .build();

                configRealms.add(realm);
            });
        }

        return new Network(configPeers, configRealms);
    }

    public Parameters prepareParametersConfiguration() {
        RequestTable requestTable = new RequestTable(
                this.parameters.getRequestTableSize(),
                this.parameters.getRequestTableClearSize()
        );

        return Parameters.builder()
                .acceptUndefinedPeer(this.parameters.isAcceptUndefinedPeer())
                .bindDelay(this.parameters.getBindDelay())
                .ceaTimeOut(this.parameters.getCeaTimeOut())
                .dpaTimeOut(this.parameters.getDpaTimeOut())
                .duplicateProtection(this.parameters.isDuplicateProtection())
                .duplicateSize(this.parameters.getDuplicateSize())
                .duplicateTimer(this.parameters.getDuplicateTimer())
                .dwaTimeOut(this.parameters.getDwaTimeOut())
                .iacTimeOut(this.parameters.getIacTimeOut())
                .messageTimeOut(this.parameters.getMessageTimeOut())
                .peerFSMThreadCount(this.parameters.getPeerFsmThreadCount())
                .queueSize(this.parameters.getQueueSize())
                .recTimeOut(this.parameters.getRecTimeOut())
                .sessionTimeOut(this.parameters.getSessionTimeOut())
                .singleLocalPeer(this.parameters.isSingleLocalPeer())
                .stopTimeOut(this.parameters.getStopTimeOut())
                .useUriAsFqdn(this.parameters.isUseUriAsFqdn())
                .requestTable(requestTable)
                .build();
    }

    public LocalPeer prepareLocalPeerConfiguration() {
        List<String> ipAddresses = Arrays.stream(Optional.ofNullable(this.localPeer.getIpAddresses()).orElse("")
                .split(",")).toList();

        List<Application> localPeerApplications = new ArrayList<>(this.localPeer.getApplications().size());
        if (!this.localPeer.getApplications().isEmpty()) {
            this.localPeer.getApplications().forEach(localPeerApplication -> {
                Application application = new Application(
                        localPeerApplication.getVendorId(),
                        localPeerApplication.getAuthApplId(),
                        localPeerApplication.getAcctApplId());
                localPeerApplications.add(application);
            });
        }

        return LocalPeer.builder()
                .id(this.localPeer.getId())
                .uri(this.localPeer.getUri())
                .productName(this.localPeer.getProductName())
                .firmwareRevision(this.localPeer.getFirmwareVersion())
                .realm(this.localPeer.getRealm())
                .vendorId(this.localPeer.getVendorId())
                .ipAddresses(ipAddresses)
                .applications(localPeerApplications)
                .build();
    }
}
