package com.smsc.management.app.diameter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paicbd.smsc.dto.diameter.ConnectionType;
import com.smsc.management.app.diameter.dto.ApplicationDTO;
import com.smsc.management.app.diameter.dto.DiameterGatewayDTO;
import com.smsc.management.app.diameter.dto.LocalPeerDTO;
import com.smsc.management.app.diameter.dto.ParametersDTO;
import com.smsc.management.app.diameter.dto.PeerDTO;
import com.smsc.management.app.diameter.dto.RealmDTO;
import com.smsc.management.app.diameter.service.DiameterManagerService;
import com.smsc.management.app.mno.controller.OperatorMnoController;
import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class DiameterManagerControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OperatorMnoController operatorMnoController;
    @Autowired
    private DiameterManagerController diameterManagerController;
    @Autowired
    private DiameterManagerService diameterManagerService;

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void completeFlowTest() {
        DiameterGatewayContext ctx = setupDiameterGatewayAndAssert();
        updateDiameterGatewayAndAssert(ctx);
        getAllDiameterGatewaysAndAssert();
        startStopDiameterGatewayAndAssert(ctx);
        startStopPeerAndAssert(ctx);
        addUpdateAndRemoveRealmAndAssert(ctx);
        addUpdateAndRemovePeerAndAssert(ctx);
        updateParametersAndAssert(ctx);
        updateLocalPeerAndAssert(ctx);
        addSecondOcsGatewayAndAssertFails();
        removeDiameterGatewayAndAssert(ctx);
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void updateLocalPeerWithInvalidIpAddressThenGetError() throws Exception {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("TestMno-IpValidation");
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> responseMno = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(responseMno);
        assertEquals(HttpStatus.OK, responseMno.getStatusCode());
        OperatorMNODTO mno = (OperatorMNODTO) responseMno.getBody().data();

        DiameterGatewayDTO dto = dtoBuilder("GatewayForIpTest", "GATEWAY", mno.getId());
        ResponseEntity<ApiResponse> response = diameterManagerController.addDiameterGateway(dto);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        String[] responseMessage = Objects.requireNonNull(response.getBody()).comment().split(" ");
        Integer gatewayId = Integer.parseInt(responseMessage[responseMessage.length - 4]);

        DiameterGatewayDTO savedGateway = getDiameterGatewayById(gatewayId);
        assertNotNull(savedGateway);

        LocalPeerDTO localPeerDTO = savedGateway.getLocalPeer();
        localPeerDTO.setIpAddresses("not-an-ip");
        mockMvc.perform(put("/diameter/{id}/localPeer/update", gatewayId)
                        .with(user("admin").roles("ADMINISTRATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localPeerDTO)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/diameter/remove/{id}", gatewayId)
                .with(user("admin").roles("ADMINISTRATOR")));
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void updateLocalPeerWithInvalidUriThenGetError() throws Exception {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("TestMno-UriValidation");
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> responseMno = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(responseMno);
        assertEquals(HttpStatus.OK, responseMno.getStatusCode());
        OperatorMNODTO mno = (OperatorMNODTO) responseMno.getBody().data();

        DiameterGatewayDTO dto = dtoBuilder("GatewayForUriTest", "GATEWAY", mno.getId());
        ResponseEntity<ApiResponse> response = diameterManagerController.addDiameterGateway(dto);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        String[] responseMessage = Objects.requireNonNull(response.getBody()).comment().split(" ");
        Integer gatewayId = Integer.parseInt(responseMessage[responseMessage.length - 4]);

        DiameterGatewayDTO savedGateway = getDiameterGatewayById(gatewayId);
        assertNotNull(savedGateway);

        LocalPeerDTO localPeerDTO = savedGateway.getLocalPeer();
        localPeerDTO.setUri("not_a_valid_uri");
        mockMvc.perform(put("/diameter/{id}/localPeer/update", gatewayId)
                        .with(user("admin").roles("ADMINISTRATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(localPeerDTO)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/diameter/remove/{id}", gatewayId)
                .with(user("admin").roles("ADMINISTRATOR")));
    }

    @WithMockUser(roles = "ADMINISTRATOR")
    @Test
    void updateLocalPeerWithMultipleValidIpAddressesThenDoItSuccessfully() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("TestMno-MultiIp");
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> responseMno = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(responseMno);
        assertEquals(HttpStatus.OK, responseMno.getStatusCode());
        OperatorMNODTO mno = (OperatorMNODTO) responseMno.getBody().data();

        DiameterGatewayDTO dto = dtoBuilder("GatewayForMultiIpTest", "GATEWAY", mno.getId());
        ResponseEntity<ApiResponse> response = diameterManagerController.addDiameterGateway(dto);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        String[] responseMessage = Objects.requireNonNull(response.getBody()).comment().split(" ");
        Integer gatewayId = Integer.parseInt(responseMessage[responseMessage.length - 4]);

        DiameterGatewayDTO savedGateway = getDiameterGatewayById(gatewayId);
        assertNotNull(savedGateway);

        LocalPeerDTO localPeerDTO = savedGateway.getLocalPeer();
        localPeerDTO.setIpAddresses("192.168.1.1,10.0.0.1,172.16.0.1");

        ResponseEntity<ApiResponse> updateResponse = diameterManagerController.updateLocalPeer(gatewayId, localPeerDTO);
        assertNotNull(updateResponse);
        assertEquals(200, updateResponse.getStatusCode().value());

        DiameterGatewayDTO updatedGateway = getDiameterGatewayById(gatewayId);
        assertEquals("192.168.1.1,10.0.0.1,172.16.0.1", updatedGateway.getLocalPeer().getIpAddresses());

        diameterManagerController.removeDiameterGateway(gatewayId);
    }

    private DiameterGatewayContext setupDiameterGatewayAndAssert() {
        OperatorMNODTO operatorMNODTO = new OperatorMNODTO();
        operatorMNODTO.setName("Test");
        operatorMNODTO.setTlvMessageReceiptId(true);

        ResponseEntity<ApiResponse> responseMno = operatorMnoController.createOperator(operatorMNODTO);
        assertNotNull(responseMno);
        assertEquals(HttpStatus.OK, responseMno.getStatusCode());
        assertInstanceOf(ApiResponse.class, responseMno.getBody());
        OperatorMNODTO mno = (OperatorMNODTO) responseMno.getBody().data();

        DiameterGatewayDTO dto = dtoBuilder("DiameterGateway", "GATEWAY", mno.getId());
        ResponseEntity<ApiResponse> response = diameterManagerController.addDiameterGateway(dto);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        String[] responseMessage = Objects.requireNonNull(response.getBody()).comment().split(" ");
        Integer gatewayId = Integer.parseInt(responseMessage[responseMessage.length - 4]);
        Integer gatewayNetworkId = Integer.parseInt(responseMessage[responseMessage.length - 1]);
        assertNotNull(gatewayId);
        assertNotNull(gatewayNetworkId);

        DiameterGatewayDTO savedGateway = getDiameterGatewayById(gatewayId);
        assertNotNull(savedGateway);

        return new DiameterGatewayContext(gatewayId, dto);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void updateDiameterGatewayAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO savedGateway = getDiameterGatewayById(ctx.gatewayId());

        savedGateway.setName("UPDATED-DiameterGateway");
        savedGateway.getLocalPeer().setIpAddresses("updated-ip-1,updated-ip-2");
        savedGateway.getLocalPeer().getApplications().forEach(a -> a.setAcctApplId(0));
        savedGateway.getParameters().setRequestTableSize(200);
        savedGateway.getParameters().setRequestTableClearSize(2500);
        savedGateway.getRealms().forEach(r -> {
            r.setLocalAction("UPDATED-LOCAL");
            r.getApplication().setVendorId(10417);
        });
        savedGateway.getPeers().forEach(p -> p.setRating(4));
        savedGateway.setMessagesPerSecondHigh(80);
        savedGateway.setMessagesPerSecondMedium(30);
        savedGateway.setMessagesPerSecondLow(15);
        savedGateway.setMessagesPerSecond();

        ResponseEntity<ApiResponse> updateResp = diameterManagerController.updateDiameterGateway(savedGateway, ctx.gatewayId());
        assertNotNull(updateResp);
        assertEquals(200, updateResp.getStatusCode().value());

        DiameterGatewayDTO updatedGateway = getDiameterGatewayById(ctx.gatewayId());
        assertNotNull(updatedGateway);
        assertEquals(savedGateway.getName(), updatedGateway.getName());
        assertEquals(savedGateway.getLocalPeer().getIpAddresses(), updatedGateway.getLocalPeer().getIpAddresses());
        assertEquals(savedGateway.getParameters().getRequestTableSize(), updatedGateway.getParameters().getRequestTableSize());
        assertEquals(savedGateway.getParameters().getRequestTableClearSize(), updatedGateway.getParameters().getRequestTableClearSize());
        assertEquals(savedGateway.getRealms().stream().findFirst().get().getLocalAction(), updatedGateway.getRealms().stream().findFirst().get().getLocalAction());
        assertEquals(savedGateway.getPeers().stream().findFirst().get().getRating(), updatedGateway.getPeers().stream().findFirst().get().getRating());
        assertEquals(80, updatedGateway.getMessagesPerSecondHigh());
        assertEquals(30, updatedGateway.getMessagesPerSecondMedium());
        assertEquals(15, updatedGateway.getMessagesPerSecondLow());
        assertEquals(125, updatedGateway.getMessagesPerSecond());
    }

    private void getAllDiameterGatewaysAndAssert() {
        ResponseEntity<ApiResponse> resp = diameterManagerController.getAllDiameterGateways();
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, ((List<?>) Objects.requireNonNull(resp.getBody()).data()).size());
    }

    private void startStopDiameterGatewayAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO gatewayBefore = getDiameterGatewayById(ctx.gatewayId());
        boolean previousStatus = gatewayBefore.isStarted();

        ResponseEntity<ApiResponse> startStopResp = diameterManagerController.startStopDiameterGateway(ctx.gatewayId(), true);
        assertNotNull(startStopResp);
        assertEquals(200, startStopResp.getStatusCode().value());

        DiameterGatewayDTO gatewayAfter = getDiameterGatewayById(ctx.gatewayId());
        assertNotNull(gatewayAfter);
        assertEquals(!previousStatus, gatewayAfter.isStarted());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void startStopPeerAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO gatewayBefore = getDiameterGatewayById(ctx.gatewayId());
        PeerDTO peerBefore = gatewayBefore.getPeers().stream().findFirst().get();
        Integer peerId = peerBefore.getId();
        boolean previousPeerStatus = peerBefore.isStarted();

        ResponseEntity<ApiResponse> startStopResp = diameterManagerController.startStopPeer(peerId, true);
        assertNotNull(startStopResp);
        assertEquals(200, startStopResp.getStatusCode().value());

        DiameterGatewayDTO gatewayAfter = getDiameterGatewayById(ctx.gatewayId());
        assertNotNull(gatewayAfter);
        assertEquals(previousPeerStatus, gatewayAfter.getPeers().stream().findFirst().get().isStarted());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void addUpdateAndRemoveRealmAndAssert(DiameterGatewayContext ctx) {
        RealmDTO realmForAdd = ctx.originalDto().getRealms().stream().findFirst().get();
        realmForAdd.setId(null);
        realmForAdd.setName("NEW-REALM");
        realmForAdd.setUri("NEW-URI.realm");
        realmForAdd.setPeers("NEW-PEER-1,NEW-PEER-2");

        ResponseEntity<ApiResponse> addResp = diameterManagerController.addDiameterRealm(ctx.gatewayId(), realmForAdd);
        assertNotNull(addResp);
        assertEquals(200, addResp.getStatusCode().value());

        DiameterGatewayDTO gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(2, gw.getRealms().size());
        assertEquals(1, gw.getRealms().stream().filter(r -> r.getName().equals("NEW-REALM")).count());

        RealmDTO realmForUpdate = gw.getRealms().stream().findFirst().get();
        realmForUpdate.getApplication().setName("UPDATED-APP");
        realmForUpdate.setName("UPDATED-REALM");

        ResponseEntity<ApiResponse> updateResp = diameterManagerController.updateDiameterRealm(realmForUpdate.getId(), realmForUpdate);
        assertNotNull(updateResp);
        assertEquals(200, updateResp.getStatusCode().value());

        gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(2, gw.getRealms().size());
        assertEquals(1, gw.getRealms().stream().filter(r -> r.getName().equals("UPDATED-REALM")).count());
        assertEquals(1, gw.getRealms().stream().filter(r -> r.getApplication().getName().equals("UPDATED-APP")).count());

        Integer maxId = gw.getRealms().stream().map(RealmDTO::getId).max(Integer::compareTo).orElse(null);
        ResponseEntity<ApiResponse> removeResp = diameterManagerController.removeDiameterRealm(maxId);
        assertNotNull(removeResp);
        assertEquals(200, removeResp.getStatusCode().value());

        gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(1, gw.getRealms().size());
        assertEquals(0, gw.getRealms().stream().filter(r -> r.getId().equals(maxId)).count());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void addUpdateAndRemovePeerAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(1, gw.getPeers().size());

        PeerDTO peerForAdd = ctx.originalDto().getPeers().stream().findFirst().get();
        peerForAdd.setId(null);
        peerForAdd.setDiameterGatewayId(ctx.gatewayId());
        peerForAdd.setUri("NEW-URI.peer");
        peerForAdd.setHost("NEW-HOST");
        peerForAdd.setRating(5);
        peerForAdd.setIp("NEW-IP");
        peerForAdd.setApplications("NEW-APP-1,NEW-APP-2");
        peerForAdd.setPortRange("2000-3000");
        peerForAdd.setSecurityRef("NEW-SECURITY-REF");
        peerForAdd.setStandbyAddresses("NEW-STANDBY-1,NEW-STANDBY-2");

        ResponseEntity<ApiResponse> addResp = diameterManagerController.addDiameterPeer(ctx.gatewayId(), peerForAdd);
        assertNotNull(addResp);
        assertEquals(200, addResp.getStatusCode().value());

        gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(2, gw.getPeers().size());
        assertEquals(1, gw.getPeers().stream().filter(p -> p.getUri().equals("NEW-URI.peer")).count());
        assertEquals(1, gw.getPeers().stream().filter(p -> p.getRating() == 5).count());
        assertEquals(1, gw.getPeers().stream().filter(p -> p.getApplications().equals("NEW-APP-1,NEW-APP-2")).count());

        PeerDTO peerForUpdate = gw.getPeers().stream().findFirst().get();
        Integer peerIdForUpdate = peerForUpdate.getId();
        peerForUpdate.setRating(6);
        peerForUpdate.setApplications("UPDATED-APP-1,UPDATED-APP-2");

        ResponseEntity<ApiResponse> updateResp = diameterManagerController.updateDiameterPeer(peerIdForUpdate, peerForUpdate);
        assertNotNull(updateResp);
        assertEquals(200, updateResp.getStatusCode().value());

        gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(2, gw.getPeers().size());
        PeerDTO updatedPeer = gw.getPeers().stream().filter(p -> p.getId().equals(peerIdForUpdate)).findFirst().get();
        assertEquals(6, updatedPeer.getRating());
        assertEquals("UPDATED-APP-1,UPDATED-APP-2", updatedPeer.getApplications());

        Integer maxPeerId = gw.getPeers().stream().map(PeerDTO::getId).max(Integer::compareTo).orElse(0);
        ResponseEntity<ApiResponse> removeResp = diameterManagerController.removeDiameterPeer(maxPeerId);
        assertNotNull(removeResp);
        assertEquals(200, removeResp.getStatusCode().value());

        gw = getDiameterGatewayById(ctx.gatewayId());
        assertEquals(1, gw.getPeers().size());
        assertEquals(0, gw.getPeers().stream().filter(p -> p.getId().equals(maxPeerId)).count());
    }

    private void updateParametersAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO gw = getDiameterGatewayById(ctx.gatewayId());
        ParametersDTO parametersDTO = gw.getParameters();
        parametersDTO.setDuplicateTimer(200);
        parametersDTO.setDuplicateSize(200);
        parametersDTO.setQueueSize(200);
        parametersDTO.setMessageTimeOut(200);
        parametersDTO.setStopTimeOut(200);
        parametersDTO.setCeaTimeOut(200);
        parametersDTO.setIacTimeOut(200);
        parametersDTO.setDwaTimeOut(200);
        parametersDTO.setDpaTimeOut(200);
        parametersDTO.setRecTimeOut(200);
        parametersDTO.setPeerFsmThreadCount(200);
        parametersDTO.setAcceptUndefinedPeer(false);

        ResponseEntity<ApiResponse> updateResp = diameterManagerController.updateParameters(ctx.gatewayId(), parametersDTO);
        assertNotNull(updateResp);
        assertEquals(200, updateResp.getStatusCode().value());

        DiameterGatewayDTO updated = getDiameterGatewayById(ctx.gatewayId());
        assertNotNull(updated);
        assertEquals(200, updated.getParameters().getDuplicateTimer());
        assertEquals(200, updated.getParameters().getDuplicateSize());
        assertEquals(200, updated.getParameters().getQueueSize());
        assertEquals(200, updated.getParameters().getStopTimeOut());
        assertEquals(200, updated.getParameters().getCeaTimeOut());
        assertEquals(200, updated.getParameters().getIacTimeOut());
        assertEquals(200, updated.getParameters().getDwaTimeOut());
        assertEquals(200, updated.getParameters().getDpaTimeOut());
        assertEquals(200, updated.getParameters().getRecTimeOut());
        assertEquals(200, updated.getParameters().getPeerFsmThreadCount());
        assertFalse(updated.getParameters().isAcceptUndefinedPeer());
    }

    private void updateLocalPeerAndAssert(DiameterGatewayContext ctx) {
        DiameterGatewayDTO gw = getDiameterGatewayById(ctx.gatewayId());
        LocalPeerDTO localPeerDTO = gw.getLocalPeer();
        localPeerDTO.setUri("UPDATED-URI.local-peer");
        localPeerDTO.setIpAddresses("UPDATED-IP-1,UPDATED-IP-2");
        localPeerDTO.setRealm("UPDATED-REALM");
        localPeerDTO.setVendorId(10417);

        ResponseEntity<ApiResponse> updateResp = diameterManagerController.updateLocalPeer(ctx.gatewayId(), localPeerDTO);
        assertNotNull(updateResp);
        assertEquals(200, updateResp.getStatusCode().value());

        DiameterGatewayDTO updated = getDiameterGatewayById(ctx.gatewayId());
        assertNotNull(updated);
        assertEquals("UPDATED-URI.local-peer", updated.getLocalPeer().getUri());
        assertEquals("UPDATED-IP-1,UPDATED-IP-2", updated.getLocalPeer().getIpAddresses());
        assertEquals("UPDATED-REALM", updated.getLocalPeer().getRealm());
        assertEquals(10417, updated.getLocalPeer().getVendorId());
    }

    private void addSecondOcsGatewayAndAssertFails() {
        DiameterGatewayDTO ocsGateway1 = dtoBuilder("OCS-Gateway-1", "OCS", null);
        DiameterGatewayDTO ocsGateway2 = dtoBuilder("OCS-Gateway-2", "OCS", null);

        ResponseEntity<ApiResponse> firstResp = diameterManagerController.addDiameterGateway(ocsGateway1);
        assertNotNull(firstResp);
        assertEquals(200, firstResp.getStatusCode().value());

        ResponseEntity<ApiResponse> allChargingResp = diameterManagerController.getAllChargingGateway();
        assertNotNull(allChargingResp);

        ResponseEntity<ApiResponse> secondResp = diameterManagerController.addDiameterGateway(ocsGateway2);
        assertNotNull(secondResp);
        assertTrue(secondResp.getStatusCode().is5xxServerError());
    }

    private void removeDiameterGatewayAndAssert(DiameterGatewayContext ctx) {
        ResponseEntity<ApiResponse> removeResp = diameterManagerController.removeDiameterGateway(ctx.gatewayId());
        assertNotNull(removeResp);
        assertEquals(200, removeResp.getStatusCode().value());
    }

    private DiameterGatewayDTO getDiameterGatewayById(Integer id) {
        return (DiameterGatewayDTO) diameterManagerService.getDiameterGatewayById(id).data();
    }

    private DiameterGatewayDTO dtoBuilder(String name, String type, Integer mnoId) {
        ParametersDTO parametersDTO = new ParametersDTO();
        parametersDTO.setDuplicateTimer(100);
        parametersDTO.setDuplicateSize(100);
        parametersDTO.setQueueSize(100);
        parametersDTO.setMessageTimeOut(100);
        parametersDTO.setStopTimeOut(100);
        parametersDTO.setCeaTimeOut(100);
        parametersDTO.setIacTimeOut(100);
        parametersDTO.setDwaTimeOut(100);
        parametersDTO.setDpaTimeOut(100);
        parametersDTO.setRecTimeOut(100);
        parametersDTO.setPeerFsmThreadCount(100);
        parametersDTO.setAcceptUndefinedPeer(true);
        parametersDTO.setSessionTimeOut(100L);
        parametersDTO.setBindDelay(1000L);
        parametersDTO.setRequestTableSize(100);
        parametersDTO.setRequestTableClearSize(1500);

        RealmDTO realmDTO = new RealmDTO();
        ApplicationDTO realmApplicationDTO = new ApplicationDTO();
        realmApplicationDTO.setName("Realm-App");
        realmApplicationDTO.setVendorId(10415);
        realmApplicationDTO.setAuthApplId(16777251);
        realmApplicationDTO.setAcctApplId(16777251);
        realmDTO.setName("realm-1");
        realmDTO.setUri("uri-1");
        realmDTO.setPeers("peer-1,peer-2");
        realmDTO.setLocalAction("LOCAL");
        realmDTO.setDynamic(true);
        realmDTO.setExpTime(100);
        realmDTO.setApplication(realmApplicationDTO);

        PeerDTO peerDTO = new PeerDTO();
        peerDTO.setUri("peer-uri-1");
        peerDTO.setHost("peer-host-1");
        peerDTO.setRating(3);
        peerDTO.setIp("127.0.0.1");
        peerDTO.setApplications("app-1,app-2");
        peerDTO.setPortRange("1000-2000");
        peerDTO.setSecurityRef("security-ref-1");
        peerDTO.setStandbyAddresses("standby-1,standby-2");

        LocalPeerDTO localPeerDTO = new LocalPeerDTO();
        localPeerDTO.setUri("local-peer-uri-1");
        localPeerDTO.setIpAddresses("ip-1,ip-2");
        localPeerDTO.setRealm("realm-1");
        localPeerDTO.setVendorId(10415);
        localPeerDTO.setProductName("product-1");
        localPeerDTO.setFirmwareVersion(1);

        ApplicationDTO localPeerApplicationDTO = new ApplicationDTO();
        localPeerApplicationDTO.setName("LocalPeer-App");
        localPeerApplicationDTO.setVendorId(10415);
        localPeerApplicationDTO.setAuthApplId(4);
        localPeerApplicationDTO.setAcctApplId(0);
        localPeerDTO.setApplications(Set.of(localPeerApplicationDTO));

        DiameterGatewayDTO dto = new DiameterGatewayDTO();
        dto.setName(name);
        dto.setConnectionType(ConnectionType.SCTP);
        dto.setType(type);

        if ("GATEWAY".equalsIgnoreCase(type)) {
            dto.setMnoId(mnoId);
            dto.setGlobalTitle("505888123456");
            dto.setSplitMessage(true);
        }

        dto.setParameters(parametersDTO);
        dto.setRealms(Set.of(realmDTO));
        dto.setPeers(Set.of(peerDTO));
        dto.setLocalPeer(localPeerDTO);
        dto.setMessagesPerSecondHigh(70);
        dto.setMessagesPerSecondMedium(20);
        dto.setMessagesPerSecondLow(10);
        dto.setMessagesPerSecond();

        return dto;
    }

    private record DiameterGatewayContext(Integer gatewayId, DiameterGatewayDTO originalDto) {
    }
}
