package com.smsc.management.app.mno.service;

import com.smsc.management.app.gateway.service.GatewaysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.mno.mapper.OperatorMnoMapper;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.utils.ResponseMapping;
import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;

/**
 * Service class for processing Operator MNO (Mobile Network Operator) entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatorMnoService {
	private final OperatorMnoRepository operatorMnoRepo;
	private final GatewaysRepository gatewaysRepo;
	private final Ss7GatewaysRepository ss7GatewaysRepo;
	private final ErrorCodeRepository errorCodeRepo;
	private final OperatorMnoMapper operatorMapper;
	private final GatewaysService gatewaysService;
	
	/**
     * Retrieves all Operator MNO entities.
     *
     * @return A ResponseDTO containing the list of Operator MNO entities.
     */
	public ApiResponse getOperators() {
		try {
			var result = operatorMnoRepo.findByEnabledTrue();
			return ResponseMapping.successMessage("Get Operator MNO request success", operatorMapper.toDTO(result));
		} catch (Exception e) {
			log.error("Get operators MNO list with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Operator MNO was end with error while getting data", e);
		}
	}

	/**
     * Creates a new Operator MNO entity.
     *
     * @param newOperator The new Operator MNO entity to be created.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse create(OperatorMNODTO newOperator) {
		try {
			var operatorMno = operatorMapper.toEntity(newOperator);
			var resultEntity = operatorMnoRepo.save(operatorMno);
            return ResponseMapping.successMessage("New Operator MNO added successful.", operatorMapper.toDTO(resultEntity));
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("New Operator MNO with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("New Operator MNO was end with error", e);
		}
	}
	
	/**
     * It is not possible to eliminate the MNO because it is related to the gateways, therefore it will only be disabled.
     *
     * @param id The ID of the Operator MNO entity to be deleted.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse delete(int id) {
		try {
			var deleteOperatorMno = operatorMnoRepo.findByIdAndEnabledTrue(id);
			if (deleteOperatorMno != null) {
				/*
				 * search if the gateway associated with the MNO id exists
				 */
				var gatewaysMno = gatewaysRepo.findByMnoIdAndEnabledNot(id, DELETED_ENABLED_STATUS);
				var ss7GatewaysMno = ss7GatewaysRepo.findByMnoIdAndEnabledNot(id, DELETED_ENABLED_STATUS);
				if (!gatewaysMno.isEmpty() || !ss7GatewaysMno.isEmpty()) {
					return ResponseMapping.errorMessage("Error Detail: The gateway table key is still referenced.");
				}
				
				/*
				 * search if the error code associated with the MNO id exists
				 */
				var errorCodeMNO = errorCodeRepo.findByMnoId(id);
				if (!errorCodeMNO.isEmpty()) {
					return ResponseMapping.errorMessage("Error Detail: The error_code table key is still referenced.");
				}
				
				/*
				 * delete MNO
				 */
				deleteOperatorMno.setEnabled(false);
				operatorMnoRepo.save(deleteOperatorMno);
                return ResponseMapping.successMessage("Operator MNO deleted successful.", operatorMapper.toDTO(deleteOperatorMno));
			}
			return ResponseMapping.errorMessageNoFound("Operator MNO was not found.");
		} catch (DataIntegrityViolationException e) {
			throw e;
		}  catch (Exception e) {
			log.error("Delete Operator MNO was end with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Operator MNO was end with error", e);
		}
	}
	
	/**
     * Updates an existing Operator MNO entity.
     *
     * @param id The ID of the Operator MNO entity to be updated.
     * @param updateProvider The updated Operator MNO entity information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	public ApiResponse update(int id, OperatorMNODTO updateProvider) {
		try {
			var updateOperatorMno = operatorMnoRepo.findByIdAndEnabledTrue(id);
			if (updateOperatorMno == null) {
				return ResponseMapping.errorMessageNoFound("Operator MNO was not found.");
			}

			updateOperatorMno.setName(updateProvider.getName());
			updateOperatorMno.setMessageIdDecimalFormat(updateProvider.isMessageIdDecimalFormat());
			updateOperatorMno.setTlvMessageReceiptId(updateProvider.isTlvMessageReceiptId());
			var resultEntity = operatorMnoRepo.save(updateOperatorMno);

			if (gatewaysService.updateAllGatewayByMno(id)) {
				log.info("All gateways associated with mnoId {} were successfully updated.", id);
			}

			return ResponseMapping.successMessage("Operator MNO was updated successfully.", operatorMapper.toDTO(resultEntity));
		} catch (DataIntegrityViolationException e) {
			throw e;
		} catch (Exception e) {
			log.error("Update Operator MNO with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Operator MNO update ended with error", e);
		}
	}
}
