package com.smsc.management.app.gateway.controller;

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

import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.gateway.service.GatewaysService;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/gateways")
public class GatewaysController {
	private final GatewaysService processor;

	@GetMapping
	public ResponseEntity<ApiResponse> listGateway(){
		ApiResponse result = processor.getGateways();
		return ResponseEntity.status(result.status()).body(result);
	}

	@GetMapping("/by-external-id/{externalId}")
	public ResponseEntity<ApiResponse> getGatewayByExternalId(@PathVariable String externalId){
		ApiResponse result = processor.getGatewayByExternalId(externalId);
		return ResponseEntity.status(result.status()).body(result);
	}

	@PostMapping("/create")
		public ResponseEntity<ApiResponse> create(@RequestBody @Valid GatewaysDTO gateway){
		ApiResponse result = processor.create(gateway);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<ApiResponse> update(@RequestBody @Valid GatewaysDTO gateway, @PathVariable int id){
		ApiResponse result = processor.update(id, gateway);
		return ResponseEntity.status(result.status()).body(result);
	}

	@PutMapping("/update-by-external-id/{externalId}")
	public ResponseEntity<ApiResponse> updateByExternalId(@RequestBody @Valid GatewaysDTO gateway, @PathVariable String externalId){
		ApiResponse result = processor.update(externalId, gateway);
		return ResponseEntity.status(result.status()).body(result);
	}
}
