package com.smsc.management.app.diameter.controller;

import com.smsc.management.app.diameter.dto.DiameterGatewayDTO;
import com.smsc.management.app.diameter.dto.LocalPeerDTO;
import com.smsc.management.app.diameter.dto.ParametersDTO;
import com.smsc.management.app.diameter.dto.PeerDTO;
import com.smsc.management.app.diameter.dto.RealmDTO;
import com.smsc.management.app.diameter.service.DiameterManagerService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/diameter")
public class DiameterManagerController {
    private final DiameterManagerService diameterManagerService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addDiameterGateway(
            @RequestBody @Valid DiameterGatewayDTO diameterGateway) {
        ApiResponse result = diameterManagerService.createDiameterGateway(diameterGateway);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateDiameterGateway(
            @RequestBody @Valid DiameterGatewayDTO diameterGateway, @PathVariable Integer id) {
        ApiResponse result = diameterManagerService.updateDiameterGatewayById(id, diameterGateway);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<ApiResponse> removeDiameterGateway(@PathVariable Integer id) {
        ApiResponse result = diameterManagerService.removeDiameterGatewayById(id);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse> getDiameterGateway(@PathVariable Integer id) {
        ApiResponse result = diameterManagerService.getDiameterGatewayById(id);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/get/charging")
    public ResponseEntity<ApiResponse> getAllChargingGateway() {
        ApiResponse result = diameterManagerService.getChargingGateway();
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse> getAllDiameterGateways() {
        ApiResponse result = diameterManagerService.getAllDiameterGateways();
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/getAll/ip-sm-gw")
    public ResponseEntity<ApiResponse> getAllDiameterGatewaysIpSmGw() {
        ApiResponse result = diameterManagerService. getAllDiameterGatewaysIpSmGw();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}/start/{start}")
    public ResponseEntity<ApiResponse> startStopDiameterGateway(@PathVariable Integer id, @PathVariable boolean start) {
        ApiResponse result = diameterManagerService.startStopDiameterGateway(id, start);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/peer/{peerId}/start/{start}")
    public ResponseEntity<ApiResponse> startStopPeer(@PathVariable Integer peerId, @PathVariable boolean start) {
        ApiResponse result = diameterManagerService.startStopPeer(peerId, start);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/{diameterGatewayId}/realm/add")
    public ResponseEntity<ApiResponse> addDiameterRealm(
            @PathVariable Integer diameterGatewayId,
            @RequestBody @Valid RealmDTO diameterRealm) {
        ApiResponse result = diameterManagerService
                .addDiameterRealmToDiameterGateway(diameterGatewayId, diameterRealm);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/realms/{realmId}/update")
    public ResponseEntity<ApiResponse> updateDiameterRealm(
            @PathVariable Integer realmId,
            @RequestBody @Valid RealmDTO diameterRealm) {
        ApiResponse result = diameterManagerService
                .updateDiameterRealmToDiameterGateway(realmId, diameterRealm);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/realms/{realmId}/remove")
    public ResponseEntity<ApiResponse> removeDiameterRealm(@PathVariable Integer realmId) {
        ApiResponse result = diameterManagerService.removeDiameterRealmFromDiameterGateway(realmId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/{diameterGatewayId}/peer/add")
    public ResponseEntity<ApiResponse> addDiameterPeer(
            @PathVariable Integer diameterGatewayId,
            @RequestBody @Valid PeerDTO diameterPeer) {
        ApiResponse result = diameterManagerService
                .addDiameterPeerToDiameterGateway(diameterGatewayId, diameterPeer);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/peers/{peerId}/update")
    public ResponseEntity<ApiResponse> updateDiameterPeer(
            @PathVariable Integer peerId,
            @RequestBody @Valid PeerDTO diameterPeer) {
        ApiResponse result = diameterManagerService
                .updateDiameterPeerToDiameterGateway(peerId, diameterPeer);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/peers/{peerId}/remove")
    public ResponseEntity<ApiResponse> removeDiameterPeer(@PathVariable Integer peerId) {
        ApiResponse result = diameterManagerService.removeDiameterPeerFromDiameterGateway(peerId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/{diameterGatewayId}/parameters/update")
    public ResponseEntity<ApiResponse> updateParameters(
            @PathVariable Integer diameterGatewayId,
            @RequestBody @Valid ParametersDTO parametersDTO) {
        ApiResponse result = diameterManagerService.updateParametersForDiameterGateway(diameterGatewayId, parametersDTO);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/{diameterGatewayId}/localPeer/update")
    public ResponseEntity<ApiResponse> updateLocalPeer(
            @PathVariable Integer diameterGatewayId,
            @RequestBody @Valid LocalPeerDTO localPeerDTO) {
        ApiResponse result = diameterManagerService.updateLocalPeerForDiameterGateway(diameterGatewayId, localPeerDTO);
        return ResponseEntity.status(result.status()).body(result);
    }
}
