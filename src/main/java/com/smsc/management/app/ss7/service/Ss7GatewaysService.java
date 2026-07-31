package com.smsc.management.app.ss7.service;

import java.util.List;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.AppProperties;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.utils.UtilsBase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class Ss7GatewaysService {
	private final Ss7GatewaysRepository ss7GatewayRepo;
	private final Ss7GatewaysMapper ss7GatewayMapper;
	private final SequenceNetworksIdGenerator seqGateway;
	private final M3uaService m3uaService;
	private final UtilsBase utilsBase;
	private final ObjectSs7Service objectSs7Service;
    private final JwtService jwtService;
	private final AppProperties appProperties;
	private final SipGatewaysRepository sipGatewaysRepository;

	/**
     * Retrieves the list of ss7 gateways
     *
     * @return A ResponseDTO containing the list of gateways.
     */
    public ApiResponse getGateways() {
        try {
            List<Ss7Gateways> result = ss7GatewayRepo.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC);
            return ResponseMapping.successMessage(Constants.SS7_SUCCESS_MESSAGE_RESPONSE, ss7GatewayMapper.toDTOList(result));
        } catch (Exception e) {
            log.error("Ss7 Gateways request with error on get : {}", e.getMessage());
            return ResponseMapping.exceptionMessage(Constants.SS7_ERROR_MESSAGE_RESPONSE, e);
        }
    }

	/**
	 * Retrieves the list of ss7 gateways in IP-SM-gw
	 *
	 * @return A ResponseDTO containing the list of gateways.
	 */
	public ApiResponse getGatewaysIpSmGw() {
		try {
			List<Ss7Gateways> result = ss7GatewayRepo.findAllForIpSmGw(Constants.DELETED_ENABLED_STATUS);
			return ResponseMapping.successMessage(Constants.SS7_SUCCESS_MESSAGE_RESPONSE, ss7GatewayMapper.toDTOList(result));
		} catch (Exception e) {
			log.error("Ss7 Gateways request with error on get : {}", e.getMessage());
			return ResponseMapping.exceptionMessage(Constants.SS7_ERROR_MESSAGE_RESPONSE, e);
		}
	}

	/**
	 * Retrieves a concrete SS7 Gateway.
	 *
	 * @return ResponseDTO containing the matching SS7 Gateway by external_id field
	 */
	public ApiResponse getSs7GatewayByExternalId(String externalId) {
		try
		{
			Ss7Gateways foundSs7Gateway =  ss7GatewayRepo.findByExternalId(externalId);
			if (foundSs7Gateway == null) {
				return ResponseMapping.errorMessageNoFound("The requested SS7 gateway was not found.");
			}
			Ss7GatewaysDTO parseSs7GatewayDto = ss7GatewayMapper.toDTO(foundSs7Gateway);
			return ResponseMapping.successMessage("Get SS7 gateway by external id request success", parseSs7GatewayDto);
		} catch (Exception e) {
			log.error("Error while getting the SS7 gateway by external id # {} : {}",externalId, e.getMessage());
			return ResponseMapping.exceptionMessage("Get SS7 gateway by external id was end with error", e);
		}
	}
    
    /**
     * Creates a new ss7 gateway
     *
     * @param newSs7Gateways The new ss7 gateway to create.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	@Transactional
    public ApiResponse create(Ss7GatewaysDTO newSs7Gateways) {
    	try {
			validateUssiAndHomeRouting(newSs7Gateways);
			// Calculate messages_per_second as sum of all priority values
			newSs7Gateways.setMessagesPerSecond();
			
			Ss7Gateways ss7GatewayEntity = ss7GatewayMapper.toEntity(newSs7Gateways);
			ss7GatewayEntity.setNetworkId(seqGateway.getNextNetworkIdSequenceValue("GW"));
			ss7GatewayEntity.setStatus(Constants.DEFAULT_STATUS);
			ss7GatewayEntity.setEnabled(0);
            if (newSs7Gateways.isApiEnabled()) {
                String token = jwtService.generateToken();
                ss7GatewayEntity.setAppToken(token);
            }
			var result = ss7GatewayRepo.save(ss7GatewayEntity);
			
			return ResponseMapping.successMessage("New ss7 gateways added successful.", ss7GatewayMapper.toDTO(result));
		} catch (Exception e) {
			log.error("New ss7 gateways request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage(Constants.SS7_ERROR_MESSAGE_RESPONSE, e);
		}
    }
    
    /**
     * Updates an existing ss7 gateway
     *
     * @param id The networkId of the gateway to update.
     * @param ss7Gateways The updated ss7 gateways information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
	@Transactional
    public ApiResponse update(Object id, Ss7GatewaysDTO ss7Gateways) {
    	try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "SS7 gateways with external id= " : "SS7 gateways with network_id= ";
			Ss7Gateways currentSs7Gateways = searchByExternalId ? ss7GatewayRepo.findByExternalId((String)id) : ss7GatewayRepo.findById((int)id);
			if (currentSs7Gateways != null) {
				if (currentSs7Gateways.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
                    return ResponseMapping.errorMessage("Illegal exception it is not possible to modify a deleted ss7 gateways.");
                }
				validateUssiAndHomeRouting(ss7Gateways);
				validateUssiRemoval(currentSs7Gateways, ss7Gateways);
				validateHomeRoutingActivation(ss7Gateways);
				int previousEnableServer = currentSs7Gateways.getEnabled();
				ss7Gateways.setStatus(utilsBase.findStatusByEnabled(ss7Gateways.getEnabled()));
				
				// Calculate messages_per_second as sum of all priority values
				ss7Gateways.setMessagesPerSecond();
				
				ss7GatewayMapper.updateEntityFromDTO(ss7Gateways, currentSs7Gateways);
                if (currentSs7Gateways.isApiEnabled() &&
                        (currentSs7Gateways.getAppToken() == null || currentSs7Gateways.getAppToken().isBlank())) {
                    String token = jwtService.generateToken();
                    currentSs7Gateways.setAppToken(token);
                }
				var result = ss7GatewayRepo.save(currentSs7Gateways);

				this.manageRedisAndSocketAction(result, previousEnableServer);
				return ResponseMapping.successMessage("ss7 gateways updated successful.", ss7GatewayMapper.toDTO(result));
			}
			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
		} catch (Exception e) {
			log.error("Update ss7 gateways request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage(Constants.SS7_ERROR_MESSAGE_RESPONSE, e);
		}
    }

	/**
	 * Manages the status propagation actions for M3UA, as well as updates to Redis
	 * and socket notifications.
	 *
	 * @param gateway the instance of Ss7Gateways containing the updated status and network ID.
	 * @param previousEnableServer the previous enabled state of the server before the update.
	 *
	 * @throws Exception if an error occurs during the status propagation or the Redis or socket actions.
	 *                   In such cases, the enabled state of the gateway is reverted to its previous value.
	 */
	private void manageRedisAndSocketAction(Ss7Gateways gateway, int previousEnableServer) throws Exception {
		try {
			m3uaService.propagateStatusForM3ua(gateway.getNetworkId(), gateway.getEnabled());
			if (this.manageRedisAction(gateway, previousEnableServer)) {
				this.manageSocketNotifications(gateway, previousEnableServer);
			}
		} catch (Exception e) {
			gateway.setEnabled(previousEnableServer);
			ss7GatewayRepo.save(gateway);
			throw e;
		}
	}
    
    public ApiResponse getGatewaysByNetworkId(int id) {
		try {
			Ss7Gateways result = ss7GatewayRepo.findById(id);
			if (result != null) {
				return ResponseMapping.successMessage(Constants.SS7_SUCCESS_MESSAGE_RESPONSE, ss7GatewayMapper.toDTO(result));
			}
			return ResponseMapping.errorMessageNoFound("ss7 gateways with network_id= " + id + " was not found.");
		} catch (Exception e) {
			log.error("Ss7 Gateways request with error on get by networkId: {}", e.getMessage());
			return ResponseMapping.exceptionMessage(Constants.SS7_ERROR_MESSAGE_RESPONSE, e);
		}
	}

	/**
	 * Handles socket notifications for an SS7 gateway.
	 * This method checks if the enabled state of the gateway has changed
	 * compared to the previously provided state. If there is a change,
	 * it sends a notification to the corresponding endpoint. Otherwise,
	 * it sends a notification to the update endpoint.
	 *
	 * @param gateway the SS7 gateway whose state is being managed
	 * @param enablePrevious the previous enabled state of the gateway
	 */
	private void manageSocketNotifications(Ss7Gateways gateway, int enablePrevious) {
		if (gateway.getEnabled() != enablePrevious) {
			String endpoint = this.utilsBase.findEndpointToGateway(gateway.getProtocol(), gateway.getEnabled());
			this.utilsBase.sendNotificationSocket(endpoint, Integer.toString(gateway.getNetworkId()));
		} else {
			this.utilsBase.sendNotificationSocket(
					this.utilsBase.findUpdateEndpointByProtocolToGw(gateway.getProtocol()),
					Integer.toString(gateway.getNetworkId())
			);
		}
	}

	private boolean manageRedisAction(Ss7Gateways gateway, int enablePrevious) throws Exception {
		boolean isUpdate = false;
		if (gateway.getEnabled() != enablePrevious) {
			if (gateway.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
				this.utilsBase.removeInRedis(GeneralSmscConstants.SS7_GATEWAYS_HASH_NAME, Integer.toString(gateway.getNetworkId()));
				this.utilsBase.removeInRedis(GeneralSmscConstants.SS7_SETTINGS_HASH_NAME, Integer.toString(gateway.getNetworkId()));
			} else {
				this.objectSs7Service.updateOrCreateJsonInRedis(gateway.getNetworkId());
			}
			isUpdate = true;
		}
		return isUpdate;
	}

    public ApiResponse generateGatewayToken(Object id) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "SS7 gateways with external id= " : "SS7 gateways with network_id= ";
            Ss7Gateways currentSs7Gateways = searchByExternalId ? ss7GatewayRepo.findByExternalId((String) id) : ss7GatewayRepo.findById((int) id);

            if (currentSs7Gateways == null) {
                return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
            }
            
            String token = jwtService.generateToken();
            currentSs7Gateways.setAppToken(token);
            ss7GatewayRepo.save(currentSs7Gateways);
            return ResponseMapping.successMessage("Gateway token generated successfully.", ss7GatewayMapper.toDTO(currentSs7Gateways));
        } catch (Exception e) {
            log.error("Generate token for gateway {} failed: {}", id, e.getMessage());
            return ResponseMapping.exceptionMessage("Generate token for gateway finished with error", e);
        }
    }
	private void validateUssiAndHomeRouting(Ss7GatewaysDTO dto) {
		if (dto.isAllowedUssi() && dto.isHomeRouting()) {
			throw new IllegalArgumentException("USSI traffic and Home Routing cannot be enabled at the same time.");
		}
	}

	private void validateUssiRemoval(Ss7Gateways current, Ss7GatewaysDTO dto) {
		boolean removingUssi = current != null && current.isAllowedUssi() && !dto.isAllowedUssi();

		if (removingUssi && sipGatewaysRepository.existsByRoutingUssiTrafficSs7GatewayId(current.getNetworkId())) {
			throw new IllegalArgumentException("USSI traffic cannot be disabled because this SS7 gateway is associated with a SIP gateway.");
		}
	}

	private void validateHomeRoutingActivation(Ss7GatewaysDTO dto) {
		if (dto.isHomeRouting() && sipGatewaysRepository.existsByRoutingUssiTrafficSs7GatewayId(dto.getNetworkId())) {
			throw new IllegalArgumentException("Home Routing cannot be enabled because this SS7 gateway is associated with a SIP gateway.");
		}
	}
}
