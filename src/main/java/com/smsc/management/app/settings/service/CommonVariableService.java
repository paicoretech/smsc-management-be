package com.smsc.management.app.settings.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.utils.UtilsBase;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smsc.management.init.LoadSpAndGwInRedis;
import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.settings.dto.CommonVariablesDTO;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.settings.model.entity.CommonVariables;
import com.smsc.management.app.settings.mapper.CommonVariablesMapper;
import com.smsc.management.app.settings.model.repository.CommonVariablesRepository;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.smsc.management.utils.Constants.GENERAL_SETTINGS_SMPP_HTTP_ENDPOINT;
import static com.smsc.management.utils.Constants.KEY_MAX_PASSWORD_LENGTH;
import static com.smsc.management.utils.Constants.KEY_MAX_SYSTEM_ID_LENGTH;
import static com.smsc.management.utils.Constants.LOCAL_CHARGING;
import static com.smsc.management.utils.Constants.SMSC_ACCOUNT_SETTINGS;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonVariableService {
	private final CommonVariablesRepository  commonVarRepo;
	private final GatewaysRepository gatewaysRepository;
	private final ServiceProviderRepository serviceProviderRepository;
	private final CommonVariablesMapper commonVarMapper;
	private final HandlerCreditByServiceProvider handlerCreditBySp;
	private final LoadSpAndGwInRedis loadSp;
	private final UtilsBase utilsBase;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public ApiResponse get(){
		try {
			List<CommonVariables> commonVar = commonVarRepo.findAll();
			return ResponseMapping.successMessage("Get common variables request success", commonVarMapper.toDTO(commonVar));
		} catch (Exception e) {
			log.error("Error to get system parameters: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get system parameters", e);
		}
	}

	public ApiResponse massiveUpdate(List<CommonVariablesDTO> variables) {
		try {
			boolean updateExecution = false;
			List<CommonVariables> commonVariablesList = new ArrayList<>();

			for (CommonVariablesDTO variable: variables) {
				String key = variable.getKey();
				CommonVariables keyData = commonVarRepo.findByKey(key);
				if (Objects.isNull(keyData)) {
                    log.warn("Key = {} was not found.", key);
					continue;
				}

				this.validateValue(variable.getValue(), keyData.getDataType());
				keyData.setValue(variable.getValue());
				this.preAction(keyData.getKey(), keyData.getValue());
				commonVariablesList.add(keyData);
			}

			List<CommonVariables> savedCommonVariablesList = commonVarRepo.saveAll(commonVariablesList);

			for (CommonVariables savedCommonVariable :  savedCommonVariablesList) {
				this.postAction(savedCommonVariable.getKey());
				updateExecution = true;
			}


			if (updateExecution) {
				// Send Redis and socket notifications for common settings
				this.updateCommonSettingsRedisAndSocket();
				return ResponseMapping.successMessage("successfully updated", null);
			}

			return ResponseMapping.errorMessage("Variables not updated, please try again.");
		} catch (Exception e) {
			log.error("Error to update parameter: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update parameter", e);
		}
	}
	
	private void preAction(String key, String value) throws JsonProcessingException {
        if (key.equals(SMSC_ACCOUNT_SETTINGS)) {
            JsonNode jsonNode = objectMapper.readTree(value);
            validateMaxPasswdAndSystemIdLength(jsonNode.get(KEY_MAX_SYSTEM_ID_LENGTH).asInt(), jsonNode.get(KEY_MAX_PASSWORD_LENGTH).asInt());
        }
	}

	private void postAction(String key) {
        if (key.equals(LOCAL_CHARGING)) {
            handlerCreditBySp.init();
            loadSp.loadSp();
        }
	}
	
	/**
	 * Updates Redis and sends socket notification for common settings that have redis_replicated=true
	 */
	private void updateCommonSettingsRedisAndSocket() {
		try {
			// Get all variables that should be replicated to Redis
			List<CommonVariables> replicatedVariables = commonVarRepo.findByRedisReplicatedTrue();

			// Convert to the format expected by Redis: [{"key": "USE_DND_FILTERING", "value": "false", "data_type": "boolean"}]
			List<CommonVariablesDTO> commonSettingsData = new ArrayList<>();
			for (CommonVariables variable : replicatedVariables) {
				CommonVariablesDTO dto = new CommonVariablesDTO();
				dto.setKey(variable.getKey());
				dto.setValue(variable.getValue());
				dto.setDataType(variable.getDataType());
				commonSettingsData.add(dto);
			}

			// Store in Redis under general_settings hash with key "common_settings"
			String jsonData = Converter.valueAsString(commonSettingsData);
			utilsBase.storeInRedis(GeneralSmscConstants.GENERAL_SETTINGS_HASH_NAME, GeneralSmscConstants.GENERAL_SETTINGS_COMMON_KEY, jsonData);

			// Send socket notification
			utilsBase.sendNotificationSocket(GENERAL_SETTINGS_SMPP_HTTP_ENDPOINT, "updated");
			log.info("Common settings updated in Redis and socket notification sent");
		} catch (Exception e) {
			log.error("Error updating common settings in Redis: {}", e.getMessage());
		}
	}

	/**
	 * Sync redis-replicated variables to Redis on startup
	 * Called from DataInitializer to ensure Redis has latest data
	 */
	public void syncRedisReplicatedVariables() {
		updateCommonSettingsRedisAndSocket();
	}

	private void validateValue(String value, String type) {
		switch (type) {
			case "boolean":
				this.validateBoolean(value);
				break;
			case "int":
				this.validateInt(value);
				break;
			case "json":
				this.validateJson(value);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}


	private void validateBoolean(String value) {
		if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
			throw new IllegalArgumentException("Invalid boolean value: " + value);
		}
	}

	private void validateInt(String value) {
		if (!value.matches("^-?\\d+$")) {
			throw new IllegalArgumentException("Invalid integer value: " + value);
		}
	}

	private void validateJson(String value) {
        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            if (!jsonNode.isObject() && !jsonNode.isArray()) {
                throw new IllegalArgumentException("Invalid JSON value");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON value", e);
        }
    }

	private void validateMaxPasswdAndSystemIdLength(int maxSystemIdLength, int maxPasswordLength) {
		int result = serviceProviderRepository.countBySystemIdLengthGreaterThan(maxSystemIdLength);
		if (result > 0) {
			throw new IllegalArgumentException(result + " Service provider accounts were found with system id longer than the maximum length you want to configure.");
		}

		result = gatewaysRepository.countBySystemIdLengthGreaterThan(maxSystemIdLength);
		if (result > 0) {
			throw new IllegalArgumentException(result + " Gateways accounts were found with system id longer than the maximum length you want to configure.");
		}

		result = serviceProviderRepository.countByPasswordLengthGreaterThan(maxPasswordLength);
		if (result > 0) {
			throw new IllegalArgumentException(result + " Service provider accounts were found with passwords longer than the maximum length you want to configure.");
		}

		result = gatewaysRepository.countByPasswordLengthGreaterThan(maxPasswordLength);
		if (result > 0) {
			throw new IllegalArgumentException(result + " Gateways accounts were found with passwords longer than the maximum length you want to configure.");
		}
	}
}
