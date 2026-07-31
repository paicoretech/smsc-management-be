package com.smsc.management.app.sip.service;

import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UssiAssociationSyncService {

    private final SipGatewaysRepository sipGatewaysRepository;
    private final Ss7GatewaysRepository ss7GatewaysRepository;
    private final ObjectSs7Service objectSs7Service;

    public void releasePreviousStoppedOwnerIfNeeded(Integer newSs7NetworkId, int currentSipNetworkId) {
        if (Objects.isNull( newSs7NetworkId)) {
            return;
        }

        sipGatewaysRepository.findFirstByRoutingUssiTrafficSs7GatewayIdAndNetworkIdNot(newSs7NetworkId, currentSipNetworkId)
                .ifPresent(previousSipOwner -> {
                    if (previousSipOwner.getEnabled() == Constants.ENABLED) {
                        throw new IllegalArgumentException("The selected SS7 gateway is already assigned to a running SIP gateway. Stop that SIP gateway before reassigning it.");
                    }
                    previousSipOwner.setRoutingUssiTrafficSs7GatewayId(null);
                    sipGatewaysRepository.save(previousSipOwner);
                });
    }

    public void syncRedisAfterAssignmentUpdate(Integer previousSs7NetworkId, Integer newSs7NetworkId) throws Exception {
        if (Objects.equals(previousSs7NetworkId, newSs7NetworkId)) {
            return;
        }

        refreshSs7SipAssociation(previousSs7NetworkId);
        refreshSs7SipAssociation(newSs7NetworkId);
    }

    private void refreshSs7SipAssociation(Integer ss7NetworkId) throws Exception {
        if (Objects.isNull(ss7NetworkId)) {
            return;
        }
        Ss7Gateways ss7Gateway = ss7GatewaysRepository.findByNetworkId(ss7NetworkId);
        if (!Objects.nonNull(ss7Gateway) || ss7Gateway.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
            return;
        }

        if (ss7Gateway.getEnabled() == Constants.ENABLED) {
            objectSs7Service.updateSs7SettingsInRedis(ss7NetworkId);
            objectSs7Service.sendSs7SettingsUpdateNotification(ss7NetworkId);
        }
    }
}