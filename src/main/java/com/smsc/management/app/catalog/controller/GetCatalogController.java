package com.smsc.management.app.catalog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.catalog.service.CatalogService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class GetCatalogController {
    private final CatalogService catalogService;

    @GetMapping("/{catalogType}")
    public ResponseEntity<ApiResponse> bindStatuses(@PathVariable String catalogType) {
        ApiResponse result = catalogService.getCatalog(catalogType);
        return ResponseEntity.status(result.status()).body(result);
    }
}
