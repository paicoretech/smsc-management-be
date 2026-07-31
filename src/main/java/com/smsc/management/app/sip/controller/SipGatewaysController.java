package com.smsc.management.app.sip.controller;

import com.smsc.management.app.sip.dto.SipGatewaysDTO;
import com.smsc.management.app.sip.service.SipGatewaysService;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/sip-gateways")
public class SipGatewaysController {

    private final SipGatewaysService sipGatewaysService;

    private final Ss7GatewaysRepository ss7GatewaysRepository;
    private final Ss7GatewaysMapper ss7GatewaysMapper;

    @GetMapping
    public ResponseEntity<ApiResponse> listSip() {
        ApiResponse result = sipGatewaysService.getSipGateways();
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/{networkId}")
    public ResponseEntity<ApiResponse> getSipByNetworkId(@PathVariable int networkId) {
        ApiResponse result = sipGatewaysService.getSipGatewaysByNetworkId(networkId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid SipGatewaysDTO dto) {
        ApiResponse result = sipGatewaysService.create(dto);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{networkId}")
    public ResponseEntity<ApiResponse> update(@RequestBody @Valid SipGatewaysDTO dto, @PathVariable int networkId) {
        ApiResponse result = sipGatewaysService.update(networkId, dto);
        return ResponseEntity.status(result.status()).body(result);
    }

    /**
     * FE helper: list SS7 gateways allowed for USSI traffic (allowed_ussi=true)
     */
    @GetMapping("/ss7-gateways/allowed-ussi")
    public ResponseEntity<ApiResponse> listSs7AllowedUssi() {
        List<Ss7Gateways> result = ss7GatewaysRepository
                .findByEnabledNotAndAllowedUssiTrue(Constants.DELETED_ENABLED_STATUS);

        List<Ss7GatewaysDTO> data = ss7GatewaysMapper.toDTOList(result);

        ApiResponse response = com.smsc.management.utils.ResponseMapping
                .successMessage("SS7 gateways allowed for USSI traffic.", data);

        return ResponseEntity.status(response.status()).body(response);
    }
}
