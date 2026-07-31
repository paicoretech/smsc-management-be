package com.smsc.management.app.diameter.mapper;

import com.smsc.management.app.diameter.dto.ApplicationDTO;
import com.smsc.management.app.diameter.dto.DiameterGatewayDTO;
import com.smsc.management.app.diameter.dto.LocalPeerDTO;
import com.smsc.management.app.diameter.dto.ParametersDTO;
import com.smsc.management.app.diameter.dto.PeerDTO;
import com.smsc.management.app.diameter.dto.RealmDTO;
import com.smsc.management.app.diameter.model.entity.DiameterApplication;
import com.smsc.management.app.diameter.model.entity.DiameterGateway;
import com.smsc.management.app.diameter.model.entity.DiameterLocalPeer;
import com.smsc.management.app.diameter.model.entity.DiameterParameters;
import com.smsc.management.app.diameter.model.entity.DiameterPeer;
import com.smsc.management.app.diameter.model.entity.DiameterRealm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GlobalDiameterMapper {

    @Mapping(target = "localPeer", ignore = true)
    @Mapping(target = "parameters", ignore = true)
    @Mapping(target = "realms", ignore = true)
    @Mapping(target = "peers", ignore = true)
    DiameterGateway toDiameterGatewayEntity(DiameterGatewayDTO diameterGatewayDTO);
    List<DiameterGatewayDTO> toDiameterGatewayDTOList(List<DiameterGateway> diameterGateways);
    DiameterGatewayDTO toDiameterGatewayDTO(DiameterGateway diameterGateway);

    @Mapping(target = "diameterLocalPeer", ignore = true)
    @Mapping(target = "diameterRealm", ignore = true)
    DiameterApplication toDiameterApplicationEntity(ApplicationDTO diameterApplicationDTO);
    ApplicationDTO toDiameterApplicationDTO(DiameterApplication diameterApplication);

    @Mapping(target = "diameterGateway", ignore = true)
    @Mapping(target = "applications", ignore = true)
    DiameterLocalPeer toDiameterLocalPeerEntity(LocalPeerDTO diameterLocalPeerDTO);
    LocalPeerDTO toDiameterLocalPeerDTO(DiameterLocalPeer diameterLocalPeer);

    @Mapping(target = "diameterGateway", ignore = true)
    DiameterParameters toDiameterParametersEntity(ParametersDTO diameterParametersDTO);
    ParametersDTO toDiameterParametersDTO(DiameterParameters diameterParametersDTO);

    @Mapping(target = "diameterGateway", ignore = true)
    DiameterPeer toDiameterPeerEntity(PeerDTO diameterPeerDTO);
    PeerDTO toDiameterPeerDTO(DiameterPeer diameterPeer);

    @Mapping(target = "diameterGateway", ignore = true)
    @Mapping(target = "application", ignore = true)
    DiameterRealm toDiameterRealmEntity(RealmDTO diameterRealmDTO);
    RealmDTO toDiameterRealmDTO(DiameterRealm diameterRealm);

    @Mapping(target = "applications", ignore = true)
    void updateDiameterLocalPeerEntity(@MappingTarget DiameterLocalPeer diameterLocalPeer, LocalPeerDTO localPeerDTO);
    @Mapping(target = "started", ignore = true)
    void updateDiameterPeerEntity(@MappingTarget DiameterPeer diameterPeer, PeerDTO peerDTO);
    void updateDiameterRealmEntity(@MappingTarget DiameterRealm diameterRealm, RealmDTO realmDTO);
    void updateDiameterApplicationEntity(@MappingTarget DiameterApplication diameterApplication, ApplicationDTO applicationDTO);
    void updateDiameterParametersEntity(@MappingTarget DiameterParameters diameterParameters, ParametersDTO parametersDTO);
}
