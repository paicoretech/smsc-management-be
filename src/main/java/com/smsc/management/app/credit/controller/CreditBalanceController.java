/**
 * 
 */
package com.smsc.management.app.credit.controller;

import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import com.smsc.management.app.credit.dto.UsedCreditByInstanceDTO;
import com.smsc.management.app.credit.service.ProcessCreditService;
import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.GlobalRecords;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller managing operations related to credit balance
 */
@RestController
@RequestMapping("/balance-credit")
@RequiredArgsConstructor
public class CreditBalanceController {
	private final ControllerSecurity security;
	private final ProcessCreditService processor;
	
	/**
     * Retrieves the current rating for a given system.
     *
     * @param request of the Service Provider for which the rating is requested.
     * @return ResponseEntity with a ResponseDTO object containing the rating and related information.
     */
	@PostMapping("/rating")
	public ResponseEntity<ApiResponse> currentRating(@RequestBody GlobalRecords.SystemIdInputParameter request) {
		security.checkAdminRoles();
		ApiResponse result = processor.getRating(request.networkId());
		return ResponseEntity.status(result.status()).body(result);
	}
	
	/**
	 * Endpoint to process the sale of credit for a specific service provider.
	 *
	 * @param creditSales The DTO containing information about the credit sales. Must be validated.
	 * @param networkId   The ID of the network for which the credit sale is being processed.
	 * @return A ResponseEntity containing the response status and body based on the processing result.
	 */
	@PostMapping("/sell/credit/{networkId}")
	public ResponseEntity<ApiResponse> sellCredit(@RequestBody @Valid CreditSalesHistoryDTO creditSales, @PathVariable int networkId) {
		security.checkAdminRoles();
		ApiResponse result = processor.updateCreditBalance(creditSales, networkId);
		return ResponseEntity.status(result.status()).body(result);
	}

	/**
	 * Endpoint to process the sale of credit for a specific service provider by using the external id field.
	 *
	 * @param creditSales The DTO containing information about the credit sales. Must be validated.
	 * @param externalId  The external ID for which the credit sale is being processed.
	 * @return A ResponseEntity containing the response status and body based on the processing result.
	 */
	@PostMapping("/sell/credit-external-id/{externalId}")
	public ResponseEntity<ApiResponse> sellCreditByExternalId(@RequestBody @Valid CreditSalesHistoryDTO creditSales, @PathVariable String externalId) {
		security.checkAdminRoles();
		ApiResponse result = processor.updateCreditBalance(creditSales, externalId);
		return ResponseEntity.status(result.status()).body(result);
	}

	@PostMapping("/credit-used/{applicationName}")
	public ResponseEntity<ApiResponse> usedCreditFromInstance(@RequestBody @Valid List<UsedCreditByInstanceDTO> usedCredits, @PathVariable String applicationName) {
		ApiResponse result = processor.updateMassiveUsedCredits(usedCredits, applicationName);
		return ResponseEntity.status(result.status()).body(result);
	}
}
