package com.smsc.management.app.ss7.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.ss7.controller.Ss7GatewaysController;
import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.dto.MapDTO;
import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import com.smsc.management.app.ss7.dto.SccpDTO;
import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRemoteResourcesDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.SccpServiceAccessPointsDTO;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.dto.TcapDTO;
import com.smsc.management.app.ss7.mapper.M3uaMapper;
import com.smsc.management.app.ss7.mapper.MapMapper;
import com.smsc.management.app.ss7.mapper.SccpMapper;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.mapper.TcapMapper;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import com.smsc.management.app.ss7.model.entity.Map;
import com.smsc.management.app.ss7.model.entity.Sccp;
import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;
import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.entity.Tcap;
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
import com.smsc.management.app.ss7.utilsTest.Utils;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.mapper.HomeRoutingMapper;
import com.smsc.management.app.ss7.model.entity.HomeRouting;
import com.smsc.management.app.ss7.model.repository.HomeRoutingRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;

class ObjectSs7ServiceTest extends BaseIntegrationTest {
    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    ObjectSs7Service objectSs7Service;

    /*
     * Gateways SS7
     */
    @Autowired
    Ss7GatewaysMapper ss7GatewaysMapper;
    @MockBean
    Ss7GatewaysRepository ss7GatewaysRepository;

    /*
     * M3UA
     */
    @MockBean
    M3uaRepository m3uaRepo;
    @MockBean
    M3uaSocketsRepository m3uaSocketRepo;
    @MockBean
    M3uaAssociationsRepository m3uaAssociationsRepo;
    @MockBean
    M3uaApplicationServerRepository m3uaAppServerRepo;
    @MockBean
    M3uaAssAppServersRepository m3uaAssAppServerRepo;
    @MockBean
    M3uaRoutesRepository m3uaRouteRepo;
    @MockBean
    M3uaAppServersRouteRepository m3uaAppServerRouteRepo;
    @Autowired
    M3uaMapper m3uaMapper;

    /*
     * SCCP
     */
    @MockBean
    SccpRepository sccpRepo;
    @MockBean
    SccpRemoteResourcesRepository sccpRemoteResourcesRepo;
    @MockBean
    SccpServiceAccessPointsRepository sccpSapRepo;
    @MockBean
    SccpMtp3DestinationsRepository sccpMtp3DestRepo;
    @MockBean
    SccpLongMessageRulesRepository sccpLongMessageRulesRepo;
    @MockBean
    SccpAddressesRepository sccpAddressRepo;
    @MockBean
    SccpRulesRepository sccpRulesRepo;
    @Autowired
    SccpMapper sccpMapper;

    /*
     * MAP
     */
    @MockBean
    MapRepository mapRepository;
    @Autowired
    MapMapper mapMapper;

    /*
     * TCAP
     */
    @MockBean
    TcapRepository tcapRepository;
    @Autowired
    TcapMapper tcapMapper;

    @MockBean
    UtilsBase utilsBase;

    @MockBean
    SipGatewaysRepository sipGatewaysRepository;

    @MockBean
    HomeRoutingRepository homeRoutingRepo;

    @MockBean
    HomeRoutingMapper homeRoutingMapper;


    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void sendToRedisSS7GatewayTest() {
        Ss7GatewaysDTO gatewayDTOMock = Utils.getSs7GatewaysDTO();
        Ss7Gateways gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);

        M3uaDTO m3uaDTOMock = Utils.getM3uatDtoMock();
        M3ua m3uaEntity = m3uaMapper.toEntity(m3uaDTOMock);
        M3uaSocketsDTO m3uaSocketsDTOMock = Utils.getM3uaSocketsMock();
        M3uaSockets m3uaSocketsEntity = m3uaMapper.toEntityServer(m3uaSocketsDTOMock);
        M3uaAssociationsDTO m3uaAssociationsDTOMock = Utils.getM3uaAssociationsDTO();
        M3uaApplicationServerDTO m3uaApplicationServerDTOMock = Utils.getM3uaApplicationServer();
        M3uaRoutesDTO m3uaRoutesDTOMock = Utils.getM3uaRoutesDTO();

        SccpDTO sccpDTOMock = Utils.getSccpDTOMock();
        Sccp sccpEntity = sccpMapper.toEntity(sccpDTOMock);
        SccpRemoteResourcesDTO sccpRemoteResourcesDTOMock = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources sccpRemoteResourcesEntity = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTOMock);
        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTOMock = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints sccpServiceAccessPointsEntity = sccpMapper.toEntitySap(sccpServiceAccessPointsDTOMock);
        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTOMock = Utils.getSccpMtp3DestinationsDTOMock();
        SccpLongMessageRulesDTO sccpLongMessageRulesDTOMock = Utils.getSccpLongMessageRulesDTOMock();
        SccpAddressesDTO sccpAddressesDTOMock = Utils.getSccpAddressesDTOMock();
        SccpAddresses sccpAddressesEntity = sccpMapper.toEntityAddress(sccpAddressesDTOMock);
        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();

        MapDTO mapDTOMock = Utils.getMapDTOMock();
        Map mapEntity = mapMapper.toEntity(mapDTOMock);

        TcapDTO tcapDTOMock = Utils.getTcapDTOMock();
        Tcap tcapEntity = tcapMapper.toEntity(tcapDTOMock);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);

        Mockito.when(m3uaRepo.findByNetworkId(anyInt())).thenReturn(m3uaEntity);
        Mockito.when(m3uaSocketRepo.findBySs7M3uaId(anyInt())).thenReturn(List.of(m3uaSocketsEntity));
        Mockito.when(m3uaAssociationsRepo.fetchM3uaAssociations(anyInt())).thenReturn(List.of(m3uaAssociationsDTOMock));
        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServer(anyInt())).thenReturn(List.of(m3uaApplicationServerDTOMock));
        Mockito.when(m3uaAssAppServerRepo.fetchAssAppServers(anyInt())).thenReturn(List.of(1));
        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServerId(anyInt())).thenReturn(List.of(1));
        Mockito.when(m3uaRouteRepo.fetchM3uaRoutes(anyList())).thenReturn(List.of(m3uaRoutesDTOMock));
        Mockito.when(m3uaAppServerRouteRepo.fetchAppServersRoute(anyInt())).thenReturn(List.of(1));

        Mockito.when(sccpRepo.findByNetworkId(anyInt())).thenReturn(sccpEntity);
        Mockito.when(sccpRemoteResourcesRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpRemoteResourcesEntity));
        Mockito.when(sccpSapRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpServiceAccessPointsEntity));
        Mockito.when(sccpMtp3DestRepo.fetchMtp3Destinations(anyInt())).thenReturn(List.of(sccpMtp3DestinationsDTOMock));
        Mockito.when(sccpLongMessageRulesRepo.fetchLongMessageRulesBySccpId(anyInt())).thenReturn(List.of(sccpLongMessageRulesDTOMock));
        Mockito.when(sccpAddressRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpAddressesEntity));
        Mockito.when(sccpRulesRepo.fetchSccpRules(anyInt())).thenReturn(List.of(sccpRulesDTO));

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(mapEntity);
        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(tcapEntity);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(anyInt())).thenReturn(Optional.empty());
        Mockito.when(homeRoutingRepo.findByNetworkId(anyInt())).thenReturn(null);
        Mockito.when(homeRoutingRepo.findByExternalId(anyString())).thenReturn(null);

        Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());
        Mockito.doNothing().when(utilsBase).sendNotificationSocket(anyString(), anyString());

        ResponseEntity<ApiResponse> responseController = ss7GatewaysController.updateOrCreateInRedis(1);
        checkAssertions(responseController, HttpStatus.OK, "SS7REFRESH");

        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(gatewaysEntity);
        Mockito.when(m3uaRepo.findByExternalId(anyString())).thenReturn(m3uaEntity);
        Mockito.when(sccpRepo.findByExternalId(anyString())).thenReturn(sccpEntity);
        Mockito.when(mapRepository.findByExternalId(anyString())).thenReturn(mapEntity);
        Mockito.when(tcapRepository.findByExternalId(anyString())).thenReturn(tcapEntity);

        responseController = ss7GatewaysController.updateOrCreateInRedisByExternalId("001");
        checkAssertions(responseController, HttpStatus.OK, "SS7REFRESH");


        gatewayDTOMock = Utils.getSs7GatewaysDTO();
        gatewayDTOMock.setEnabled(1);
        gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);
        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);

        ApiResponse response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkOkAssertions(response);

        gatewayDTOMock = Utils.getSs7GatewaysDTO();
        gatewayDTOMock.setEnabled(2);
        gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);
        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);

        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkOkAssertions(response);

        gatewayDTOMock = Utils.getSs7GatewaysDTO();
        gatewayDTOMock.setEnabled(3);
        gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);
        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);

        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkOkAssertions(response);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);
        Mockito.when(m3uaSocketRepo.findBySs7M3uaId(anyInt())).thenReturn(null);
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(m3uaSocketRepo.findBySs7M3uaId(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(m3uaSocketRepo.findBySs7M3uaId(anyInt())).thenReturn(List.of(m3uaSocketsEntity));
        Mockito.when(m3uaAssociationsRepo.fetchM3uaAssociations(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(m3uaAssociationsRepo.fetchM3uaAssociations(anyInt())).thenReturn(List.of(m3uaAssociationsDTOMock));
        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServer(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServer(anyInt())).thenReturn(List.of(m3uaApplicationServerDTOMock));
        Mockito.when(m3uaRouteRepo.fetchM3uaRoutes(anyList())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(m3uaRouteRepo.fetchM3uaRoutes(anyList())).thenReturn(List.of(m3uaRoutesDTOMock));
        Mockito.when(sccpRemoteResourcesRepo.findBySs7SccpId(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(sccpRemoteResourcesRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpRemoteResourcesEntity));
        Mockito.when(sccpSapRepo.findBySs7SccpId(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(sccpSapRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpServiceAccessPointsEntity));
        Mockito.when(sccpMtp3DestRepo.fetchMtp3Destinations(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(sccpMtp3DestRepo.fetchMtp3Destinations(anyInt())).thenReturn(List.of(sccpMtp3DestinationsDTOMock));
        Mockito.when(sccpAddressRepo.findBySs7SccpId(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);

        Mockito.when(sccpAddressRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpAddressesEntity));
        Mockito.when(sccpRulesRepo.fetchSccpRules(anyInt())).thenReturn(List.of());
        response = objectSs7Service.refreshingSettingSs7Gateway(1);
        checkErrorAssertions(response);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void refreshingSettingSs7GatewayWithSipAssociationAndHomeRoutingTest() {
        Ss7GatewaysDTO gatewayDTOMock = Utils.getSs7GatewaysDTO();
        Ss7Gateways gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);

        M3uaDTO m3uaDTOMock = Utils.getM3uatDtoMock();
        M3ua m3uaEntity = m3uaMapper.toEntity(m3uaDTOMock);
        M3uaSocketsDTO m3uaSocketsDTOMock = Utils.getM3uaSocketsMock();
        M3uaSockets m3uaSocketsEntity = m3uaMapper.toEntityServer(m3uaSocketsDTOMock);
        M3uaAssociationsDTO m3uaAssociationsDTOMock = Utils.getM3uaAssociationsDTO();
        M3uaApplicationServerDTO m3uaApplicationServerDTOMock = Utils.getM3uaApplicationServer();
        M3uaRoutesDTO m3uaRoutesDTOMock = Utils.getM3uaRoutesDTO();

        SccpDTO sccpDTOMock = Utils.getSccpDTOMock();
        Sccp sccpEntity = sccpMapper.toEntity(sccpDTOMock);
        SccpRemoteResourcesDTO sccpRemoteResourcesDTOMock = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources sccpRemoteResourcesEntity = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTOMock);
        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTOMock = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints sccpServiceAccessPointsEntity = sccpMapper.toEntitySap(sccpServiceAccessPointsDTOMock);
        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTOMock = Utils.getSccpMtp3DestinationsDTOMock();
        SccpLongMessageRulesDTO sccpLongMessageRulesDTOMock = Utils.getSccpLongMessageRulesDTOMock();
        SccpAddressesDTO sccpAddressesDTOMock = Utils.getSccpAddressesDTOMock();
        SccpAddresses sccpAddressesEntity = sccpMapper.toEntityAddress(sccpAddressesDTOMock);
        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();

        MapDTO mapDTOMock = Utils.getMapDTOMock();
        Map mapEntity = mapMapper.toEntity(mapDTOMock);

        TcapDTO tcapDTOMock = Utils.getTcapDTOMock();
        Tcap tcapEntity = tcapMapper.toEntity(tcapDTOMock);

        SipGateways sipGateway = Mockito.mock(SipGateways.class);
        HomeRouting homeRouting = Mockito.mock(HomeRouting.class);
        HomeRoutingDTO homeRoutingDTO = Mockito.mock(HomeRoutingDTO.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(anyInt())).thenReturn(gatewaysEntity);

        Mockito.when(m3uaRepo.findByNetworkId(anyInt())).thenReturn(m3uaEntity);
        Mockito.when(m3uaSocketRepo.findBySs7M3uaId(anyInt())).thenReturn(List.of(m3uaSocketsEntity));
        Mockito.when(m3uaAssociationsRepo.fetchM3uaAssociations(anyInt())).thenReturn(List.of(m3uaAssociationsDTOMock));
        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServer(anyInt())).thenReturn(List.of(m3uaApplicationServerDTOMock));
        Mockito.when(m3uaAssAppServerRepo.fetchAssAppServers(anyInt())).thenReturn(List.of(1));
        Mockito.when(m3uaAppServerRepo.fetchM3uaAppServerId(anyInt())).thenReturn(List.of(1));
        Mockito.when(m3uaRouteRepo.fetchM3uaRoutes(anyList())).thenReturn(List.of(m3uaRoutesDTOMock));
        Mockito.when(m3uaAppServerRouteRepo.fetchAppServersRoute(anyInt())).thenReturn(List.of(1));

        Mockito.when(sccpRepo.findByNetworkId(anyInt())).thenReturn(sccpEntity);
        Mockito.when(sccpRemoteResourcesRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpRemoteResourcesEntity));
        Mockito.when(sccpSapRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpServiceAccessPointsEntity));
        Mockito.when(sccpMtp3DestRepo.fetchMtp3Destinations(anyInt())).thenReturn(List.of(sccpMtp3DestinationsDTOMock));
        Mockito.when(sccpLongMessageRulesRepo.fetchLongMessageRulesBySccpId(anyInt())).thenReturn(List.of(sccpLongMessageRulesDTOMock));
        Mockito.when(sccpAddressRepo.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpAddressesEntity));
        Mockito.when(sccpRulesRepo.fetchSccpRules(anyInt())).thenReturn(List.of(sccpRulesDTO));

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(mapEntity);
        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(tcapEntity);

        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(anyInt()))
                .thenReturn(Optional.of(sipGateway));
        Mockito.when(sipGateway.getNetworkId()).thenReturn(999);

        Mockito.when(homeRoutingRepo.findByNetworkId(anyInt())).thenReturn(homeRouting);
        Mockito.when(homeRoutingMapper.toDTO(homeRouting)).thenReturn(homeRoutingDTO);

        Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

        ApiResponse response = objectSs7Service.refreshingSettingSs7Gateway(1);

        checkOkAssertions(response);
        Mockito.verify(sipGatewaysRepository).findFirstByRoutingUssiTrafficSs7GatewayId(anyInt());
        Mockito.verify(homeRoutingRepo).findByNetworkId(anyInt());
        Mockito.verify(homeRoutingMapper).toDTO(homeRouting);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void sendSs7SettingsUpdateNotificationWhenGatewayExists() {
        Ss7Gateways gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(1)).thenReturn(gateway);
        Mockito.doNothing().when(utilsBase).sendNotificationSocket(anyString(), anyString());

        assertDoesNotThrow(() -> objectSs7Service.sendSs7SettingsUpdateNotification(1));

        Mockito.verify(utilsBase).sendNotificationSocket(anyString(), eq("1"));
    }


    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void sendSs7SettingsUpdateNotificationWhenGatewayDoesNotExist() {
        Mockito.when(ss7GatewaysRepository.findByNetworkId(1)).thenReturn(null);

        assertDoesNotThrow(() -> objectSs7Service.sendSs7SettingsUpdateNotification(1));

        Mockito.verify(utilsBase, Mockito.never()).sendNotificationSocket(anyString(), anyString());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void sendSs7SettingsUpdateNotificationWhenSocketFails() {
        Ss7Gateways gateway = Mockito.mock(Ss7Gateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(1)).thenReturn(gateway);
        Mockito.doThrow(new RuntimeException("socket error")).when(utilsBase).sendNotificationSocket(anyString(), anyString());

        Mockito.clearInvocations(utilsBase);

        assertDoesNotThrow(() -> objectSs7Service.sendSs7SettingsUpdateNotification(1));

        Mockito.verify(utilsBase).sendNotificationSocket(anyString(), eq("1"));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSs7SettingsInRedisSuccessfully() throws Exception {
        Ss7GatewaysDTO gatewayDTOMock = Utils.getSs7GatewaysDTO();
        Ss7Gateways gatewaysEntity = ss7GatewaysMapper.toEntity(gatewayDTOMock);

        SipGateways sipGateway = Mockito.mock(SipGateways.class);

        Mockito.when(ss7GatewaysRepository.findByNetworkId(1)).thenReturn(gatewaysEntity);
        Mockito.when(sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(1))
                .thenReturn(Optional.of(sipGateway));
        Mockito.when(sipGateway.getNetworkId()).thenReturn(999);

        Mockito.doNothing().when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

        objectSs7Service.updateSs7SettingsInRedis(1);

        Mockito.verify(sipGatewaysRepository).findFirstByRoutingUssiTrafficSs7GatewayId(1);
        Mockito.verify(utilsBase).storeInRedis(eq(GeneralSmscConstants.SS7_SETTINGS_HASH_NAME), eq("1"), anyString());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSs7SettingsInRedisWhenGatewayDoesNotExist() {
        Mockito.when(ss7GatewaysRepository.findByNetworkId(1)).thenReturn(null);

        assertThrows(SmscBackendException.class, () -> objectSs7Service.updateSs7SettingsInRedis(1));

        Mockito.verify(utilsBase, Mockito.never()).storeInRedis(anyString(), anyString(), anyString());
    }

    public static void checkErrorAssertions(ApiResponse response) {
        assertNotNull(response);
        assertEquals("error", response.message());
        assertEquals(500, response.status());
    }

    public static void checkOkAssertions(ApiResponse response) {
        assertNotNull(response);
        assertEquals("success", response.message());
        assertEquals(200, response.status());
    }
}