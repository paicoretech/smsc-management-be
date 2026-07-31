package com.smsc.management.app.diameter.service;

import com.paicbd.smsc.dto.diameter.DiameterConfig;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.paicbd.smsc.utils.WebsocketConstants;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.diameter.dto.ApplicationDTO;
import com.smsc.management.app.diameter.dto.DiameterGatewayDTO;
import com.smsc.management.app.diameter.dto.LocalPeerDTO;
import com.smsc.management.app.diameter.dto.ParametersDTO;
import com.smsc.management.app.diameter.dto.PeerDTO;
import com.smsc.management.app.diameter.dto.RealmDTO;
import com.smsc.management.app.diameter.mapper.GlobalDiameterMapper;
import com.smsc.management.app.diameter.model.entity.DiameterApplication;
import com.smsc.management.app.diameter.model.entity.DiameterGateway;
import com.smsc.management.app.diameter.model.entity.DiameterLocalPeer;
import com.smsc.management.app.diameter.model.entity.DiameterParameters;
import com.smsc.management.app.diameter.model.entity.DiameterPeer;
import com.smsc.management.app.diameter.model.entity.DiameterRealm;
import com.smsc.management.app.diameter.model.repository.DiameterApplicationRepository;
import com.smsc.management.app.diameter.model.repository.DiameterGatewayRepository;
import com.smsc.management.app.diameter.model.repository.DiameterLocalPeerRepository;
import com.smsc.management.app.diameter.model.repository.DiameterParametersRepository;
import com.smsc.management.app.diameter.model.repository.DiameterPeerRepository;
import com.smsc.management.app.diameter.model.repository.DiameterRealmRepository;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.exception.ResourceNotFoundException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.StaticMethods;
import com.smsc.management.utils.UtilsBase;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiameterManagerService {
    private static final Map<Boolean, String> CHARGING_OR_SMS_WEBSOCKET_ACTIONS =
            Map.of(true, WebsocketConstants.ADD_OR_UPDATE_CHARGING_DIAMETER_GATEWAY,
                    false, WebsocketConstants.ADD_OR_UPDATE_SMS_DIAMETER_GATEWAY);

    private final GlobalDiameterMapper mapper = Mappers.getMapper(GlobalDiameterMapper.class);

    private final UtilsBase utilsBase;
    private final SequenceNetworksIdGenerator networksIdGenerator;

    private final DiameterGatewayRepository repository;
    private final DiameterPeerRepository peersRepository;
    private final DiameterRealmRepository realmsRepository;
    private final DiameterLocalPeerRepository localPeerRepository;
    private final DiameterParametersRepository parametersRepository;
    private final DiameterApplicationRepository applicationsRepository;

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        List<DiameterGateway> gateways = repository.findAllDiameterGateways();
        if (!gateways.isEmpty()) {
            gateways.forEach(gateway -> {
                boolean isForCharging = "OCS".equals(gateway.getType());
                String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
                executeRedisActionsAndSendWebsocketNotification(gateway, isForCharging, false, websocketEndpoint);
            });
        }
    }

    @Transactional
    public ApiResponse createDiameterGateway(DiameterGatewayDTO dto) {
        try {
            log.info("Creating diameter gateway: {}", dto);
            Assert.isNull(dto.getId(), "Diameter gateway must not have an id");
            
            // Calculate messages_per_second as sum of all priority values
            dto.setMessagesPerSecond();
            
            DiameterGateway entity = mapper.toDiameterGatewayEntity(dto);
            boolean isForCharging = "OCS".equals(entity.getType());
            if (isForCharging) {
                int count = repository.countDiameterGatewaysForCharging();
                if (count > 0) {
                    throw new IllegalArgumentException("Only one OCS diameter gateway is allowed");
                }
            } else {
                entity.setNetworkId(networksIdGenerator.getNextNetworkIdSequenceValue("GW"));
            }

            repository.save(entity);
            if (StaticMethods.applyForUpdate(dto.getLocalPeer())) {
                dto.getLocalPeer().setId(null);
                DiameterLocalPeer localPeer = saveLocalPeerFromDto(entity.getId(), dto.getLocalPeer());
                entity.setLocalPeerId(localPeer.getId());
                entity.setLocalPeer(localPeer);
            }

            if (StaticMethods.applyForUpdate(dto.getParameters())) {
                dto.getParameters().setId(null);
                DiameterParameters parameters = saveParametersFromDto(entity.getId(), dto.getParameters());
                entity.setParametersId(parameters.getId());
                entity.setParameters(parameters);
            }

            if (StaticMethods.applyForUpdate(dto.getRealms())) {
                dto.getRealms().forEach(realm -> realm.setId(null));
                Set<DiameterRealm> realms = saveRealmsFromDto(entity.getId(), dto.getRealms());
                entity.setRealms(realms);
            }

            if (StaticMethods.applyForUpdate(dto.getPeers())) {
                dto.getPeers().forEach(peer -> peer.setId(null));
                Set<DiameterPeer> peers = savePeersFromDto(entity.getId(), dto.getPeers());
                entity.setPeers(peers);
            }

            repository.save(entity);
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter gateway created: {}", entity);
            return ResponseMapping.successMessage("Diameter gateway created successfully with id: " + entity.getId() + " and networkId: " + entity.getNetworkId(), "OK");
        } catch (Exception e) {
            log.error("An error occurred while creating diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while creating diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse updateDiameterGatewayById(Integer id, DiameterGatewayDTO dto) {
        try {
            log.info("Updating diameter gateway: {}", dto);
            DiameterGateway entity = repository.findDiameterGatewayById(id);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("updateDiameterGatewayById: diameter gateway not found with id: " + id + " and networkId: " + dto.getNetworkId());
            }
            
            // Calculate messages_per_second as sum of all priority values
            dto.setMessagesPerSecond();
            
            updateEntityFromGlobalDto(entity, dto);
            repository.save(entity);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter gateway updated: {}", entity);
            return ResponseMapping.successMessage("Diameter gateway updated successfully with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while updating diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while updating diameter gateway", e);
        }

    }

    @Transactional
    public ApiResponse removeDiameterGatewayById(Integer id) {
        try {
            log.info("Deleting diameter gateway by id: {}", id);
            DiameterGateway entity = repository.findDiameterGatewayById(id);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While deleting, diameter gateway not found with id: " + id);
            }

            entity.setDeleted(true);
            entity.setStarted(false);
            repository.save(entity);
            entity.getPeers().forEach(peer -> {
                peer.setStarted(false);
                peersRepository.save(peer);
            });

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = WebsocketConstants.REMOVE_DIAMETER_GATEWAY;
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, true, websocketEndpoint);

            log.debug("Diameter gateway deleted: {}", entity);
            return ResponseMapping.successMessage("Diameter gateway deleted successfully with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while deleting diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while deleting diameter gateway", e);
        }
    }

    public ApiResponse getDiameterGatewayById(Integer id) {
        try {
            log.info("Getting diameter gateway by id: {}", id);
            DiameterGateway entity = repository.findDiameterGatewayById(id);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While fetching, diameter gateway not found with id: " + id);
            }
            DiameterGatewayDTO dto = mapper.toDiameterGatewayDTO(entity);

            log.debug("Diameter gateway retrieved: {}", dto);
            return ResponseMapping.successMessage("Diameter gateway fetched successfully with id: " + entity.getId(), dto);
        } catch (Exception e) {
            log.error("An error occurred while fetching diameter gateway by id: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while fetching diameter gateway by id", e);
        }

    }

    public ApiResponse getChargingGateway() {
        try {
            log.info("Getting OCS diameter gateway");
            DiameterGateway entity = repository.findOCSGateway();
            if (Objects.isNull(entity)) {
                log.error("getChargingGateway >>> No OCS diameter gateway found");
                return ResponseMapping.errorMessageNoFound("No OCS diameter gateway found");
            }

            DiameterGatewayDTO dto = mapper.toDiameterGatewayDTO(entity);

            log.debug("OCS diameter gateway retrieved: {}", dto);
            return ResponseMapping.successMessage("OCS diameter gateway fetched successfully with id: " + entity.getId(), dto);
        } catch (Exception e) {
            log.error("An error occurred while fetching diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while fetching diameter gateway", e);
        }
    }

    public ApiResponse getAllDiameterGateways() {
        try {
            log.info("Getting all diameter gateways");
            List<DiameterGateway> entities = repository.findGateways();
            if (entities.isEmpty()) {
                return ResponseMapping.errorMessageNoFound("No diameter gateways found");
            }

            List<DiameterGatewayDTO> dtos = mapper.toDiameterGatewayDTOList(entities);

            log.debug("All diameter gateways IP SM GW retrieved: {}", dtos);
            return ResponseMapping.successMessage("All diameter gateways fetched successfully", dtos);
        } catch (Exception e) {
            log.error("An error occurred while fetching all diameter gateways IP SM GW {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while fetching all diameter gateways", e);
        }
    }


    public ApiResponse getAllDiameterGatewaysIpSmGw() {
        try {
            log.info("Getting all diameter gateways ip sm gw");
            List<DiameterGateway> entities = repository.findGatewaysIpSmGw();
            if (entities.isEmpty()) {
                return ResponseMapping.errorMessageNoFound("No diameter gateways found");
            }

            List<DiameterGatewayDTO> dtos = mapper.toDiameterGatewayDTOList(entities);

            log.debug("All diameter gateways retrieved: {}", dtos);
            return ResponseMapping.successMessage("All diameter gateways fetched successfully", dtos);
        } catch (Exception e) {
            log.error("An error occurred while fetching all diameter gateways {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while fetching all diameter gateways", e);
        }
    }

    @Transactional
    public ApiResponse startStopDiameterGateway(Integer id, boolean start) {
        try {
            String message = start ? "started" : "stopped";
            log.info("{} diameter gateway by id: {}", message, id);
            DiameterGateway entity = repository.findDiameterGatewayById(id);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While starting/stopping, diameter gateway not found with id: " + id);
            }
            entity.setStarted(start);
            repository.save(entity);

            entity.getPeers().forEach(peer -> {
                peer.setStarted(start);
                peersRepository.save(peer);
            });

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = start ?
                    WebsocketConstants.START_DIAMETER_GATEWAY :
                    WebsocketConstants.STOP_DIAMETER_GATEWAY;
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter gateway started: {}", entity);
            return ResponseMapping.successMessage("Diameter gateway started successfully with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while starting diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while starting diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse startStopPeer(Integer peerId, boolean start) {
        try {
            String message = start ? "started" : "stopped";
            log.info("{} peer by id: {}", message, peerId);

            DiameterPeer peer = Optional.ofNullable(peersRepository.findDiameterPeerById(peerId))
                    .orElseThrow(() -> new ResourceNotFoundException("While starting/stopping, peer not found with id: " + peerId));

            peer.setStarted(start);
            peersRepository.save(peer);

            DiameterGateway diameterGatewayEntity = repository.findDiameterGatewayById(peer.getDiameterGatewayId());
            boolean isForCharging = "OCS".equals(diameterGatewayEntity.getType());
            String websocketEndpoint = start ?
                    WebsocketConstants.START_PEER_FOR_DIAMETER_GATEWAY :
                    WebsocketConstants.STOP_PEER_FOR_DIAMETER_GATEWAY;
            websocketEndpoint = websocketEndpoint + "-" + diameterGatewayEntity.getId() + "|" + peerId;
            executeRedisActionsAndSendWebsocketNotification(
                    diameterGatewayEntity, isForCharging, false, websocketEndpoint);

            log.debug("Peer {} in diameter gateway started: {}", message, diameterGatewayEntity);
            return ResponseMapping.successMessage("Peer " + message + " successfully with id: " + peerId, null);
        } catch (Exception e) {
            log.error("An error occurred while starting peer diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while starting peer diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse addDiameterRealmToDiameterGateway(Integer diameterGatewayId, RealmDTO realmDTO) {
        try {
            log.info("Adding diameter realm to diameter gateway with id: {}", diameterGatewayId);
            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While adding realm, diameter gateway not found with id: " + diameterGatewayId);
            }

            Assert.isNull(realmDTO.getId(), "Diameter realm must not have an id");
            realmDTO.setDiameterGatewayId(diameterGatewayId);
            DiameterRealm diameterRealm = mapper.toDiameterRealmEntity(realmDTO);
            entity.addRealm(diameterRealm);
            realmsRepository.save(diameterRealm);
            processRealmAndApplicationUpdate(realmDTO, diameterRealm);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter realm added to diameter gateway: {}", entity);
            return ResponseMapping.successMessage("Diameter realm added successfully to gateway with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while adding diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while adding diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse updateDiameterRealmToDiameterGateway(Integer realmId, RealmDTO realmDTO) {
        try {
            log.info("Updating diameter realm with id: {}", realmId);

            Integer diameterGatewayId = realmsRepository.findDiameterGatewayIdByRealmId(realmId);
            Assert.notNull(diameterGatewayId, "The realmId not exists or is not associated with any gateway");

            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            DiameterRealm realm = entity.getRealms().stream()
                    .filter(r -> r.getId().equals(realmId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("While updating, realm not found with id: " + realmId));

            mapper.updateDiameterRealmEntity(realm, realmDTO);
            realm.setDiameterGatewayId(diameterGatewayId);
            processRealmAndApplicationUpdate(realmDTO, realm);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter realm updated: {}", entity);
            return ResponseMapping.successMessage("Diameter realm updated successfully with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while updating diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while updating diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse removeDiameterRealmFromDiameterGateway(Integer realmId) {
        try {
            log.info("Removing diameter realm with id: {}", realmId);

            Integer diameterGatewayId = realmsRepository.findDiameterGatewayIdByRealmId(realmId);
            Assert.notNull(diameterGatewayId, "The realm not exists or is not associated with any gateway");

            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            DiameterRealm realm = entity.getRealms().stream()
                    .filter(r -> r.getId().equals(realmId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("While removing, realm not found with id: " + realmId));

            entity.removeRealm(realm);
            realmsRepository.delete(realm);
            applicationsRepository.delete(realm.getApplication());

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter realm removed: {}", entity);
            return ResponseMapping.successMessage("Diameter realm removed successfully with id: " + realmId, null);
        } catch (Exception e) {
            log.error("An error occurred while removing diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while removing diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse addDiameterPeerToDiameterGateway(Integer diameterGatewayId, PeerDTO peerDTO) {
        try {
            log.info("Adding diameter peer to diameter gateway with id: {}", diameterGatewayId);
            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While adding peer, diameter gateway not found with id: " + diameterGatewayId);
            }

            DiameterPeer peer = mapper.toDiameterPeerEntity(peerDTO);
            peer.setDiameterGatewayId(diameterGatewayId);
            peer.setStarted(false);
            entity.addPeer(peer);
            peersRepository.save(peer);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter peer added to diameter gateway: {}", entity);
            return ResponseMapping.successMessage("Diameter peer added successfully, new id is: " + peer.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while adding peer diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while adding peer diameter gateway", e);
        }
    }

    @Transactional
    public ApiResponse updateDiameterPeerToDiameterGateway(Integer peerId, PeerDTO peer) {
        try {
            log.info("Updating diameter peer with id: {}", peerId);

            Integer diameterGatewayId = peersRepository.findDiameterGatewayIdByPeerId(peerId);
            Assert.notNull(diameterGatewayId, "The peerId not exists or is not associated with any gateway");

            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            DiameterPeer peerEntity = entity.getPeers().stream()
                    .filter(p -> p.getId().equals(peerId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("While updating, peer not found with id: " + peerId));

            mapper.updateDiameterPeerEntity(peerEntity, peer);
            peerEntity.setDiameterGatewayId(diameterGatewayId);
            peerEntity.setId(peerId);
            peersRepository.save(peerEntity);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter peer updated: {}", entity);
            return ResponseMapping.successMessage("Diameter peer updated successfully with id: " + peerId, null);
        } catch (Exception e) {
            log.error("An error occurred while updating peer diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while updating peer gateway", e);
        }
    }

    @Transactional
    public ApiResponse removeDiameterPeerFromDiameterGateway(Integer peerId) {
        try {
            log.info("Removing diameter peer with id: {}", peerId);

            Integer diameterGatewayId = peersRepository.findDiameterGatewayIdByPeerId(peerId);
            Assert.notNull(diameterGatewayId, "The peerId not exists or is not associated with any gateway");

            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            DiameterPeer peer = entity.getPeers().stream()
                    .filter(p -> p.getId().equals(peerId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("While removing, peer not found with id: " + peerId));

            entity.removePeer(peer);
            peersRepository.delete(peer);

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Diameter peer removed: {}", entity);
            return ResponseMapping.successMessage("Diameter peer removed successfully with id: " + peerId, null);
        } catch (Exception e) {
            log.error("An error occurred while removing peer diameter gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while removing peer gateway", e);
        }
    }

    @Transactional
    public ApiResponse updateLocalPeerForDiameterGateway(Integer diameterGatewayId, LocalPeerDTO localPeerDTO) {
        try {
            log.info("Updating local peer for diameter gateway with id: {}", diameterGatewayId);
            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While updating, diameter gateway not found with id: " + diameterGatewayId);
            }

            localPeerDTO.setIpAddresses(localPeerDTO.getIpAddresses().replaceAll("\\s", ""));
            localPeerDTO.setId(entity.getLocalPeerId());
            localPeerDTO.setDiameterGatewayId(diameterGatewayId);
            mapper.updateDiameterLocalPeerEntity(entity.getLocalPeer(), localPeerDTO);

            Set<ApplicationDTO> applicationDTOS = localPeerDTO.getApplications();
            if (Objects.nonNull(applicationDTOS) && !applicationDTOS.isEmpty()) {
                log.debug("Applications found: {}", applicationDTOS);
                processLocalPeerApplicationsUpdate(entity, applicationDTOS);
            }

            localPeerRepository.save(entity.getLocalPeer());

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Local peer updated for diameter gateway: {}", entity);
            return ResponseMapping.successMessage("Local peer updated successfully for gateway with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("An error occurred while updating local peer gateway: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while updating local peer gateway", e);
        }
    }

    @Transactional
    public ApiResponse updateParametersForDiameterGateway(Integer diameterGatewayId, ParametersDTO parametersDTO) {
        try {
            log.info("Updating parameters for diameter gateway with id: {}", diameterGatewayId);
            DiameterGateway entity = repository.findDiameterGatewayById(diameterGatewayId);
            if (Objects.isNull(entity)) {
                return ResponseMapping.errorMessageNoFound("While updating, diameter gateway not found with id: " + diameterGatewayId);
            }

            parametersDTO.setId(entity.getParametersId());
            parametersDTO.setDiameterGatewayId(diameterGatewayId);
            mapper.updateDiameterParametersEntity(entity.getParameters(), parametersDTO);
            parametersRepository.save(entity.getParameters());

            boolean isForCharging = "OCS".equals(entity.getType());
            String websocketEndpoint = CHARGING_OR_SMS_WEBSOCKET_ACTIONS.get(isForCharging);
            executeRedisActionsAndSendWebsocketNotification(
                    entity, isForCharging, false, websocketEndpoint);

            log.debug("Parameters updated for diameter gateway: {}", entity);
            return ResponseMapping.successMessage("Parameters updated successfully for gateway with id: " + entity.getId(), null);
        } catch (Exception e) {
            log.error("Update parameters for diameter gateway with id: {}", diameterGatewayId);
            return ResponseMapping.exceptionMessage("Update parameters for gateway with id: " + diameterGatewayId, e);
        }
    }

    private DiameterLocalPeer saveLocalPeerFromDto(Integer diameterGatewayId, LocalPeerDTO localPeerDTO) {
        DiameterLocalPeer localPeer = mapper.toDiameterLocalPeerEntity(localPeerDTO);
        localPeer.setDiameterGatewayId(diameterGatewayId);
        localPeerRepository.save(localPeer);

        if (Objects.nonNull(localPeerDTO.getApplications())) {
            localPeerDTO.getApplications().forEach(application -> {
                application.setId(null);
                DiameterApplication app = mapper.toDiameterApplicationEntity(application);
                app.setDiameterLocalPeerId(localPeer.getId());
                applicationsRepository.save(app);

                localPeer.addApplication(app);
            });
        }

        return localPeer;
    }

    private DiameterParameters saveParametersFromDto(Integer diameterGatewayId, ParametersDTO parametersDTO) {
        DiameterParameters parameters = mapper.toDiameterParametersEntity(parametersDTO);
        parameters.setDiameterGatewayId(diameterGatewayId);
        parametersRepository.save(parameters);
        return parameters;
    }

    private Set<DiameterRealm> saveRealmsFromDto(Integer diameterGatewayId, Set<RealmDTO> realms) {
        return realms.stream()
                .map(realm -> {
                    DiameterRealm realmEntity = mapper.toDiameterRealmEntity(realm);
                    realmEntity.setDiameterGatewayId(diameterGatewayId);
                    realmsRepository.save(realmEntity);

                    processRealmAndApplicationUpdate(realm, realmEntity);
                    return realmEntity;
                })
                .collect(Collectors.toSet());
    }

    private Set<DiameterPeer> savePeersFromDto(Integer diameterGatewayId, Set<PeerDTO> peers) {
        return peers.stream()
                .map(peer -> {
                    DiameterPeer peerEntity = mapper.toDiameterPeerEntity(peer);
                    peerEntity.setDiameterGatewayId(diameterGatewayId);
                    peersRepository.save(peerEntity);
                    return peerEntity;
                })
                .collect(Collectors.toSet());
    }

    private void processLocalPeerApplicationsUpdate(DiameterGateway entity, Set<ApplicationDTO> applicationDTOS) {
        applicationDTOS.forEach(applicationDTO -> {
            if (applicationDTO.getId() == null) {
                DiameterApplication application = mapper.toDiameterApplicationEntity(applicationDTO);
                application.setDiameterLocalPeerId(entity.getLocalPeer().getId());
                entity.getLocalPeer().addApplication(application);
                applicationsRepository.save(application);
            } else {
                if (applicationDTO.isDelete()) {
                    DiameterApplication applicationToDelete = entity.getLocalPeer().removeApplicationById(applicationDTO.getId());
                    applicationsRepository.delete(applicationToDelete);
                } else {
                    entity.getLocalPeer().getApplications().stream()
                            .filter(app -> app.getId().equals(applicationDTO.getId()))
                            .findFirst()
                            .ifPresent(app -> {
                                mapper.updateDiameterApplicationEntity(app, applicationDTO);
                                applicationsRepository.save(app);
                            });
                }
            }
        });
    }

    private void updateEntityFromGlobalDto(DiameterGateway entity, DiameterGatewayDTO dto) {
        entity.setName(dto.getName());
        entity.setConnectionType(dto.getConnectionType());
        entity.setSplitMessage(dto.isSplitMessage());
        entity.setGlobalTitle(dto.getGlobalTitle());
        entity.setHssUpdateEnabled(dto.isHssUpdateEnabled());
        entity.setMnoId(dto.getMnoId());
        entity.setAllowedTraffic(dto.isAllowedTraffic());
        entity.setMessagesPerSecondHigh(dto.getMessagesPerSecondHigh());
        entity.setMessagesPerSecondMedium(dto.getMessagesPerSecondMedium());
        entity.setMessagesPerSecondLow(dto.getMessagesPerSecondLow());
        entity.setMessagesPerSecond(dto.getMessagesPerSecond());

        if (StaticMethods.applyForUpdate(dto.getType())) {
            processTypeUpdates(entity, dto);
        }

        if (StaticMethods.applyForUpdate(dto.getPeers())) {
            processPeerUpdates(entity, dto);
        }
        if (StaticMethods.applyForUpdate(dto.getRealms())) {
            processRealmUpdates(entity, dto);
        }
        if (StaticMethods.applyForUpdate(dto.getLocalPeer())) {
            processLocalPeerUpdates(entity, dto);
        }
        if (StaticMethods.applyForUpdate(dto.getParameters())) {
            processUpdateParametersUpdates(entity, dto);
        }
    }

    private void processTypeUpdates(DiameterGateway entity, DiameterGatewayDTO dto) {
        if (!Objects.equals(entity.getType(), dto.getType())) {
            String newType = dto.getType();
            entity.setType(dto.getType());

            if (Objects.equals("OCS", newType)) {
                entity.setNetworkId(networksIdGenerator.getNextNetworkIdSequenceValue("GW"));
            } else {
                entity.setNetworkId(null);
            }
        }
    }

    private void processPeerUpdates(DiameterGateway entity, DiameterGatewayDTO dto) {
        dto.getPeers().forEach(peer -> {
            if (peer.getId() == null) {
                DiameterPeer peerEntity = mapper.toDiameterPeerEntity(peer);
                peerEntity.setDiameterGatewayId(entity.getId());
                peersRepository.save(peerEntity);
            } else {
                if (peer.isDelete()) {
                    DiameterPeer peerToDelete = entity.removePeerById(peer.getId());
                    entity.removePeer(peerToDelete);
                    peersRepository.delete(peerToDelete);
                } else {
                    entity.getPeers().stream()
                            .filter(p -> p.getId().equals(peer.getId()))
                            .findFirst()
                            .ifPresent(p -> {
                                mapper.updateDiameterPeerEntity(p, peer);
                                p.setDiameterGatewayId(entity.getId());
                                peersRepository.save(p);
                            });
                }
            }
        });
    }

    private void processRealmUpdates(DiameterGateway entity, DiameterGatewayDTO dto) {
        dto.getRealms().forEach(realm -> {
            if (realm.getId() == null) {
                DiameterRealm realmEntity = mapper.toDiameterRealmEntity(realm);
                realmEntity.setDiameterGatewayId(entity.getId());
                realmsRepository.save(realmEntity);
                processRealmAndApplicationUpdate(realm, realmEntity);
            } else {
                if (realm.isDelete()) {
                    DiameterRealm realmToDelete = entity.removeRealmById(realm.getId());
                    entity.removeRealm(realmToDelete);
                    realmsRepository.delete(realmToDelete);
                } else {
                    entity.getRealms().stream()
                            .filter(r -> r.getId().equals(realm.getId()))
                            .findFirst()
                            .ifPresent(r -> {
                                mapper.updateDiameterRealmEntity(r, realm);
                                r.setDiameterGatewayId(entity.getId());
                                processRealmAndApplicationUpdate(realm, r);
                            });
                }
            }
        });
    }

    private void processRealmAndApplicationUpdate(RealmDTO realm, DiameterRealm r) {
        if (Objects.nonNull(realm.getApplication())) {
            DiameterApplication app = mapper.toDiameterApplicationEntity(realm.getApplication());
            app.setDiameterRealmId(r.getId());
            applicationsRepository.save(app);

            r.setApplicationId(app.getId());
            r.setApplication(app);
        }

        realmsRepository.save(r);
    }

    private void processLocalPeerUpdates(DiameterGateway entity, DiameterGatewayDTO dto) {
        Set<ApplicationDTO> applicationsInDto = dto.getLocalPeer().getApplications();
        if (StaticMethods.applyForUpdate(applicationsInDto)) {
            processLocalPeerApplicationsUpdate(entity, applicationsInDto);
        }

        if (StaticMethods.applyForUpdate(dto.getLocalPeer())) {
            mapper.updateDiameterLocalPeerEntity(entity.getLocalPeer(), dto.getLocalPeer());
            localPeerRepository.save(entity.getLocalPeer());
        }
    }

    private void processUpdateParametersUpdates(DiameterGateway entity, DiameterGatewayDTO dto) {
        mapper.updateDiameterParametersEntity(entity.getParameters(), dto.getParameters());
        parametersRepository.save(entity.getParameters());
    }

    // Due to the gateways could be or not have a networkId, the management for diameter must be by id, not by networkId
    private void executeRedisActionsAndSendWebsocketNotification(
            DiameterGateway diameterGateway,
            boolean isChargingGateway,
            boolean isForDelete,
            String webSocketEndpoint
    ) {
        String hashName = isChargingGateway ?
                GeneralSmscConstants.CONFIGURATIONS_HASH_NAME :
                GeneralSmscConstants.DIAMETER_GATEWAYS_HASH_NAME;
        String key = isChargingGateway ?
                GeneralSmscConstants.CONFIGURATIONS_CHARGING_KEY :
                String.valueOf(diameterGateway.getId());

        if (isForDelete) {
            utilsBase.removeInRedis(hashName, key);
        } else {
            DiameterConfig diameterConfig = diameterGateway.toDiameterConfig();
            String stringDiameterConfig = Converter.valueAsString(diameterConfig);
            utilsBase.storeInRedis(hashName, key, stringDiameterConfig);
        }

        String websocketBody = diameterGateway.getId().toString();
        if (wsEndpointIsForStartOrStopPeer(webSocketEndpoint)) {
            log.info("Sending notification to websocket endpoint: {}", webSocketEndpoint);
            String[] parts = webSocketEndpoint.split("-");
            Assert.isTrue(parts.length == 2, "Invalid websocket endpoint for start/stop peer");
            webSocketEndpoint = parts[0];
            websocketBody = parts[1];
        }

        log.info("Sending websocket notification for diameter gateway id:{} ; endpoint:{} ; body:{}", diameterGateway.getId(), webSocketEndpoint, websocketBody);
        utilsBase.sendNotificationSocket(webSocketEndpoint, websocketBody);
    }

    private boolean wsEndpointIsForStartOrStopPeer(String websocketEndpoint) {
        return websocketEndpoint.contains(WebsocketConstants.START_PEER_FOR_DIAMETER_GATEWAY) ||
                websocketEndpoint.contains(WebsocketConstants.STOP_PEER_FOR_DIAMETER_GATEWAY);
    }
}
