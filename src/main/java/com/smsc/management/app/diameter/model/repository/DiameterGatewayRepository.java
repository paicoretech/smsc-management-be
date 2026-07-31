package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiameterGatewayRepository extends JpaRepository<DiameterGateway, Integer> {
    @Query("""
            SELECT dg FROM DiameterGateway dg
                  JOIN FETCH DiameterLocalPeer lp ON dg.id = lp.diameterGatewayId
                  JOIN FETCH DiameterParameters dparams ON dg.id = dparams.diameterGatewayId
                  LEFT JOIN FETCH DiameterRealm dr ON dg.id = dr.diameterGatewayId
                  LEFT JOIN FETCH DiameterPeer dpeer ON dg.id = dpeer.diameterGateway.id
                  LEFT JOIN FETCH DiameterApplication da ON dr.diameterGatewayId = da.id
                              AND da.diameterLocalPeerId = lp.id
                              AND da.diameterRealmId = dr.id
                  WHERE dg.id = :id
                  AND (dg.deleted = false OR dg.deleted IS NULL)            
            """)
    DiameterGateway findDiameterGatewayById(Integer id);

    @Query("""
            SELECT dg FROM DiameterGateway dg
            WHERE dg.deleted = false OR dg.deleted IS NULL            
            """)
    List<DiameterGateway> findAllDiameterGateways();

    @Query("""
            SELECT dg FROM DiameterGateway dg WHERE dg.type = 'OCS' AND dg.networkId IS NULL AND (dg.deleted = false OR dg.deleted IS NULL)
            """)
    DiameterGateway findOCSGateway();

    @Query("""
            SELECT dg FROM DiameterGateway dg WHERE dg.type = 'GATEWAY' AND dg.networkId IS NOT NULL AND (dg.deleted = false OR dg.deleted IS NULL)
            """)
    List<DiameterGateway> findGateways();

    @Query("""
            SELECT dg FROM DiameterGateway dg WHERE dg.type = 'GATEWAY' AND dg.networkId IS NOT NULL AND (dg.deleted = false OR dg.deleted IS NULL) AND dg.hssUpdateEnabled =true
            """)
    List<DiameterGateway> findGatewaysIpSmGw();

    @Query("""
            SELECT COUNT(dg) FROM DiameterGateway dg
            WHERE dg.networkId IS NULL
            AND (dg.deleted = false OR dg.deleted IS NULL)            
            """)
    int countDiameterGatewaysForCharging();
}
