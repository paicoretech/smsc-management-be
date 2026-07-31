package com.smsc.management.app.sip.model.repository;

import com.smsc.management.app.sip.model.entity.SipGateways;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SipGatewaysRepository extends JpaRepository<SipGateways, Integer> {

    SipGateways findByNetworkId(int networkId);

    SipGateways findByExternalId(String externalId);

    List<SipGateways> findByEnabledNot(int enabled);

    boolean existsByName(String sipName);

    boolean existsByNameAndNetworkIdNot(String sipName, int networkId);

    boolean existsByIpAddressIgnoreCaseAndPortAndEnabledNot(String ipAddress, int port, int enabled);

    boolean existsByIpAddressIgnoreCaseAndPortAndEnabledNotAndNetworkIdNot(String ipAddress, int port, int enabled, int networkId);

    Optional<SipGateways> findFirstByRoutingUssiTrafficSs7GatewayId(Integer ss7NetworkId);

    Optional<SipGateways> findFirstByRoutingUssiTrafficSs7GatewayIdAndNetworkIdNot(Integer ss7NetworkId, int networkId
    );

    boolean existsByRoutingUssiTrafficSs7GatewayId(Integer ss7NetworkId);
}
