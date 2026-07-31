package com.smsc.management.app.settings.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smsc.management.app.settings.dto.CommonVariablesDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.settings.service.CommonVariableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/smsc-settings")
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequiredArgsConstructor
public class CommonVariableController {
	private final CommonVariableService commonVariableService;
	
	@GetMapping("/variables")
	public ResponseEntity<ApiResponse> get() {
		ApiResponse result = commonVariableService.get();
		return ResponseEntity.status(result.status()).body(result);
	}

	@PutMapping("/massiveUpdate")
	public ResponseEntity<ApiResponse> massiveUpdate(@RequestBody @Valid List<CommonVariablesDTO> variable) {
		ApiResponse result = commonVariableService.massiveUpdate(variable);
		return ResponseEntity.status(result.status()).body(result);
	}

}
