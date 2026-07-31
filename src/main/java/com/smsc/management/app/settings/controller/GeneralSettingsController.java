package com.smsc.management.app.settings.controller;

import com.smsc.management.app.settings.service.GeneralSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/general-settings")
public class GeneralSettingsController {
	private final GeneralSettingsService generalSettingsService;

	@GetMapping
	public ResponseEntity<ApiResponse> listGeneralSettings(){
		ApiResponse result = generalSettingsService.getGeneralSettingsSmppHttp();
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PutMapping("/update")
	public ResponseEntity<ApiResponse> update(@RequestBody @Valid GeneralSettingsSmppHttpDTO generalSettings){
		ApiResponse result = generalSettingsService.updateGeneralSettings(generalSettings);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@GetMapping("/smsc-retry")
	public ResponseEntity<ApiResponse> smscRetry(){
		ApiResponse result = generalSettingsService.getGeneralSmscRetry();
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PutMapping("/smsc-retry/update")
	public ResponseEntity<ApiResponse> updateSmscRetry(@RequestBody @Valid GeneralSmscRetryDTO generalSmscRetry){
		ApiResponse result = generalSettingsService.updateGeneralSmscRetry(generalSmscRetry);
		return ResponseEntity.status(result.status()).body(result);
	}
}
