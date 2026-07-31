package com.smsc.management.app.routing.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.routing.dto.RedisRoutingRulesDestinationDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO;
import com.smsc.management.app.routing.model.entity.RoutingRulesDestination;
import java.util.List;

public interface RoutingRulesDestinationRepository extends JpaRepository<RoutingRulesDestination, Integer> {

	List<RoutingRulesDestination> findByRoutingRulesId(int routingRulesId);
	
	RoutingRulesDestination findById(int id);

	@Query("SELECT new com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO(rd.id, rd.priority, rd.networkId, " +
			"CASE WHEN gw.name IS NOT NULL THEN gw.name " +
			"WHEN gw2.name IS NOT NULL THEN gw2.name " +
            "WHEN dg.name IS NOT NULL THEN dg.name " +
			"WHEN st.name IS NOT NULL THEN st.name " +
			"ELSE sp.name end as name, " +
			"rd.routingRulesId, 0 as action, rd.networkType) " +
			"FROM RoutingRulesDestination rd " +
			"LEFT JOIN Gateways gw ON rd.networkId = gw.networkId " +
			"LEFT JOIN Ss7Gateways gw2 ON rd.networkId = gw2.networkId " +
			"LEFT JOIN ServiceProvider sp ON rd.networkId = sp.networkId " +
            "LEFT JOIN DiameterGateway dg ON rd.networkId = dg.networkId " +
			"LEFT JOIN SipGateways st ON rd.networkId = st.networkId " +
			"WHERE rd.routingRulesId = :routingRulesId " +
			"ORDER BY rd.priority")
	List<RoutingRulesDestinationDTO> findByRoutingRulesIdList(@Param("routingRulesId") int routingRulesId);
	
	@Query("SELECT new com.smsc.management.app.routing.dto.RedisRoutingRulesDestinationDTO(rd.priority, rd.networkId, " +
			"CASE WHEN gw.protocol IS NOT NULL THEN gw.protocol " +
			"WHEN gw2.protocol IS NOT NULL THEN gw2.protocol " +
            "WHEN dg.protocol IS NOT NULL THEN dg.protocol " +
			"WHEN st.protocol IS NOT NULL THEN st.protocol " +
			"ELSE sp.protocol end as destProtocol, rd.networkType) " +
			"FROM RoutingRulesDestination rd " +
			"LEFT JOIN Gateways gw ON rd.networkId = gw.networkId " +
			"LEFT JOIN Ss7Gateways gw2 ON rd.networkId = gw2.networkId " +
			"LEFT JOIN ServiceProvider sp ON rd.networkId = sp.networkId " +
            "LEFT JOIN DiameterGateway dg ON rd.networkId = dg.networkId " +
			"LEFT JOIN SipGateways st ON rd.networkId = st.networkId " +
			"WHERE rd.routingRulesId=:routingRulesId " +
			"ORDER BY rd.priority")
	List<RedisRoutingRulesDestinationDTO> findByRoutingRulesIdDTO(@Param("routingRulesId") int routingRulesId);
}
