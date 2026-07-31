package com.smsc.management.app.errorcode.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.errorcode.model.entity.DeliveryErrorCode;
import com.smsc.management.app.errorcode.mapper.DeliveryErrorCodeMapper;
import com.smsc.management.app.errorcode.dto.DeliveryErrorCodeDTO;
import com.smsc.management.app.errorcode.model.repository.DeliveryErrorCodeRepository;
import com.smsc.management.utils.ResponseMapping;

/**
 * Service class for processing delivery error codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDeliveryErrorCode {
	private final DeliveryErrorCodeRepository deliveryErrorCodeRepo;
	private final DeliveryErrorCodeMapper deliveryErrorCodeMapper;

	/**
     * Retrieves all delivery error codes.
     *
     * @return A ResponseDTO containing the delivery error codes.
     */
	public ApiResponse getDeliveryErrorCode() {
		try {
			var result =  deliveryErrorCodeRepo.findAll();
			return ResponseMapping.successMessage("get delivery error code request success", deliveryErrorCodeMapper.toDTO(result));
		} catch (Exception e) {
			log.error("new delivery error code request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delivery error code request was end with error on getDeliveryErrorCode()", e);
		}
	}
	
	/**
     * Creates a new delivery error code.
     *
     * @param newErrorCode The delivery error code to create.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse create(DeliveryErrorCodeDTO newErrorCode) {
		try {
			var errorCodeEntity = deliveryErrorCodeMapper.toEntity(newErrorCode);
			var resultEntity = deliveryErrorCodeRepo.save(errorCodeEntity);
            return ResponseMapping.successMessage("Delivery error code added successful.", deliveryErrorCodeMapper.toDTO(resultEntity));
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("New delivery error Code request was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delivery error code request was end with error on create()", e);
		}
	}

	/**
     * Updates an existing delivery error code.
     *
     * @param id The ID of the delivery error code to update.
     * @param errorCode The updated delivery error code information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse update(int id, DeliveryErrorCodeDTO errorCode)
	{
		try {
			DeliveryErrorCode updateServerErrorCode = deliveryErrorCodeRepo.findById(id);
			if (updateServerErrorCode != null) {
				errorCode.setId(id);
				deliveryErrorCodeMapper.updateToEntity(errorCode, updateServerErrorCode);
				var resultEntity = deliveryErrorCodeRepo.save(updateServerErrorCode);

                return ResponseMapping.successMessage("Delivery error Code was update successful.", deliveryErrorCodeMapper.toDTO(resultEntity));
			}
			return ResponseMapping.errorMessageNoFound("Delivery error Code was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Delivery error Code request was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delivery error code request was end with error", e);
		}
	}
	
	/**
     * Deletes a delivery error code.
     *
     * @param id The ID of the delivery error code to delete.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse delete(int id) {
		try {
			var deleteServerErrorCode = deliveryErrorCodeRepo.findById(id);
			
			if (deleteServerErrorCode != null) {
				deliveryErrorCodeRepo.delete(deleteServerErrorCode);
                return ResponseMapping.successMessage("Delivery error Code deleted successful.", deliveryErrorCodeMapper.toDTO(deleteServerErrorCode));
			}
			
			return ResponseMapping.errorMessageNoFound("Server Error Code was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("delivery error Code request was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delivery error code request was end with error", e);
		}
	}
}
