package com.smsc.management.app.errorcode.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.smsc.management.app.errorcode.dto.ErrorCodeMappingDTO;
import com.smsc.management.app.errorcode.dto.RedisErrorCodeMappingDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;
import com.smsc.management.app.errorcode.model.entity.ErrorCodeMapping;
import com.smsc.management.app.errorcode.mapper.ErrorCodeMappingMapper;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeMappingRepository;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import static com.smsc.management.utils.Constants.UPDATE_ERROR_CODE_MAPPING_ENDPOINT;

/**
 * Service class for processing error code mappings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorCodeMappingService {
	private final ErrorCodeMappingRepository errorCodeMappingRepo;
	private final ErrorCodeRepository errorCodeRepo;
	private final ErrorCodeMappingMapper errorCodeMappingMapper;
	private final UtilsBase utils;
	private final AppProperties appProperties;
	
	/**
     * Retrieves the error code mappings.
     *
     * @return A ResponseDTO containing the error code mappings.
     */
	public ApiResponse getErrorCodeMapping() {
		try {
			var errorCodeMappingList = errorCodeMappingRepo.fetchErrorCodeMappings();
			var result = utils.getErrorCodeMappings(errorCodeMappingList);
			return ResponseMapping.successMessage("get error code mapping request success", result);
		} catch (Exception e) {
			log.error("Error code mapping request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code mapping request was end with error on getErrorCodeMapping()", e);
		}
	}
	
	/**
     * Creates a new error code mapping.
     *
     * @param newErrorCodeMapping The new error code mapping to create.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse create(ErrorCodeMappingDTO newErrorCodeMapping) {
		try {
		var errorCodeMapping = errorCodeMappingMapper.toEntity(newErrorCodeMapping);
			var resultEntity = errorCodeMappingRepo.save(errorCodeMapping);
			
			// socket and redis action
			ErrorCode errorCodeEntity = errorCodeRepo.findById(resultEntity.getErrorCodeId());
			socketAndRedisAction(errorCodeEntity.getMnoId());
			
            return ResponseMapping.successMessage("New error code mapping added successful.", errorCodeMappingMapper.toDTO(resultEntity));
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("New error code mapping request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code mapping request was end with error on create()", e);
		}
	}
	
	/**
     * Updates an existing error code mapping.
     *
     * @param id The ID of the error code mapping to update.
     * @param errorCodeMapping The updated error code mapping information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse update(int id, ErrorCodeMappingDTO errorCodeMapping) {
		try {
			ErrorCodeMapping getErrorCodeMapping = errorCodeMappingRepo.findById(id);
			
			if (getErrorCodeMapping != null) {
				var tempCreatedAt = getErrorCodeMapping.getCreatedAt();
				errorCodeMapping.setId(id);
				
				errorCodeMappingMapper.updateToEntity(errorCodeMapping, getErrorCodeMapping);
				getErrorCodeMapping.setCreatedAt(tempCreatedAt);
				
				var resultEntity = errorCodeMappingRepo.save(getErrorCodeMapping);
				
				// socket and redis action
				ErrorCode errorCodeEntity = errorCodeRepo.findById(resultEntity.getErrorCodeId());
				socketAndRedisAction(errorCodeEntity.getMnoId());
				
                return ResponseMapping.successMessage("Error code mapping was update successful.", errorCodeMappingMapper.toDTO(resultEntity));
			}
			
			return ResponseMapping.errorMessageNoFound("Error code mapping was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Update error code mapping request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code mapping request was end with error", e);
		}
	}
	
	/**
     * Deletes an error code mapping.
     *
     * @param id The ID of the error code mapping to delete.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse delete(int id) {
		try {
			ErrorCodeMapping getErrorCodeMapping = errorCodeMappingRepo.findById(id);
			if (getErrorCodeMapping != null) {
				errorCodeMappingRepo.delete(getErrorCodeMapping);
				
				// socket and redis action
				ErrorCode errorCodeEntity = errorCodeRepo.findById(getErrorCodeMapping.getErrorCodeId());
				socketAndRedisAction(errorCodeEntity.getMnoId());
				
                return ResponseMapping.successMessage("Error code mapping deleted successful.", errorCodeMappingMapper.toDTO(getErrorCodeMapping));
			}
			
			return ResponseMapping.errorMessageNoFound("Error code mapping was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Delete error code mapping request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code mapping request was end with error", e);
		}
	}
	
	/**
     * A notification is sent by socket and the action on error code mapping is updated in Redis
     *
     * @param mnoId The ID of the Mobile Network Operator.
     * @return True if the actions were successful, false otherwise.
     */
	public boolean socketAndRedisAction(Integer mnoId) {
		try {
			List<RedisErrorCodeMappingDTO> errorCodeMapping = this.errorCodeMappingRepo.findByMnoId(mnoId);
			String errorCodeMappingString = errorCodeMapping.toString();
			
			if (!errorCodeMapping.isEmpty()) {
				this.utils.storeInRedis(GeneralSmscConstants.ERROR_CODE_MAPPING_HASH_NAME, String.valueOf(mnoId), errorCodeMappingString);
			} else {
				this.utils.removeInRedis(GeneralSmscConstants.ERROR_CODE_MAPPING_HASH_NAME, String.valueOf(mnoId));
			}
			
			this.utils.sendNotificationSocket(UPDATE_ERROR_CODE_MAPPING_ENDPOINT, String.valueOf(mnoId));
			return true;
		} catch (Exception e) {
			log.error("An error occurred while updating redis and sending notification socket when creating/updating/deleting error code mapping");
		}
		return false;
	}
}
