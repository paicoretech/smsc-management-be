/**
 * 
 */
package com.smsc.management.app.credit.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.utils.AppProperties;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import com.smsc.management.app.credit.dto.RatingManagementDTO;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.credit.dto.UsedCreditByInstanceDTO;
import com.smsc.management.app.credit.model.entity.CreditSalesHistory;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.credit.mapper.CreditSalesMapper;
import com.smsc.management.app.credit.model.repository.CreditSalesHistoryRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for processing credit balance management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCreditService {

	private final CreditSalesHistoryRepository creditSalesRepo;
	private final ServiceProviderRepository serviceProviderRepo;
	private final UtilsBase utilsBase;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CreditSalesMapper creditSalesMapper;
	private final HandlerCreditByServiceProvider handlerCreditSp;
	private final AppProperties appProperties;
	private static final String GENERIC_EXCEPTION_MESSAGE = "Request was end with error";

	/**
     * Retrieves classification information for a service provider with a given system ID.
     *
     * @param networkId of the service provider for which the rating is requested.
     * @return ResponseDTO containing the rating details or an exception message.
     */
	public ApiResponse getRating(String networkId) {
		try {
			RatingManagementDTO ratingDTO = new RatingManagementDTO();

			String serviceProviderStr = utilsBase.getInRedis(GeneralSmscConstants.SERVICE_PROVIDERS_HASH_NAME, networkId);
			if (serviceProviderStr == null) {
				return ResponseMapping.errorMessageNoFound("NetworkId " + networkId + " was not found in Redis");
			}
			var serviceProviderDTO = objectMapper.readValue(serviceProviderStr, RedisServiceProviderDTO.class);
			ratingDTO.setHasAvailableCredit(serviceProviderDTO.getHasAvailableCredit());
			ratingDTO.setNetworkId(serviceProviderDTO.getNetworkId());

			// Get rating
			String spBalanceCreditStr = utilsBase.getInRedis(GeneralSmscConstants.BALANCE_HANDLER_HASH_NAME, networkId);
			if (spBalanceCreditStr != null && !spBalanceCreditStr.isBlank()) {
				JsonNode spBalanceCreditJson = objectMapper.readTree(spBalanceCreditStr);
				ratingDTO.setTps(spBalanceCreditJson.get("tps").asInt(0));
			}
			ratingDTO.setAvailableCredit(handlerCreditSp.getCurrentBalance(serviceProviderDTO.getNetworkId()));

			// Get sales history
			List<CreditSalesHistoryDTO> creditSalesHistory = creditSalesRepo.fetchByNetworkId(serviceProviderDTO.getNetworkId());
			ratingDTO.setCreditSalesHistory(creditSalesHistory);

			return ResponseMapping.successMessage("Get rating request success", ratingDTO);
		} catch (Exception e) {
			log.error("getRating with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage(GENERIC_EXCEPTION_MESSAGE, e);
		}
	}

	/**
	 * Updates the credit balance for a given credit sales history and network ID.
	 *
	 * @param creditSales The CreditSalesHistoryDTO object containing credit sales information.
	 * @param id          The network ID to update the credit balance.
	 * @return A ResponseDTO object indicating the status of the credit balance update operation.
	 */
	public ApiResponse updateCreditBalance(CreditSalesHistoryDTO creditSales, Object id) {
		try {
			boolean searchByExternalId = id instanceof String;
			ServiceProvider serviceProvider = searchByExternalId ? serviceProviderRepo.findByExternalIdAndEnabledNot((String) id, DELETED_ENABLED_STATUS) :
					serviceProviderRepo.findByNetworkIdAndEnabledNot((int) id, DELETED_ENABLED_STATUS);
			if (serviceProvider == null) {
				return ResponseMapping.errorMessageNoFound("Service provider was not found");
			}

			CreditSalesHistory creditSalesEntity = new CreditSalesHistory();
			creditSalesEntity.setNetworkId(serviceProvider.getNetworkId());
			creditSalesEntity.setCredit(creditSales.getCredit());
			creditSalesEntity.setDescription(creditSales.getDescription());
			var resultEntity = creditSalesRepo.save(creditSalesEntity);

			// updated cache balance
			if (!handlerCreditSp.addCreditPackage(serviceProvider.getNetworkId(), creditSales.getCredit())) {
				log.error("Error syncing new balance to cache for network id {}", serviceProvider.getNetworkId());
			}
			log.info("Balance amount to networkId id {} was updated successfully", serviceProvider.getNetworkId());
			return ResponseMapping.successMessage("Credit add successful.", creditSalesMapper.toDTO(resultEntity));
		} catch (Exception e) {
			log.error("updateCreditBalance with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage(GENERIC_EXCEPTION_MESSAGE, e);
		}
	}
	
	/**
	 * Updates the used credits for multiple instances in bulk.
	 *
	 * @param usedCredits      The list of UsedCreditByInstanceDTO objects containing information about used credits.
	 * @param applicationName  The name of the application instance.
	 * @return                 A ResponseDTO object indicating the status of the operation.
	 */
	public ApiResponse updateMassiveUsedCredits(List<UsedCreditByInstanceDTO> usedCredits, String applicationName) {
		try {
			if (usedCredits.isEmpty()) {
				log.error("Empty used credit list to {} instance", applicationName);
				return ResponseMapping.errorMessage("empty used credit list");
			}
			for (UsedCreditByInstanceDTO credit : usedCredits) {
				handlerCreditSp.decreaseCreditUsed(credit.getNetworkId(), (long) credit.getCreditUsed());
			}
			log.debug("Mass update of credits used for the {} instance processed successfully -> {}", applicationName, usedCredits);
			return ResponseMapping.successMessage("Used credits successfully processed.", null);
		} catch (Exception e) {
			log.error("Update massive used credits to {} instance with error: {}", applicationName, e.getMessage());
			return ResponseMapping.exceptionMessage(GENERIC_EXCEPTION_MESSAGE, e);
		}
	}
}
