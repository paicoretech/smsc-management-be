package com.smsc.management.app.gateway.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.gateway.dto.GatewaysInterpreterDTO;
import com.smsc.management.app.gateway.model.repository.GatewaySearchCriteria;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.headers.model.repository.CallbackHeaderHttpRepository;
import com.smsc.management.app.interpreter.mapper.InterpreterMapper;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import com.smsc.management.app.interpreter.service.DefaultTemplateService;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.gateway.dto.ParseGatewaysDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.gateway.mapper.GatewaysMapper;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.DISABLED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.ACTIVE_ENABLED_STATUS;

/**
 * Service class for processing gateways.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewaysService {
    private final GatewaysRepository gatewaysRepo;
    private final GatewaysMapper gatewaysMapper;
    private final InterpreterMapper interpreterMapper;
    private final UtilsBase utilsBase;
    private final OperatorMnoRepository operatorRepo;
    private final SequenceNetworksIdGenerator seqGateway;
    private final InterpreterRepository interpreterRepository;
    private final DefaultTemplateService defaultTemplateService;
    private final CallbackHeaderHttpRepository callbackHeaderRepo;

    /**
     * Retrieves the list of gateways
     *
     * @return A ResponseDTO containing the list of gateways.
     */
    public ApiResponse getGateways() {
        try {
            List<Gateways> gatewaysEntity = gatewaysRepo.findByEnabledNot(DELETED_ENABLED_STATUS);
            List<ParseGatewaysDTO> gatewaysList = gatewaysMapper.toDTO(gatewaysEntity);

            return ResponseMapping.successMessage("Get gateways request success", gatewaysList);
        } catch (Exception e) {
            log.error("Error to get gateways: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Gateways was end with error", e);
        }
    }

    /**
     * Retrieves a concrete Gateway.
     *
     * @return ResponseDTO containing the matching Gateway by external_id field
     */
    public ApiResponse getGatewayByExternalId(String externalId) {
        try {
            Gateways foundGateway = gatewaysRepo.findByExternalId(externalId);
            if (foundGateway == null) {
                return ResponseMapping.errorMessageNoFound("The requested gateway was not found.");
            }
            ParseGatewaysDTO parseGatewayDto = gatewaysMapper.toDTO(foundGateway);
            return ResponseMapping.successMessage("Get gateway by external id request success", parseGatewayDto);
        } catch (Exception e) {
            log.error("Error while getting the gateway by external id # {} : {}", externalId, e.getMessage());
            return ResponseMapping.exceptionMessage("Get gateway by external id was end with error", e);
        }
    }

    /**
     * Creates a new gateway, notify by socket and update Redis.
     *
     * @param newGateways The new gateway to create.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
    public ApiResponse create(GatewaysDTO newGateways) {
        try {
            String existingGwMessage = newGateways.getProtocol().equalsIgnoreCase("SMPP") ?
                    "port, system type, interface version and system id. " :
                    "and system id. ";
            this.utilsBase.isRequestDlrAndTransmitterBind(newGateways.getBindType(), newGateways.getRequestDlr(), newGateways.getProtocol());
            this.utilsBase.validateMaxLengthSpAndGw(newGateways.getPassword(), newGateways.getSystemId(), newGateways.getProtocol());
            this.validateCallbackUrl(newGateways.getIp(), newGateways.getProtocol());
            this.validateTokenAuthorization(newGateways);

            boolean existActiveGateway =
                    existsGateway(
                            new ExistGatewayValidatorParams(
                                    0,
                                    newGateways.getIp(),
                                    newGateways.getPort(),
                                    newGateways.getSystemType(),
                                    newGateways.getInterfaceVersion(),
                                    newGateways.getBindType(),
                                    "create",
                                    newGateways.getSystemId(),
                                    newGateways.getProtocol(),
                                    DELETED_ENABLED_STATUS
                            )
                    );

            if (existActiveGateway) {
                return ResponseMapping.errorMessage("There is already an active or disabled gateway with the same IP, " +
                        existingGwMessage +
                        "You must assign a different IP, " + existingGwMessage);
            }

            // Calculate messages_per_second as sum of all priority values
            newGateways.setMessagesPerSecond();
            
            var gatewayEntity = gatewaysMapper.toEntity(newGateways);
            gatewayEntity.setNetworkId(seqGateway.getNextNetworkIdSequenceValue("GW"));
            gatewayEntity.setStatus(Constants.DEFAULT_STATUS);
            gatewayEntity.setActiveSessionsNumbers(0);
            var resultEntity = gatewaysRepo.save(gatewayEntity);

            // create interpreter template default for http gateway
            if ("HTTP".equalsIgnoreCase(resultEntity.getProtocol())) {
                defaultTemplateService.createDefaultTemplate(resultEntity.getNetworkId());
            }

            // create format to redis
            ParseGatewaysDTO redisGatewayDTO = this.createGatewayFormatToRedis(resultEntity.getNetworkId());

            String gatewayString = redisGatewayDTO.toString();
            Objects.requireNonNull(gatewayString, "gatewayString should not be null");
            utilsBase.storeInRedis(GeneralSmscConstants.SMPP_HTTP_GATEWAYS_HASH_NAME, String.valueOf(resultEntity.getNetworkId()), gatewayString);

            String endpoint = utilsBase.findUpdateEndpointByProtocolToGw(redisGatewayDTO.getProtocol());
            String networkId = String.valueOf(resultEntity.getNetworkId());
            utilsBase.sendNotificationSocket(
                    endpoint,
                    networkId
            );

            log.info("New gateway: {}", gatewayString);
            return ResponseMapping.successMessage("Gateway added successful.", redisGatewayDTO);
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("New gateways request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New gateways request with error", e);
        }
    }

    /**
     * Updates an existing gateway, notify by socket and update Redis.
     *
     * @param id      The ID of the gateway to update.
     * @param gateway The updated gateway information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
    @Transactional
    public ApiResponse update(Object id, GatewaysDTO gateway) {
        try {
            boolean searchByExternalId = id instanceof String;
            this.utilsBase.isRequestDlrAndTransmitterBind(gateway.getBindType(), gateway.getRequestDlr(), gateway.getProtocol());
            this.utilsBase.validateMaxLengthSpAndGw(gateway.getPassword(), gateway.getSystemId(), gateway.getProtocol());
            this.validateCallbackUrl(gateway.getIp(), gateway.getProtocol());
            this.validateTokenAuthorization(gateway);

            var findGatewayEntity = searchByExternalId ? gatewaysRepo.findByExternalId((String) id) : gatewaysRepo.findById((int) id);
            if (Objects.isNull(findGatewayEntity)) {
                String notFoundMessagePrefix = searchByExternalId ? "SS7 Gateway with external id= " : "SS7 Gateway with network_id= ";
                return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
            }

            boolean existActiveGateway = existsGateway(
                    new ExistGatewayValidatorParams(
                            findGatewayEntity.getNetworkId(),
                            gateway.getIp(),
                            gateway.getPort(),
                            gateway.getSystemType(),
                            gateway.getInterfaceVersion(),
                            gateway.getBindType(),
                            "update",
                            gateway.getSystemId(),
                            gateway.getProtocol(),
                            DELETED_ENABLED_STATUS
                    )
            );
            if (existActiveGateway) {
                return ResponseMapping.errorMessage("The gateway with the same IP, port, system type, and interface version is not found.");
            }

            if (findGatewayEntity.getEnabled() == DELETED_ENABLED_STATUS) {
                return ResponseMapping.errorMessage("Illegal exception it is not possible to modify a deleted account.");
            }

            int enablePrevious = findGatewayEntity.getEnabled();
            gateway.setNetworkId(findGatewayEntity.getNetworkId());
            
            // Calculate messages_per_second as sum of all priority values
            gateway.setMessagesPerSecond();
            
            gatewaysMapper.updateEntityFromDTO(gateway, findGatewayEntity);
            Gateways resultEntity = gatewaysRepo.save(findGatewayEntity);

            if (resultEntity.getEnabled() == DELETED_ENABLED_STATUS) {
                interpreterRepository.deleteAllByGatewayId(resultEntity.getNetworkId());
            }

            // create format to Redis
            ParseGatewaysDTO redisGatewayDTO = this.createGatewayFormatToRedis(resultEntity.getNetworkId());

            utilsBase.storeInRedis(GeneralSmscConstants.SMPP_HTTP_GATEWAYS_HASH_NAME, String.valueOf(resultEntity.getNetworkId()), redisGatewayDTO.toString());
            this.manageSocketNotifications(gateway, redisGatewayDTO, enablePrevious);
            return ResponseMapping.successMessage("Gateway updated successfully.", redisGatewayDTO);

        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Update gateways request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update gateways request with error", e);
        }
    }

    public void validateCallbackUrl(String callbackUrl, String protocol) {
        if (protocol.equalsIgnoreCase("http") && (callbackUrl == null || callbackUrl.isBlank())) {
            throw new SmscBackendException("callback url is required to HTTP protocol");
        }
    }

    public void validateTokenAuthorization(GatewaysDTO gw) {
        try {
            if ("http".equalsIgnoreCase(gw.getProtocol())) {
                if ("basic".equalsIgnoreCase(gw.getAuthenticationTypes())) {
                    if (gw.getPasswd() == null || gw.getPasswd().isBlank()) {
                        throw new SmscBackendException("User name is required to Basic authentication");
                    }
                    if (gw.getUserName() == null || gw.getUserName().isBlank()) {
                        throw new SmscBackendException("password is required to Basic authentication");
                    }

                    String authString = gw.getUserName() + ":" + gw.getPasswd();
                    String authEncoded = Base64.getEncoder().encodeToString(authString.getBytes());
                    gw.setToken(authEncoded);
                } else if (!"undefined".equalsIgnoreCase(gw.getAuthenticationTypes()) && (gw.getToken() == null || gw.getToken().isBlank())) {
                    throw new SmscBackendException("Token is required to " + gw.getAuthenticationTypes() + " authentication");
                }
            }
        } catch (Exception e) {
            throw new SmscBackendException(e.getMessage());
        }
    }

    public boolean existsGateway(ExistGatewayValidatorParams params) {
        if (params.enabled == null || params.enabled.length == 0 || params.action == null) {
            return false;
        }
        try {
            for (Integer enable : params.enabled) {
                GatewaySearchCriteria gwSearchCriteria = new GatewaySearchCriteria(params.ip, params.port, params.systemType,
                        params.interfaceVersion, params.bindType, enable, params.networkId, params.systemId);

                boolean exists = switch (params.action) {
                    case "update" -> params.protocol.equalsIgnoreCase("SMPP") ?
                            // Validating all combinations for SMPP (IP + Port + SysType + IFVersion + BindType + enabled + SystemID)
                            gatewaysRepo.existsByGatewaySearchCriteria(gwSearchCriteria) :
                            // Validating only HTTP-Properties (IP + enabled + SystemID)
                            gatewaysRepo.existsByIpAndEnabledNotAndNetworkIdNotAndSystemId(params.ip, enable, params.networkId, params.systemId);
                    case "create" -> params.protocol.equalsIgnoreCase("SMPP") ?
                            // Validating all combinations for SMPP (IP + Port + SysType + IFVersion + BindType + enabled + SystemID)
                            gatewaysRepo.existsByIpAndPortAndSystemTypeAndInterfaceVersionAndBindTypeAndEnabledNotAndSystemId(
                                    params.ip, params.port, params.systemType,
                                    params.interfaceVersion, params.bindType, enable, params.systemId) :
                            // Validating only HTTP-Properties (IP + enabled + SystemID)
                            gatewaysRepo.existsByIpAndEnabledNotAndSystemId(
                                    params.ip, enable, params.systemId);
                    default -> false;
                };
                if (exists) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("An error occurred while checking the existence of the gateway with IP: " +
                            "{}, port: {}, systemType: {}, interfaceVersion: {}, and enabled values: {}",
                    params.ip, params.port, params.systemType, params.interfaceVersion, Arrays.toString(params.enabled), e);
            return false;
        }
    }

    private void manageSocketNotifications(GatewaysDTO gateway, ParseGatewaysDTO redisGatewayDTO, int enablePrevious) {
        if (gateway.getEnabled() != enablePrevious) {
            String endpoint = utilsBase.findEndpointToGateway(gateway.getProtocol(), gateway.getEnabled());
            utilsBase.sendNotificationSocket(endpoint, String.valueOf(gateway.getNetworkId()));
            if (gateway.getEnabled() == 1) {
                log.info("Gateway with networkId {} was connected via websocket.", gateway.getNetworkId());
            }
        } else {
            String updateEndpoint = utilsBase.findUpdateEndpointByProtocolToGw(redisGatewayDTO.getProtocol());
            utilsBase.sendNotificationSocket(updateEndpoint, String.valueOf(gateway.getNetworkId()));
        }
    }

    /**
     * Creates a gateway format suitable for storing in Redis based on the network ID.
     *
     * @param networkId The network ID of the gateway.
     * @return A {@link ParseGatewaysDTO} representing the gateway format suitable for Redis, or null if an error occurs.
     */
    public ParseGatewaysDTO createGatewayFormatToRedis(int networkId) {
        try {
            // searching gateway by networkId
            Gateways gatewayEntity = gatewaysRepo.findById(networkId);
            ParseGatewaysDTO redisGatewayDTO = gatewaysMapper.toDTO(gatewayEntity);

            // interpreter
            List<Interpreter> interpreterList = interpreterRepository.findByGatewayId(networkId);
            if (!interpreterList.isEmpty()) {
                List<GatewaysInterpreterDTO> interpreterRedisList = this.getGatewaysInterpreterDTOS(interpreterList);
                redisGatewayDTO.setInterpreter(interpreterRedisList);
            }

            // adding message id fields from MNO
            OperatorMno mno = operatorRepo.findById(redisGatewayDTO.getMnoId());
            redisGatewayDTO.setTlvMessageReceiptId(mno.isTlvMessageReceiptId());
            redisGatewayDTO.setMessageIdDecimalFormat(mno.isMessageIdDecimalFormat());

            return redisGatewayDTO;
        } catch (Exception e) {
            log.error("Error create gateway format to redis {}", e.getMessage());
            return null;
        }
    }

    private List<GatewaysInterpreterDTO> getGatewaysInterpreterDTOS(List<Interpreter> interpreterList) {
        List<GatewaysInterpreterDTO> interpreterRedisList = new ArrayList<>();

        for (Interpreter interpreter : interpreterList) {
            GatewaysInterpreterDTO gatewaysInterpreterDTO = new GatewaysInterpreterDTO();
            gatewaysInterpreterDTO.setEventType(interpreter.getEventType());
            gatewaysInterpreterDTO.setDirection(interpreter.getDirection());
            gatewaysInterpreterDTO.setBodyType(interpreter.getBodyType());
            gatewaysInterpreterDTO.setTemplate(interpreter.getTemplate());
            gatewaysInterpreterDTO.setUseProxy(interpreter.isUseProxy());
            gatewaysInterpreterDTO.setPath(interpreter.getPath());

            List<CallbackHeaderHttp> headers = callbackHeaderRepo.findByInterpreterId(interpreter.getId());
            List<CallbackHeaderHttpDTO> headersDTO = interpreterMapper.toDTOCallbackHeader(headers);
            gatewaysInterpreterDTO.setCallbackHeadersHttp(headersDTO);
            interpreterRedisList.add(gatewaysInterpreterDTO);
        }
        return interpreterRedisList;
    }

    /**
     * Loads gateway data for a specific network into Redis and sends a notification via socket.
     * loading and notification for gateways during the overall loading process.
     *
     * @param networkId The ID of the network for which to load gateway data.
     */
    public void onlyToLoadInitInRedisAndSocket(int networkId) {
        ParseGatewaysDTO redisGatewayDTO = this.createGatewayFormatToRedis(networkId);
        utilsBase.storeInRedis(GeneralSmscConstants.SMPP_HTTP_GATEWAYS_HASH_NAME, String.valueOf(redisGatewayDTO.getNetworkId()), redisGatewayDTO.toString());
        utilsBase.sendNotificationSocket(utilsBase.findUpdateEndpointByProtocolToGw(redisGatewayDTO.getProtocol()), String.valueOf(redisGatewayDTO.getNetworkId()));
    }

    /**
     * Updates all gateways associated with the specified MNO (Mobile Network Operator) in Redis.
     *
     * @param mnoId the ID of the MNO
     * @return true if the update is successful, false otherwise
     */
    public boolean updateAllGatewayByMno(int mnoId) {
        try {
            List<Gateways> gateways = gatewaysRepo.findByMnoIdAndEnabledNot(mnoId, DELETED_ENABLED_STATUS);
            for (Gateways gateway : gateways) {
                ParseGatewaysDTO redisGatewayDTO = this.createGatewayFormatToRedis(gateway.getNetworkId());
                utilsBase.storeInRedis(GeneralSmscConstants.SMPP_HTTP_GATEWAYS_HASH_NAME, String.valueOf(gateway.getNetworkId()), redisGatewayDTO.toString());
                utilsBase.sendNotificationSocket(utilsBase.findUpdateEndpointByProtocolToGw(gateway.getProtocol()), String.valueOf(gateway.getNetworkId()));
            }
            return true;
        } catch (Exception e) {
            log.error("Error to update gateway with mnoId {} -> {}", mnoId, e.getMessage());
            return false;
        }
    }

    public record ExistGatewayValidatorParams(
            int networkId,
            String ip,
            int port,
            String systemType,
            String interfaceVersion,
            String bindType,
            String action,
            String systemId,
            String protocol,
            Integer... enabled
    ) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExistGatewayValidatorParams that = (ExistGatewayValidatorParams) o;
            return networkId == that.networkId && port == that.port && enabled.length == that.enabled.length
                    && action.equals(that.action) && ip.equals(that.ip) && systemType.equals(that.systemType)
                    && interfaceVersion.equals(that.interfaceVersion) && bindType.equals(that.bindType)
                    && systemId.equals(that.systemId) && protocol.equals(that.protocol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(networkId, ip, port, systemType, interfaceVersion, bindType, action, Arrays.hashCode(enabled));
        }

        @Override
        public String toString() {
            return Converter.valueAsString(this);
        }
    }
}
