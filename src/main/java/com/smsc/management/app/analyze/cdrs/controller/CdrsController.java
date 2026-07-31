package com.smsc.management.app.analyze.cdrs.controller;

import com.smsc.management.app.analyze.cdrs.service.CdrsService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analyze")
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR', 'TECH_SUPPORT')")
public class CdrsController {
    private final CdrsService cdrsService;

    @PostMapping("/cdrs")
    public ResponseEntity<ApiResponse> data(@RequestBody @Valid Map<String, Object> filters) {
        ApiResponse result = cdrsService.getDataLogs(filters);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/catalog/broadcast")
    public ResponseEntity<ApiResponse> broadcastCatalog() {
        ApiResponse result = cdrsService.getBroadcastCatalog();
        return ResponseEntity.status(result.status()).body(result);
    }
}
