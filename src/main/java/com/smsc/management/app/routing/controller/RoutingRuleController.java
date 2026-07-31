package com.smsc.management.app.routing.controller;

import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.service.RoutingRulesService;
import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/routing-rules")
public class RoutingRuleController {
    private final ControllerSecurity security;
    private final RoutingRulesService processor;

    @GetMapping
    public ResponseEntity<ApiResponse> getListRouting() {
        security.checkAdminRoles();
        ApiResponse result = processor.getRoutingRules();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid RoutingRulesDTO routingRules) {
        security.checkAdminRoles();
        ApiResponse result = processor.create(routingRules);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@RequestBody @Valid RoutingRulesDTO routingRules, @PathVariable int id) {
        security.checkAdminRoles();
        ApiResponse result = processor.update(id, routingRules);
        return ResponseEntity.status(result.status()).body(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable int id) {
        security.checkAdminRoles();
        ApiResponse result = processor.delete(id);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/networks")
    public ResponseEntity<ApiResponse> getNetworks() {
        security.checkCommonRoles();
        ApiResponse result = processor.getNetworks();
        return ResponseEntity.status(result.status()).body(result);
    }

    /*
    This endpoint was created to notify changes from the delivery rate routing script.
     */
    @PostMapping("/notify-change/{originNetworkId}")
    public ResponseEntity<ApiResponse> updateRedis(@PathVariable int originNetworkId) {
        ApiResponse response = processor.updateRedisAndSocketNotification(originNetworkId);
        return ResponseEntity.status(response.status()).body(response);
    }
}
