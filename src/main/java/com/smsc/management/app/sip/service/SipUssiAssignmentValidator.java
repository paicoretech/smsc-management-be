package com.smsc.management.app.sip.service;

import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SipUssiAssignmentValidator {

    private final Ss7GatewaysRepository ss7GatewaysRepository;
    private final SipGatewaysRepository sipGatewaysRepository;

    public void validate(SipGatewaysService.UssiAssignmentValidationContext context) {
        validateCurrentAssignmentCanBeReleased(context.currentAssignedSs7NetworkId(), context.targetSs7NetworkId());
        validateTargetAssignment(context.targetSs7NetworkId(), context.currentSipNetworkId());
    }

    private void validateCurrentAssignmentCanBeReleased(Integer currentAssignedSs7NetworkId, Integer targetSs7NetworkId) {
        if (Objects.isNull(currentAssignedSs7NetworkId)) {
            return;
        }

        if (Objects.equals(currentAssignedSs7NetworkId, targetSs7NetworkId)) {
            return;
        }
        Ss7Gateways currentSs7Gateway = ss7GatewaysRepository.findByNetworkId(currentAssignedSs7NetworkId);
        if (Objects.isNull(currentSs7Gateway) || currentSs7Gateway.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
            return;
        }
        if (currentSs7Gateway.getEnabled() == Constants.ENABLED) {
            throw new IllegalArgumentException("The currently assigned SS7 gateway is running. Stop it before changing or removing the USSI association.");
        }
    }

    private void validateTargetAssignment(Integer targetSs7NetworkId, Integer currentSipNetworkId) {
        if (Objects.isNull( targetSs7NetworkId)) {
            return;
        }

        Ss7Gateways targetSs7Gateway = ss7GatewaysRepository.findByNetworkId(targetSs7NetworkId);

        if (!Objects.nonNull(targetSs7Gateway) || targetSs7Gateway.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
            throw new IllegalArgumentException("Selected SS7 gateway does not exist.");
        }

        if (!targetSs7Gateway.isAllowedUssi()) {
            throw new IllegalArgumentException("Selected SS7 gateway does not allow USSI traffic.");
        }

        if (targetSs7Gateway.isHomeRouting()) {
            throw new IllegalArgumentException("Selected SS7 gateway cannot be used for USSI because Home Routing is enabled.");
        }

        Optional<SipGateways> currentOwnerOpt = sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayId(targetSs7NetworkId);
        if (currentOwnerOpt.isEmpty()) {
            return;
        }
        SipGateways currentOwner = currentOwnerOpt.get();
        if (Objects.equals(currentOwner.getNetworkId(), currentSipNetworkId)) {
            return;
        }

        if (targetSs7Gateway.getEnabled() == Constants.ENABLED && currentOwner.getEnabled() == Constants.ENABLED) {
            throw new IllegalArgumentException("Selected SS7 gateway is running and cannot be assigned to a SIP gateway.");
        }

        if (currentOwner.getEnabled() == Constants.ENABLED) {
            throw new IllegalArgumentException("Selected SS7 gateway is already assigned to a running SIP gateway. Stop that SIP gateway before reassigning it.");
        }
    }
}