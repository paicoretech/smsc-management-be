package com.smsc.management.app.settings.controller;

import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.settings.service.StatusHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class HandlerStatusController {
    private final ControllerSecurity security;
    private final Flux<String> notifyRealtime;
    private final StatusHandlerService statusHandlerService;

    @GetMapping(value = "/handler-status-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> handleStatus() {
        return this.notifyRealtime;
    }

    @PostMapping("/http-server-status")
    public ResponseEntity<ApiResponse> handlerServerHttp(@RequestParam(name = "application_name") String applicationName, @RequestParam(name = "new_status") String newStatus) {
        security.checkAdminRoles();
    	ApiResponse result = statusHandlerService.updateStatusServerHttp(applicationName, newStatus.toUpperCase());
        return ResponseEntity.status(result.status()).body(result);
    }
    
    @PostMapping("/http-server-status/all")
    public ResponseEntity<ApiResponse> handlerStatusAllServerHttp(@RequestParam(name = "new_status") String newStatus) {
        security.checkAdminRoles();
    	ApiResponse result = statusHandlerService.updateStatusAllServerHttp(newStatus.toUpperCase());
        return ResponseEntity.status(result.status()).body(result);
    }
    
    @GetMapping("/http-server-config")
    public ResponseEntity<ApiResponse> getHttpServerConfig(){
        security.checkAdminRoles();
    	ApiResponse result = statusHandlerService.getAllHttpServers();
		return ResponseEntity.status(result.status()).body(result);
    }
}
