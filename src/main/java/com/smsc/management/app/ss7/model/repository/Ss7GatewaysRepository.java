package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.model.entity.Ss7Gateways;

public interface Ss7GatewaysRepository extends JpaRepository<Ss7Gateways, Integer> {
	Ss7Gateways findById(int id);
	List<Ss7Gateways> findByEnabledNotAndAllowedTraffic(int enabled, boolean allowedTraffic);

	@Query("""
			select sg
			from Ss7Gateways sg
			where sg.enabled <> :enabled
			  and (sg.hssUpdateEnabled or sg.allowedUssi)
			""")
	List<Ss7Gateways> findAllForIpSmGw(@Param("enabled") int enabled);

	List<Ss7Gateways> findByMnoIdAndEnabledNot(int mnoId, int enabled);
	
	Ss7Gateways findByNetworkId(int id);

	Ss7Gateways findByExternalId(String externalId);

	boolean existsByNetworkIdAndEnabled(int networkId, int enabled);

	List<Ss7Gateways> findByEnabledNotAndAllowedUssiTrue(int enabled);

	List<Ss7Gateways> findByEnabledNotAndAllowedUssiTrueAndHomeRoutingFalse(int enabled);
}
