package com.smsc.management.app.ss7.service;

import java.util.List;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.RedisSs7DTO;
import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.mapper.HomeRoutingMapper;
import com.smsc.management.app.ss7.mapper.M3uaMapper;
import com.smsc.management.app.ss7.mapper.MapMapper;
import com.smsc.management.app.ss7.mapper.SccpMapper;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.mapper.TcapMapper;
import com.smsc.management.app.ss7.model.entity.HomeRouting;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import com.smsc.management.app.ss7.model.entity.Map;
import com.smsc.management.app.ss7.model.entity.Sccp;
import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;
import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.entity.Tcap;
import com.smsc.management.app.ss7.model.repository.HomeRoutingRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAppServersRouteRepository;
import com.smsc.management.app.ss7.model.repository.M3uaApplicationServerRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssAppServersRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssociationsRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRoutesRepository;
import com.smsc.management.app.ss7.model.repository.M3uaSocketsRepository;
import com.smsc.management.app.ss7.model.repository.MapRepository;
import com.smsc.management.app.ss7.model.repository.SccpAddressesRepository;
import com.smsc.management.app.ss7.model.repository.SccpLongMessageRulesRepository;
import com.smsc.management.app.ss7.model.repository.SccpMtp3DestinationsRepository;
import com.smsc.management.app.ss7.model.repository.SccpRemoteResourcesRepository;
import com.smsc.management.app.ss7.model.repository.SccpRepository;
import com.smsc.management.app.ss7.model.repository.SccpRulesRepository;
import com.smsc.management.app.ss7.model.repository.SccpServiceAccessPointsRepository;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.model.repository.TcapRepository;
import com.smsc.management.exception.SmscBackendException;
import org.springframework.stereotype.Service;
import static com.smsc.management.utils.Constants.SS7_HOT_RELOAD_ENDPOINT;
import static com.smsc.management.utils.Constants.SS7_SETTINGS_UPDATE_ENDPOINT;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ObjectSs7Service {
    /*
     * Gateways SS7
     */
    private final Ss7GatewaysRepository ss7GatewayRepo;
    private final Ss7GatewaysMapper ss7GatewayMapper;
    private final SipGatewaysRepository sipGatewaysRepository;

    /*
     * M3UA
     */
    private final M3uaRepository m3uaRepo;
    private final M3uaSocketsRepository m3uaSocketRepo;
    private final M3uaAssociationsRepository m3uaAssociationsRepo;
    private final M3uaApplicationServerRepository m3uaAppServerRepo;
    private final M3uaAssAppServersRepository m3uaAssAppServerRepo;
    private final M3uaRoutesRepository m3uaRouteRepo;
    private final M3uaAppServersRouteRepository m3uaAppServerRouteRepo;
    private final M3uaMapper m3uaMapper;

    /*
     * SCCP
     */
    private final SccpRepository sccpRepo;
    private final SccpRemoteResourcesRepository sccpRemoteResourcesRepo;
    private final SccpServiceAccessPointsRepository sccpSapRepo;
    private final SccpMtp3DestinationsRepository sccpMtp3DestRepo;
    private final SccpLongMessageRulesRepository sccpLongMessageRulesRepo;
    private final SccpAddressesRepository sccpAddressRepo;
    private final SccpRulesRepository sccpRulesRepo;
    private final SccpMapper sccpMapper;

    /*
     * MAP
     */
    private final MapRepository mapRepository;
    private final MapMapper mapMapper;

    /*
     * TCAP
     */
    private final TcapRepository tcapRepository;
    private final TcapMapper tcapMapper;
    private final UtilsBase utilsBase;

    /*
     * HR
     */
    private final HomeRoutingRepository homeRoutingRepo;
    private final HomeRoutingMapper homeRoutingMapper;

    public ApiResponse refreshingSettingSs7Gateway(Object id) {
        try {
            this.updateOrCreateJsonInRedis(id);
            return ResponseMapping.successMessage("success", null);
        } catch (Exception e) {
            log.error("Error to create JSON SS7 -> {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Error to create JSON SS7", e);
        }
    }

    /**
     * Sends structured hot reload notification for specific SCCP module
     * 
     * @param networkId The network ID of the gateway
     * @param layer The SS7 layer (e.g., "SCCP")
     * @param module The specific module (e.g., "RESOURCES", "ADDRESSES", etc.)
     */
    public ApiResponse sendSccpHotReloadNotification(int networkId, String layer, String module) {
        try {
            Ss7Gateways gateway = ss7GatewayRepo.findByNetworkId(networkId);
            if (gateway == null) {
                log.warn("Gateway not found for networkId: {}", networkId);
                return ResponseMapping.errorMessageNoFound("Gateway not found for networkId: " + networkId);
            }
            
            if (gateway.getEnabled() != com.smsc.management.utils.Constants.ENABLED) {
                log.debug("Gateway {} is not started, skipping hot reload notification", gateway.getName());
                return ResponseMapping.errorMessage("Gateway is not started, cannot trigger hot reload");
            }

            // Refresh gateway configuration in Redis before triggering hot reload
            refreshingSettingSs7Gateway(networkId);
            
            // Create message in required format: networkId-layer-module
            String message = String.format("%s-%s-%s", networkId, layer, module);
            
            log.info("Sending SCCP hot reload notification for gateway {} - {}", gateway.getName(), module);
            utilsBase.sendNotificationSocket(SS7_HOT_RELOAD_ENDPOINT, message);
            
            return ResponseMapping.successMessage("Hot reload triggered successfully", null);
            
        } catch (Exception e) {
            log.error("Error sending SCCP hot reload notification for networkId {}: {}", networkId, e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Failed to trigger hot reload", e);
        }
    }

    public void updateOrCreateJsonInRedis(Object id) throws Exception {
        boolean searchByExternalId = id instanceof String;
        String connectionMessage = searchByExternalId ? "external_id= " : "network_id= ";

        Ss7Gateways gateway = searchByExternalId ? ss7GatewayRepo.findByExternalId((String) id) : ss7GatewayRepo.findByNetworkId((int) id);
        validateObjectValue(gateway, "ss7 gateway");

        M3ua m3ua = searchByExternalId ? m3uaRepo.findByExternalId((String) id) : m3uaRepo.findByNetworkId((int) id); // general
        validateObjectValue(m3ua, "m3ua");
        int m3uaId = m3ua.getId();

        List<M3uaSockets> m3uaSocket = m3uaSocketRepo.findBySs7M3uaId(m3uaId); // socket
        m3uaSocket = validateList(m3uaSocket);
        validateObjectValue(m3uaSocket, "m3ua sockets");

        List<M3uaAssociationsDTO> m3uaAssociations = m3uaAssociationsRepo.fetchM3uaAssociations(m3uaId); // associations
        m3uaAssociations = validateList(m3uaAssociations);
        validateObjectValue(m3uaAssociations, "m3ua associations");

        List<M3uaApplicationServerDTO> m3uaAppServers = m3uaAppServerRepo.fetchM3uaAppServer(m3uaId); // application servers
        m3uaAppServers = validateList(m3uaAppServers);
        validateObjectValue(m3uaAppServers, "m3ua application servers");
        for (M3uaApplicationServerDTO m3uaAppServer : m3uaAppServers) {
            List<Integer> aspFactories = m3uaAssAppServerRepo.fetchAssAppServers(m3uaAppServer.getId());
            m3uaAppServer.setAspFactories(aspFactories);
        }

        List<Integer> m3uaAppServersId = m3uaAppServerRepo.fetchM3uaAppServerId(m3uaId); // route
        List<M3uaRoutesDTO> m3uaRoutes = m3uaRouteRepo.fetchM3uaRoutes(m3uaAppServersId);
        m3uaRoutes = validateList(m3uaRoutes);
        validateObjectValue(m3uaRoutes, "m3ua routes");
        for (M3uaRoutesDTO m3uaRoute : m3uaRoutes) {
            List<Integer> appServers = m3uaAppServerRouteRepo.fetchAppServersRoute(m3uaRoute.getId());
            m3uaRoute.setAppServers(appServers);
        }

        Sccp sccp = searchByExternalId ? sccpRepo.findByExternalId((String) id) : sccpRepo.findByNetworkId((int) id); // general
        validateObjectValue(sccp, "sccp");
        int sccpId = sccp.getId();

        List<SccpRemoteResources> sccpRemoteResources = sccpRemoteResourcesRepo.findBySs7SccpId(sccpId); // remote resources
        sccpRemoteResources = validateList(sccpRemoteResources);
        validateObjectValue(sccpRemoteResources, "sccp remote resources");

        List<SccpServiceAccessPoints> sccpSap = sccpSapRepo.findBySs7SccpId(sccpId); // service access
        sccpSap = validateList(sccpSap);
        validateObjectValue(sccpSap, "sccp service access point");

        List<SccpMtp3DestinationsDTO> mtp3Destinations = sccpMtp3DestRepo.fetchMtp3Destinations(sccpId); // mtp3 destinations
        mtp3Destinations = validateList(mtp3Destinations);
        validateObjectValue(mtp3Destinations, "sccp mtp3 destinations");

        List<SccpLongMessageRulesDTO> longMessageRules = sccpLongMessageRulesRepo.fetchLongMessageRulesBySccpId(sccpId); // long message rules
        longMessageRules = validateList(longMessageRules);

        List<SccpAddresses> address = sccpAddressRepo.findBySs7SccpId(sccpId); // address
        address = validateList(address);
        validateObjectValue(address, "sccp address");

        List<SccpRulesDTO> rules = sccpRulesRepo.fetchSccpRules(sccpId); // rules
        rules = validateList(rules);
        validateObjectValue(rules, "sccp rules");

        Map map = searchByExternalId ? mapRepository.findByExternalId((String) id) : mapRepository.findByNetworkId((int) id); // map
        validateObjectValue(map, "map");

        Tcap tcap = searchByExternalId ? tcapRepository.findByExternalId((String) id) : tcapRepository.findByNetworkId((int) id); // tcap
        validateObjectValue(tcap, "tcap");

        // convert to DTO
        RedisSs7DTO redisSs7DTO = new RedisSs7DTO();
        RedisSs7DTO.RedisM3ua objectM3ua = redisSs7DTO.new RedisM3ua();
        RedisSs7DTO.Redisassociations redisAssociations = redisSs7DTO.new Redisassociations();
        RedisSs7DTO.RedisSccp redisSccp = redisSs7DTO.new RedisSccp();
        RedisSs7DTO.RedisSap redisSap = redisSs7DTO.new RedisSap();

        // gateways
        Ss7GatewaysDTO redisSs7Settings = buildRedisSs7Settings(gateway);
        redisSs7DTO.setName(gateway.getName());
        redisSs7DTO.setNetworkId(gateway.getNetworkId());
        redisSs7DTO.setMnoId(gateway.getMnoId());
        redisSs7DTO.setEnabled(gateway.getEnabled());
        redisSs7DTO.setApiEnabled(gateway.isApiEnabled());
        redisSs7DTO.setHssUpdateEnabled(gateway.isHssUpdateEnabled());
        redisSs7DTO.setAllowedTraffic(gateway.isAllowedTraffic());
        redisSs7DTO.setAllowedUssi(gateway.isAllowedUssi());
        redisSs7DTO.setAppToken(gateway.getAppToken());
        redisSs7DTO.setHomeRouting(gateway.isHomeRouting());
        redisSs7DTO.setMessagesPerSecondHigh(gateway.getMessagesPerSecondHigh());
        redisSs7DTO.setMessagesPerSecondMedium(gateway.getMessagesPerSecondMedium());
        redisSs7DTO.setMessagesPerSecondLow(gateway.getMessagesPerSecondLow());
        redisSs7DTO.setMessagesPerSecond(gateway.getMessagesPerSecond());

        // Home Routing
        HomeRouting homeRouting = searchByExternalId
                ? homeRoutingRepo.findByExternalId((String) id)
                : homeRoutingRepo.findByNetworkId((int) id);
        if (homeRouting != null) {
            validateObjectValue(homeRouting, "home routing");
            HomeRoutingDTO homeRoutingDTO = homeRoutingMapper.toDTO(homeRouting);
            redisSs7DTO.setSettingsHomeRouting(homeRoutingDTO);
        }

        // M3UA DTO
        objectM3ua.setGeneral(m3uaMapper.toDTO(m3ua));
        redisAssociations.setAssociations(m3uaAssociations);
        redisAssociations.setSockets(m3uaMapper.toDTOServerList(m3uaSocket));
        objectM3ua.setAssociations(redisAssociations);
        objectM3ua.setApplicationServers(m3uaAppServers);
        objectM3ua.setRoutes(m3uaRoutes);
        redisSs7DTO.setM3ua(objectM3ua);

        // sccp
        redisSccp.setGeneral(sccpMapper.toDTO(sccp));
        redisSccp.setRemoteResources(sccpMapper.toDTORemoteResources(sccpRemoteResources));
        redisSap.setServiceAccess(sccpMapper.toDTOSap(sccpSap));
        redisSap.setMtp3Destinations(mtp3Destinations);
        redisSap.setLongMessageRules(longMessageRules);
        redisSccp.setServiceAccessPoints(redisSap);
        redisSccp.setAddresses(sccpMapper.toDTOAddress(address));
        redisSccp.setRules(rules);
        redisSs7DTO.setSccp(redisSccp);

        // tcap and map
        redisSs7DTO.setTcap(tcapMapper.toDTO(tcap));
        redisSs7DTO.setMap(mapMapper.toDTO(map));

        log.info("Ss7 Gateway with {} {} was sent to Redis -> {}", connectionMessage, id, redisSs7DTO);
        utilsBase.storeInRedis(GeneralSmscConstants.SS7_GATEWAYS_HASH_NAME, Integer.toString(redisSs7DTO.getNetworkId()), redisSs7DTO.toString());
        utilsBase.storeInRedis(GeneralSmscConstants.SS7_SETTINGS_HASH_NAME, Integer.toString(redisSs7DTO.getNetworkId()), redisSs7Settings.toString());
    }

    public void validateObjectValue(Object data, String objectName) {
        if (data == null || isListEmpty(data)) {
            throw new SmscBackendException("Object " + objectName + " cannot be null");
        }
    }

    public void sendSs7SettingsUpdateNotification(int networkId) {
        try {
            Ss7Gateways gateway = ss7GatewayRepo.findByNetworkId(networkId);
            if (gateway == null) {
                log.error("Gateway not found for networkId: " + networkId);
                return;
            }

            utilsBase.sendNotificationSocket(SS7_SETTINGS_UPDATE_ENDPOINT, Integer.toString(networkId));

            log.info("SS7 settings update triggered successfully");
        } catch (Exception e) {
            log.error("Error sending SS7 settings update for networkId {}: {}", networkId, e.getMessage(), e);

        }
    }

    public void updateSs7SettingsInRedis(int networkId) throws Exception {
        Ss7Gateways gateway = ss7GatewayRepo.findByNetworkId(networkId);
        validateObjectValue(gateway, "ss7 gateway");
        Ss7GatewaysDTO redisSs7Settings = buildRedisSs7Settings(gateway);

        utilsBase.storeInRedis(GeneralSmscConstants.SS7_SETTINGS_HASH_NAME, Integer.toString(networkId), redisSs7Settings.toString());

        log.info("SS7 settings stored in Redis network_id={} -> {}", networkId, redisSs7Settings);
    }

    private Ss7GatewaysDTO buildRedisSs7Settings(Ss7Gateways gateway) {
        Integer sipNetworkId = resolveSipNetworkId(gateway.getNetworkId());

        Ss7GatewaysDTO redisSs7Settings = ss7GatewayMapper.toDTO(gateway);
        redisSs7Settings.setSipNetworkId(sipNetworkId);

        return redisSs7Settings;
    }

    private Integer resolveSipNetworkId(int ss7NetworkId) {
        return sipGatewaysRepository
                .findFirstByRoutingUssiTrafficSs7GatewayId(ss7NetworkId)
                .map(SipGateways::getNetworkId)
                .orElse(null);
    }

    /**
     * Checks if the given object is a list and if it is empty.
     *
     * @param obj The object to check.
     * @return true if the object is a list and is empty; false otherwise.
     */
    public static boolean isListEmpty(Object obj) {
        if (obj instanceof List<?> list) {
            return list.isEmpty();
        }
        return false;
    }

    /**
     * Checks if the list is null or empty. If so, returns null; otherwise, returns the list.
     *
     * @param list The list to check.
     * @param <T>  The type of elements in the list.
     * @return empty list if the list is null or empty; otherwise, returns the list.
     */
    private static <T> List<T> validateList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list;
    }
}
