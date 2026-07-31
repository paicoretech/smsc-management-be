package com.smsc.management.app.analyze.dashboards.controller;

import com.smsc.management.app.analyze.dashboards.service.DashboardsService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analyze")
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR', 'TECH_SUPPORT')")
public class DashboardsController {
    private final DashboardsService dashboardsService;

    @PostMapping("/dashboards")
    public ResponseEntity<ApiResponse> data(@RequestBody @Valid Map<String, Object> filters) {
        ApiResponse result = dashboardsService.getDashboards(filters);
        return ResponseEntity.status(result.status()).body(result);
    }
}
