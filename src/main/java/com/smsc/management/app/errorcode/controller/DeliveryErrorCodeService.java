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

import com.smsc.management.app.errorcode.dto.DeliveryErrorCodeDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.errorcode.service.ProcessDeliveryErrorCode;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROOT', 'ADMINISTRATOR')")
@RequestMapping("/delivery-error-code")
public class DeliveryErrorCodeService {
	private final ProcessDeliveryErrorCode processor;
	
	@GetMapping
	public ResponseEntity<ApiResponse> listDeliveryErrorCode(){
		ApiResponse result = processor.getDeliveryErrorCode();
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PostMapping("/create")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid DeliveryErrorCodeDTO errorCode){
		ApiResponse result = processor.create(errorCode);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<ApiResponse> update(@RequestBody @Valid DeliveryErrorCodeDTO errorCode, @PathVariable int id){
		ApiResponse result = processor.update(id, errorCode);
		return ResponseEntity.status(result.status()).body(result);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ApiResponse> delete(@PathVariable int id){
		ApiResponse result = processor.delete(id);
		return ResponseEntity.status(result.status()).body(result);
	}
}
