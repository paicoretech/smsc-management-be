package com.smsc.management.app.user.controller;

import com.smsc.management.app.user.dto.AuthRequestDTO;
import com.smsc.management.app.user.service.AuthService;
import com.smsc.management.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse> authenticate(@RequestBody AuthRequestDTO request) {
        ApiResponse response = service.authenticate(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PutMapping("/reset-passwd/{isFirstLogin}")
    public ResponseEntity<ApiResponse> resetPasswd(@PathVariable("isFirstLogin") boolean isFirstLogin, @RequestBody AuthRequestDTO request) {
        ApiResponse response = service.resetPassword(request, isFirstLogin);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        ApiResponse response = service.logOut();
        return ResponseEntity.status(response.status()).body(response);
    }
}
