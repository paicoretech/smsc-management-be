package com.smsc.management.app.server.controller;

import com.smsc.management.app.server.dto.SmppServerDTO;
import com.smsc.management.app.server.service.SmppServerService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/smpp-server")
public class SmppServerController {
    private final SmppServerService smppServerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getSmppServers() {
        ApiResponse result = smppServerService.getSmppServers();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> newSmppServer(@RequestBody @Valid SmppServerDTO smppServer) {
        ApiResponse result = smppServerService.createSmppServer(smppServer);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateSmppServer(@PathVariable int id, @RequestBody @Valid SmppServerDTO smppServer) {
        ApiResponse result = smppServerService.updateSmppServer(id, smppServer);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteSmppServer(@PathVariable int id) {
        ApiResponse result = smppServerService.deleteSmppServer(id);
        return ResponseEntity.status(result.status()).body(result);
    }
}
