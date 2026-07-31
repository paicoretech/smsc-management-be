package com.smsc.management.app.errorcode.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.smsc.management.app.errorcode.dto.ParseErrorCodeDTO;
import com.smsc.management.app.errorcode.dto.ErrorCodeDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;
import com.smsc.management.app.errorcode.mapper.ErrorCodeMapper;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.utils.ResponseMapping;

/**
 * Service class for processing error codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorCodeService {
	private final ErrorCodeRepository errorCodeRepo;
	private final ErrorCodeMapper errorCodeMapper;

	/**
     * Retrieves a list of error codes.
     *
     * @return A ResponseDTO containing the list of error codes.
     */
	public ApiResponse getErrorCodeList() {
		try {
			List<ParseErrorCodeDTO> providerErroCode = errorCodeRepo.fetchErrorCode();
			return ResponseMapping.successMessage("get error code request success", providerErroCode);
		} catch (Exception e) {
			log.error("new provider error code request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code request was end with error", e);
		}
	}
	
	/**
     * Retrieves a list of error codes by Mobile Network Operator (MNO) ID.
     *
     * @param mnoId The ID of the Mobile Network Operator.
     * @return A ResponseDTO containing the list of error codes for the specified MNO.
     */
	public ApiResponse getErrorCodeListByMNO(int mnoId) {
		try {
			var result = errorCodeRepo.findByMnoId(mnoId);
			return ResponseMapping.successMessage("get error code  by MNO request success", errorCodeMapper.toListDTO(result));
		} catch (Exception e) {
			log.error("new error code request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code request was end with error", e);
		}
	}
	
	/**
     * Creates a new error code.
     *
     * @param newErrorCode The error code to create.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse create(ErrorCodeDTO newErrorCode) {
		try {
			var errorCodeEntity = errorCodeMapper.toEntity(newErrorCode);
			var resultEntity = errorCodeRepo.save(errorCodeEntity);
            return ResponseMapping.successMessage("Error code added successful.", errorCodeMapper.toDTO(resultEntity));
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("New Error Code was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error code was end with error", e);
		}
	}
	
	/**
     * Updates an existing error code.
     *
     * @param id The ID of the error code to update.
     * @param updateErrorCode The updated error code information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse update(int id, ErrorCodeDTO updateErrorCode) {
		try {
			ErrorCode getErrorCode = errorCodeRepo.findById(id);
			if (getErrorCode != null) {
				updateErrorCode.setId(id);
				errorCodeMapper.updateToEntity(updateErrorCode, getErrorCode);
				var resultEntity = errorCodeRepo.save(getErrorCode);
                return ResponseMapping.successMessage("Error Code was update successful.", errorCodeMapper.toDTO(resultEntity));
			}
			
			return ResponseMapping.errorMessageNoFound("Error Code was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Update Error Code was with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error Code was end with error", e);
		}
	}
	
	/**
     * Deletes an error code.
     *
     * @param id The ID of the error code to delete.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse delete(int id) {
		try {
			var deleteErrorCode = errorCodeRepo.findById(id);
			if (deleteErrorCode != null) {
				errorCodeRepo.delete(deleteErrorCode);
                return ResponseMapping.successMessage("Error Code deleted successful.", errorCodeMapper.toDTO(deleteErrorCode));
			}
			
			return ResponseMapping.errorMessageNoFound("Error Code was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Deleted Error Code was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error Code was end with error", e);
		}
	}
}
