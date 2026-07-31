package com.smsc.management.app.broadcast.controller;

import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.dto.BroadcastStatusRequestDTO;
import com.smsc.management.app.broadcast.dto.BroadcastTestDTO;
import com.smsc.management.app.broadcast.service.BroadcastLogsService;
import com.smsc.management.app.broadcast.service.BroadcastService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/broadcast")
public class BroadcastController {
    private final BroadcastService processor;
    private final BroadcastLogsService broadcastLogsService;
    private final BroadcastService broadcastService;
    private final ControllerSecurity security;

    @GetMapping
    public ResponseEntity<ApiResponse> get() {
        security.checkCommonRoles();
        ApiResponse result = processor.getAll();
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getByBroadcastId(@PathVariable int id) {
        security.checkCommonRoles();
        ApiResponse result = processor.getById(id);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @RequestBody @Valid BroadcastDTO broadcast) {
        security.checkOperatorRoles();
        ApiResponse result = processor.create(broadcast);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<ApiResponse> update(
            @PathVariable int id,
            @RequestBody @Valid BroadcastDTO broadcast,
            @RequestParam("updatedFile") boolean updatedFile) {
        security.checkOperatorRoles();
        ApiResponse result = processor.update(id, broadcast, updatedFile);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse> messageTest(@RequestBody @Valid BroadcastTestDTO broadcastTest) {
        security.checkOperatorRoles();
        ApiResponse result = processor.sendTestMessage(broadcastTest);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/logs/{broadcastId}")
    public ResponseEntity<ApiResponse> startDownloadLogs(@PathVariable int broadcastId) {
        security.checkOperatorRoles();
        ApiResponse result = broadcastLogsService.startDownloadProcess(broadcastId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/logs/{fileId}")
    public ResponseEntity<ApiResponse> monitorDownloadProcess(@PathVariable int fileId) {
        security.checkOperatorRoles();
        ApiResponse result = broadcastLogsService.monitorDownloadProcess(fileId);
        return ResponseEntity.status(result.status()).body(result);
    }
    @GetMapping("/download/logs/{token}")
    public ResponseEntity<InputStreamResource> downloadProcess(@PathVariable String token) {
        return broadcastLogsService.downloadFile(token);
    }

    @GetMapping("/clone/{id}")
    public ResponseEntity<ApiResponse> clone(@PathVariable int id) {
        security.checkOperatorRoles();
        ApiResponse result = broadcastService.cloneBroadcast(id);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/change-status/{id}")
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable int id, @RequestBody @Valid BroadcastStatusRequestDTO broadcastStatus) {
        if ("DELETED".equalsIgnoreCase(broadcastStatus.getBroadcastStatus())) {
            security.checkOperatorRoles();
        } else {
            security.checkApproverRoles();
        }
        ApiResponse response = processor.changeStatus(id, broadcastStatus);
        return ResponseEntity.status(response.status()).body(response);
    }
    @GetMapping("/{id}/failures")
    public ResponseEntity<ApiResponse> getFailures(@PathVariable int id) {
        security.checkCommonRoles();
        ApiResponse result = processor.getFailureReasons(id);
        return ResponseEntity.status(result.status()).body(result);
    }

}
