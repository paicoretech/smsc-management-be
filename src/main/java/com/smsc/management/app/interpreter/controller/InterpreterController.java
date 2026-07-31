package com.smsc.management.app.interpreter.controller;

import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.interpreter.service.InterpreterService;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/interpreter")
public class InterpreterController {
    private final InterpreterService interpreter;

    @GetMapping
    public ResponseEntity<ApiResponse> get() {
        ApiResponse result = interpreter.getInterpreterSettings();
        return ResponseEntity.status(result.status()).body(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable int id, @RequestBody @Valid InterpreterDTO updatedInterpreter) {
        ApiResponse result = interpreter.updateInterpreter(id, updatedInterpreter);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/gateways")
    public ResponseEntity<ApiResponse> getHttpGateways() {
        ApiResponse result = interpreter.getHttpGateways();
        return ResponseEntity.status(result.status()).body(result);
    }
}
