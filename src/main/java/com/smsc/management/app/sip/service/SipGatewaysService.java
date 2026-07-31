package com.smsc.management.app.sip.service;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.app.sip.dto.SipGatewaysDTO;
import com.smsc.management.app.sip.mapper.SipGatewaysMapper;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.paicbd.smsc.utils.Converter;
import java.util.Arrays;
import java.util.Objects;
import jakarta.transaction.Transactional;
import java.util.List;

import static com.smsc.management.utils.Constants.DUPLICATE_SIP_GATEWAY_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SipGatewaysService {

    private final SipGatewaysRepository sipGatewaysRepository;
    private final SipGatewaysMapper sipGatewaysMapper;
    private final SequenceNetworksIdGenerator seqGateway;
    private final ObjectSipService objectSipService;
    private final UtilsBase utilsBase;
    private final UssiAssociationSyncService ussiAssociationSyncService;
    private final SipUssiAssignmentValidator sipUssiAssignmentValidator;

    /**
     * Retrieves all SIP gateways excluding those marked as deleted.
     *
     * @return an {@link ApiResponse} containing the list of available SIP gateways, or an error response if the operation fails
     */
    public ApiResponse getSipGateways() {
        try {
            List<SipGateways> result = sipGatewaysRepository.findByEnabledNot(Constants.DELETED_ENABLED_STATUS);
            return ResponseMapping.successMessage("Sip gateways list success.", result.stream().map(sipGatewaysMapper::toDTO).toList());
        } catch (Exception e) {
            log.error("Sip gateways get list error: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Sip gateways list error", e);
        }
    }

    /**
     * Retrieves a SIP gateway by its network identifier.
     *
     * @param networkId the unique network identifier of the SIP gateway
     * @return an {@link ApiResponse} containing the SIP gateway data if found,
     *         or an error response if the gateway does not exist
     */
    public ApiResponse getSipGatewaysByNetworkId(int networkId) {
        try {
            SipGateways gateways = sipGatewaysRepository.findByNetworkId(networkId);
            if (Objects.isNull(gateways)) {
                return ResponseMapping.errorMessageNoFound("Sip gateways with network_id= " + networkId + " was not found.");
            }
            return ResponseMapping.successMessage("Sip gateways get success.", sipGatewaysMapper.toDTO(gateways));
        } catch (Exception e) {
            log.error("Sip gateways get error networkId={}: {}", networkId, e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Sip gateways get error", e);
        }
    }

    /**
     * Creates a new SIP gateway.
     *
     * @param dto the SIP gateway data to be created
     * @return an {@link ApiResponse} containing the created SIP gateway,
     *         or an error response if the operation fails
     */
    @Transactional
    public ApiResponse create(SipGatewaysDTO dto) {
        try {
            validateCreate(dto);
            dto.setMessagesPerSecond();

            SipGateways entity = sipGatewaysMapper.toEntity(dto);
            entity.setNetworkId(seqGateway.getNextNetworkIdSequenceValue("GW"));

            entity.setProtocol("SIP");
            entity.setStatus(Constants.DEFAULT_STATUS);
            entity.setEnabled(Constants.DISABLED);
            ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(entity.getRoutingUssiTrafficSs7GatewayId(), entity.getNetworkId());
            SipGateways saved = sipGatewaysRepository.save(entity);
            return ResponseMapping.successMessage("Sip gateways created successful.", sipGatewaysMapper.toDTO(saved));
        } catch (Exception e) {
            log.error("Sip gateways create error: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Sip gateways create error", e);
        }
    }

    /**
     * Updates an existing SIP gateway identified by its network id.
     *
     * @param networkId the network identifier of the SIP gateway to update
     * @param dto the new SIP gateway data
     * @return an {@link ApiResponse} containing the updated SIP gateway,
     *         or an error response if the operation fails
     */
    public ApiResponse update(int networkId, SipGatewaysDTO dto) {
        try {
            SipGateways current = sipGatewaysRepository.findByNetworkId(networkId);
            if (Objects.isNull(current)) {
                return ResponseMapping.errorMessageNoFound("Sip gateways with network_id= " + networkId + " was not found.");
            }

            if (current.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
                return ResponseMapping.errorMessage("Illegal exception it is not possible to modify a deleted sip gateways.");
            }

            int previousEnabled = current.getEnabled();
            Integer previousUssiSs7GatewayId = current.getRoutingUssiTrafficSs7GatewayId();
            if (previousEnabled == Constants.ENABLED && dto.getEnabled() == Constants.ENABLED) {
                return ResponseMapping.errorMessage("Sip gateways is in STARTED state. Stop it before editing configuration.");
            }
            dto.setNetworkId(networkId);
            dto.setStatus(utilsBase.findStatusByEnabled(dto.getEnabled()));
            dto.setMessagesPerSecond();
            validateUpdate(dto, previousUssiSs7GatewayId);
            ussiAssociationSyncService.releasePreviousStoppedOwnerIfNeeded(dto.getRoutingUssiTrafficSs7GatewayId(), networkId);
            sipGatewaysMapper.updateEntityFromDTO(dto, current);
            SipGateways saved = sipGatewaysRepository.save(current);
            ussiAssociationSyncService.syncRedisAfterAssignmentUpdate(previousUssiSs7GatewayId, saved.getRoutingUssiTrafficSs7GatewayId());

            manageRedisAndSocketAction(saved, previousEnabled);

            return ResponseMapping.successMessage("Sip gateways updated successful.", sipGatewaysMapper.toDTO(saved));
        } catch (Exception e) {
            log.error("Sip gateways update error networkId={}: {}", networkId, e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Sip gateways update error", e);
        }
    }

    private void validateCreate(SipGatewaysDTO dto) {
        validateRouting(dto,new UssiAssignmentValidationContext(null, dto.getRoutingUssiTrafficSs7GatewayId(), null));
        if (sipGatewaysRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("sip name already exists.");
        }

        boolean existsSipGateway = existsSipGateway(
                new ExistSipGatewayValidatorParams(
                        0,
                        dto.getIpAddress(),
                        dto.getPort(),
                        dto.getTransport(),
                        "create",
                        Constants.DELETED_ENABLED_STATUS
                )
        );

        if (existsSipGateway) {
            throw new IllegalArgumentException(DUPLICATE_SIP_GATEWAY_MESSAGE);
        }
    }

    private void validateUpdate(SipGatewaysDTO dto, Integer currentAssignedSs7NetworkId) {
        validateRouting(dto, new UssiAssignmentValidationContext(currentAssignedSs7NetworkId, dto.getRoutingUssiTrafficSs7GatewayId(), dto.getNetworkId()));
        if (dto.getName() != null && sipGatewaysRepository.existsByNameAndNetworkIdNot(dto.getName(), dto.getNetworkId())) {
            throw new IllegalArgumentException("sip name already exists.");
        }

        boolean existsSipGateway = existsSipGateway(
                new ExistSipGatewayValidatorParams(
                        dto.getNetworkId(),
                        dto.getIpAddress(),
                        dto.getPort(),
                        dto.getTransport(),
                        "update",
                        Constants.DELETED_ENABLED_STATUS
                )
        );

        if (existsSipGateway) {
            throw new IllegalArgumentException(DUPLICATE_SIP_GATEWAY_MESSAGE);
        }
    }

    /**
     * Validates routing configuration rules and target assignments,
     * including the USSI assignment rules.
     *
     * @param dto the SIP gateway data containing the routing configuration
     * @param context the context required to validate USSI SS7 assignment rules
     */
    private void validateRouting(SipGatewaysDTO dto, UssiAssignmentValidationContext context) {
        validateRoutingFlags(dto);
        if (dto.isRoutingEnableSs7()) {
            validateSs7Targets(dto, context);
            return;
        }

        if (dto.isRoutingEnableDiameter()) {
            validateDiameterTargets(dto);
        }
    }

    private void validateRoutingFlags(SipGatewaysDTO dto) {
        boolean ss7 = dto.isRoutingEnableSs7();
        boolean dia = dto.isRoutingEnableDiameter();

        boolean hasAnyTarget =
                dto.getRoutingRegistrationTrafficSs7GatewayId() != null
                        || dto.getRoutingRegistrationTrafficDiameterGatewayId() != null
                        || dto.getRoutingUssiTrafficSs7GatewayId() != null;

        if (ss7 && dia) {
            throw new IllegalArgumentException("Only one routing flag can be active at a time: enable_ss7 OR enable_diameter.");
        }

        if (hasAnyTarget && ss7 == dia) {
            throw new IllegalArgumentException("Routing targets require exactly one routing flag enabled (SS7 or Diameter).");
        }
    }

    private void validateSs7Targets(SipGatewaysDTO dto, UssiAssignmentValidationContext context ) {
        if (dto.getRoutingRegistrationTrafficDiameterGatewayId() != null) {
            throw new IllegalArgumentException("registration_traffic_diameter is not allowed when SS7 routing is enabled.");
        }
        sipUssiAssignmentValidator.validate(context);
    }

    private void validateDiameterTargets(SipGatewaysDTO dto) {
        if (Objects.nonNull(dto.getRoutingRegistrationTrafficSs7GatewayId()) || Objects.nonNull( dto.getRoutingUssiTrafficSs7GatewayId())) {
            throw new IllegalArgumentException("SS7 routing targets are not allowed when Diameter routing is enabled.");
        }

        if (Objects.isNull( dto.getRoutingRegistrationTrafficDiameterGatewayId())) {
            throw new IllegalArgumentException("registration_traffic_diameter is required when Diameter routing is enabled.");
        }

    }

    private void manageRedisAndSocketAction(SipGateways saved, int previousEnabled) throws Exception {
        try {
            boolean enabledChanged = saved.getEnabled() != previousEnabled;
            if (enabledChanged) {
                if (saved.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
                    utilsBase.removeInRedis(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME, Integer.toString(saved.getNetworkId()));
                    utilsBase.sendNotificationSocket(Constants.DELETE_SIP_GATEWAYS_ENDPOINT, Integer.toString(saved.getNetworkId()));
                    return;
                }
                objectSipService.updateOrCreateJsonInRedis(saved.getNetworkId());

                String endpoint = (saved.getEnabled() == Constants.ENABLED)
                        ? Constants.CONNECT_SIP_GATEWAYS_ENDPOINT
                        : Constants.STOP_SIP_GATEWAYS_ENDPOINT;

                utilsBase.sendNotificationSocket(endpoint, Integer.toString(saved.getNetworkId()));
            }

        } catch (Exception e) {
            saved.setEnabled(previousEnabled);
            sipGatewaysRepository.save(saved);
            throw e;
        }
    }

    public boolean existsSipGateway(ExistSipGatewayValidatorParams params) {
        if (Objects.isNull(params.enabled) || params.enabled.length == 0 || Objects.isNull(params.action)) {
            return false;
        }

        try {
            for (Integer enabled : params.enabled) {
                boolean exists = switch (params.action) {
                    case "create" ->
                            sipGatewaysRepository.existsByIpAddressIgnoreCaseAndPortAndEnabledNot(
                                    params.ipAddress,
                                    params.port,
                                    enabled
                            );
                    case "update" ->
                            sipGatewaysRepository.existsByIpAddressIgnoreCaseAndPortAndEnabledNotAndNetworkIdNot(
                                    params.ipAddress,
                                    params.port,
                                    enabled,
                                    params.networkId
                            );
                    default -> false;
                };

                if (exists) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("An error occurred while checking SIP gateway existence with ipAddress: {}, port: {}, transport: {}, enabled values: {}", params.ipAddress, params.port, params.transport, Arrays.toString(params.enabled), e);
            return false;
        }
    }
    public record ExistSipGatewayValidatorParams(
            int networkId,
            String ipAddress,
            int port,
            String transport,
            String action,
            Integer... enabled
    ) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (Objects.isNull(o) || getClass() != o.getClass()) return false;
            ExistSipGatewayValidatorParams that = (ExistSipGatewayValidatorParams) o;
            return networkId == that.networkId
                    && port == that.port
                    && enabled.length == that.enabled.length
                    && Objects.equals(ipAddress, that.ipAddress)
                    && Objects.equals(transport, that.transport)
                    && Objects.equals(action, that.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(networkId, ipAddress, port, transport, action, Arrays.hashCode(enabled));
        }

        @Override
        public String toString() {return Converter.valueAsString(this);}
    }

    public record UssiAssignmentValidationContext(
            Integer currentAssignedSs7NetworkId,
            Integer targetSs7NetworkId,
            Integer currentSipNetworkId
    ) {
    }
}
