package com.smsc.management.app.errorcode.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smsc.management.app.errorcode.dto.ErrorCodeDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.errorcode.service.ErrorCodeService;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/error-code")
public class ErrorCodeController {
	private final ErrorCodeService processor;

	@GetMapping
	public ResponseEntity<ApiResponse> listErrorCode(){
		ApiResponse result = processor.getErrorCodeList();
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@GetMapping("/by-mno-id/{mnoId}")
	public ResponseEntity<ApiResponse> listErrorCodeByMNO(@PathVariable int mnoId){
		ApiResponse result = processor.getErrorCodeListByMNO(mnoId);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PostMapping("/create")
	public ResponseEntity<ApiResponse> createErrorCode(@RequestBody @Valid ErrorCodeDTO provider){
		ApiResponse result = processor.create(provider);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ApiResponse> deleteErrorCode(@PathVariable int id){
		ApiResponse result = processor.delete(id);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<ApiResponse> updateErrorCode(@RequestBody @Valid ErrorCodeDTO provider, @PathVariable int id){
		ApiResponse result = processor.update(id, provider);
		return ResponseEntity.status(result.status()).body(result);
	}
}
