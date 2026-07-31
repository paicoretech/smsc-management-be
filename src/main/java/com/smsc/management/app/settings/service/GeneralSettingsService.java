package com.smsc.management.app.settings.service;

import static com.smsc.management.utils.Constants.GENERAL_SETTINGS_SMPP_HTTP_ENDPOINT;
import static com.smsc.management.utils.Constants.GENERAL_SMSC_RETRY_ENDPOINT;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.utils.AppProperties;
import org.springframework.stereotype.Service;
import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.settings.model.entity.GeneralSettingsSmppHttp;
import com.smsc.management.app.settings.model.entity.GeneralSmscRetry;
import com.smsc.management.app.settings.mapper.GeneralSettingsMapper;
import com.smsc.management.app.settings.model.repository.GeneralSettingsSmppHttpRepository;
import com.smsc.management.app.settings.model.repository.GeneralSmscRetryRepository;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralSettingsService {
	private final GeneralSettingsSmppHttpRepository generalSettingsHttpSmppRepo;
	private final GeneralSmscRetryRepository generalSmscRetryRepo;
	private final GeneralSettingsMapper generalSettingsMapper;
	private final AppProperties appProperties;
	private final UtilsBase utilsBase;

	/**
	 * Retrieves the general settings for SMPP/HTTP communication.
	 *
	 * @return A {@link ApiResponse} object containing either the retrieved settings
	 *         as a DTO or an error message if not found.
	 */
	public ApiResponse getGeneralSettingsSmppHttp() {
		try {
			GeneralSettingsSmppHttp generalSetting = generalSettingsHttpSmppRepo.findById(1);
			if (generalSetting == null) {
				return ResponseMapping.errorMessageNoFound("General settings to smpp/http was not found");
			}
			
			return ResponseMapping.successMessage("Get general settings smpp-http success", generalSettingsMapper.toDTO(generalSetting));
		} catch (Exception e) {
			log.error("Error to get general settings smpp-http -> {}", e.getMessage(), e);
			return ResponseMapping.exceptionMessage("Error to get general settings smpp-http", e);
		}
	}
	
	/**
	 * Updates the general settings for SMPP/HTTP communication.
	 * Updates the existing settings in the database and Redis cache.
	 * Sends a notification via socket upon successful update.
	 *
	 * @param generalSettingsDTO The DTO containing the updated general settings.
	 * @return A {@link ApiResponse} object containing a success message with the updated
	 *         settings or an error message if the update fails.
	 */
	public ApiResponse updateGeneralSettings(GeneralSettingsSmppHttpDTO generalSettingsDTO) {
		try {
			GeneralSettingsSmppHttp generalSetting = generalSettingsHttpSmppRepo.findById(generalSettingsDTO.getId());
			if (generalSetting == null) {
				return ResponseMapping.errorMessageNoFound("General settings to smpp/http was not found");
			}
			generalSetting = generalSettingsMapper.toEntity(generalSettingsDTO);
			var result = generalSettingsHttpSmppRepo.save(generalSetting);

			// store in Redis
			utilsBase.storeInRedis(
					GeneralSmscConstants.GENERAL_SETTINGS_HASH_NAME,
					GeneralSmscConstants.GENERAL_SETTINGS_SMPP_HTTP_CONFIG_KEY,
					generalSettingsDTO.toString());
			utilsBase.sendNotificationSocket(GENERAL_SETTINGS_SMPP_HTTP_ENDPOINT, "updated");
			
			return ResponseMapping.successMessage("General settings smpp-http updated success", generalSettingsMapper.toDTO(result));
		} catch (Exception e) {
			log.error("Error to update General Settings smpp-http", e);
			return ResponseMapping.exceptionMessage("Error to update General Settings smpp-http", e);
		}
	}
	
	/*
	 * smsc retry
	 */
	
	public ApiResponse getGeneralSmscRetry() {
		try {
			GeneralSmscRetry generalSmscRetry = generalSmscRetryRepo.findById(1);
			if (generalSmscRetry == null) {
				return ResponseMapping.errorMessageNoFound("General smsc retry was not found.");
			}
			
			return ResponseMapping.successMessage("Get general smsc retry success", generalSettingsMapper.toDTOSmscRetry(generalSmscRetry));
		} catch (Exception e) {
			log.error("Error to get general smsc retry -> {}", e.getMessage(), e);
			return ResponseMapping.exceptionMessage("Error to get general smsc retry", e);
		}
	}
	
	public ApiResponse updateGeneralSmscRetry(GeneralSmscRetryDTO generalSmscRetry) {
		try {
			GeneralSmscRetry generalSmscRetryEntity = generalSmscRetryRepo.findById(generalSmscRetry.getId());
			if (generalSmscRetryEntity == null) {
				return ResponseMapping.errorMessageNoFound("General smsc retry was not found");
			}
			generalSmscRetryEntity = generalSettingsMapper.toEntitySmscRetry(generalSmscRetry);
			var result = generalSmscRetryRepo.save(generalSmscRetryEntity);

			utilsBase.storeInRedis(
					GeneralSmscConstants.GENERAL_SETTINGS_HASH_NAME,
					GeneralSmscConstants.GENERAL_SETTINGS_RETRIES_KEY,
					generalSmscRetry.toString());
			utilsBase.sendNotificationSocket(GENERAL_SMSC_RETRY_ENDPOINT, "updated");
			
			return ResponseMapping.successMessage("General smsc retry updated success", generalSettingsMapper.toDTOSmscRetry(result));
		} catch (Exception e) {
			log.error("Error to update general smsc retry -> {}", e.getMessage(), e);
			return ResponseMapping.exceptionMessage("Error to update general smsc retry", e);
		}
	}
}
