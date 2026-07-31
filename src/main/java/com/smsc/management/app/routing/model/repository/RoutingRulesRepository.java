package com.smsc.management.app.routing.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.model.entity.RoutingRules;

public interface RoutingRulesRepository extends JpaRepository<RoutingRules, Integer> {

    RoutingRules findById(int id);

    List<RoutingRules> findByOriginNetworkId(int networkId);

    @Query("SELECT new com.smsc.management.app.routing.dto.RoutingRulesDTO(" +
            "rr.id, rr.originNetworkId, " +
            "CASE WHEN gw.name IS NOT NULL THEN gw.name " +
            "WHEN gw2.name IS NOT NULL THEN gw2.name " +
            "WHEN dg.name IS NOT NULL THEN dg.name " +
            "WHEN ss.name IS NOT NULL THEN ss.name " +
            "ELSE sp.name end as name, " +
            "rr.regexSourceAddr, rr.regexSourceAddrTon," +
            "rr.regexSourceAddrNpi, rr.regexDestinationAddr, rr.regexDestAddrTon, rr.regexDestAddrNpi, " +
            "rr.regexImsiDigitsMask, rr.regexNetworkNodeNumber, rr.regexCallingPartyAddress, rr.isSriResponse, rr.regexShortMessage, " +
            "rr.newSourceAddr, rr.newSourceAddrTon, rr.newSourceAddrNpi, rr.newDestinationAddr," +
            "rr.newDestAddrTon, rr.newDestAddrNpi, rr.addSourceAddrPrefix, rr.addDestAddrPrefix, rr.removeSourceAddrPrefix, " +
            "rr.removeDestAddrPrefix, rr.newGtSccpAddr, rr.dropMapSri, rr.networkIdToMapSri, rr.networkIdToPermanentFailure, " +
            "rr.dropTempFailure, rr.networkIdTempFailure, rr.checkSriResponse, rr.newShortMessage, rr.diameterCharging, rr.applyForRefund) " +
            "FROM RoutingRules rr " +
            "LEFT JOIN Gateways gw ON rr.originNetworkId = gw.networkId " +
            "LEFT JOIN Ss7Gateways gw2 ON rr.originNetworkId = gw2.networkId " +
            "LEFT JOIN ServiceProvider sp ON rr.originNetworkId = sp.networkId " +
            "LEFT JOIN DiameterGateway dg ON rr.originNetworkId = dg.networkId " +
            "LEFT JOIN SipGateways ss ON rr.originNetworkId = ss.networkId " +
            "ORDER BY rr.id")
    List<RoutingRulesDTO> findByRoutingRulesList();

    @Query("SELECT NEW com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO(f.networkId, f.name, f.type, f.protocol) " +
            "FROM ( " +
            "   SELECT g.networkId AS networkId, g.name AS name, 'gw' AS type, g.protocol as protocol FROM Gateways g WHERE g.enabled != 2 " +
            "   UNION " +
            "   SELECT sg.networkId AS networkId, sg.name AS name, 'gw' AS type, sg.protocol as protocol FROM Ss7Gateways sg WHERE sg.enabled != 2 and sg.allowedTraffic = true" +
            "   UNION " +
            "   SELECT sp.networkId AS networkId, sp.name AS name, 'sp' AS type, sp.protocol as protocol  FROM ServiceProvider sp WHERE sp.enabled != 2 " +
            "   UNION " +
            "   SELECT dg.networkId AS networkId, dg.name AS name, 'gw' AS type, dg.protocol as protocol FROM DiameterGateway dg WHERE dg.type = 'GATEWAY' and dg.allowedTraffic = true" +
            "   UNION " +
            "   SELECT st.networkId, st.name AS name, 'gw' AS type, st.protocol FROM SipGateways st WHERE st.enabled != 2 " +
            ") AS f " +
            "ORDER BY f.networkId")
    List<NetworksToRoutingRulesDTO> findGatewayNamesAndIds();

    @Query("SELECT f.protocol " +
            "FROM ( " +
            "   SELECT g.networkId AS networkId, g.protocol AS protocol FROM Gateways g " +
            "   UNION " +
            "   SELECT sg.networkId AS networkId, sg.protocol AS protocol FROM Ss7Gateways sg " +
            "   UNION " +
            "   SELECT sp.networkId AS networkId, sp.protocol AS protocol FROM ServiceProvider sp " +
            "   UNION " +
            "   SELECT dg.networkId AS networkId, dg.protocol AS protocol FROM DiameterGateway dg " +
            "   UNION " +
            "   SELECT st.networkId AS networkId, st.protocol AS protocol FROM SipGateways st " +
            ") AS f " +
            "WHERE f.networkId = :originNetworkId "
    )
    String findOriginProtocol(@Param("originNetworkId") int originNetworkId);

    @Query("SELECT NEW com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO(f.networkId, f.name, f.type, f.protocol) " +
            "FROM ( " +
            "   SELECT g.networkId AS networkId, g.name AS name, 'gw' AS type, g.protocol as protocol FROM Gateways g" +
            "   UNION " +
            "   SELECT sg.networkId AS networkId, sg.name AS name, 'gw' AS type, sg.protocol as protocol FROM Ss7Gateways sg" +
            "   UNION " +
            "   SELECT sp.networkId AS networkId, sp.name AS name, 'sp' AS type, sp.protocol as protocol  FROM ServiceProvider sp" +
            "   UNION " +
            "   SELECT dg.networkId AS networkId, dg.name AS name, 'gw' AS type, dg.protocol as protocol  FROM DiameterGateway dg" +
            "   UNION " +
            "   SELECT st.networkId AS networkId, st.name AS name, 'gw' AS type, st.protocol as protocol  FROM SipGateways st" +
            ") AS f " +
            "WHERE f.networkId IN (:networkIds) " +
            "ORDER BY f.networkId")
    List<NetworksToRoutingRulesDTO> findByNetworkIds(@Param("networkIds") List<Integer> networkIds);
}
