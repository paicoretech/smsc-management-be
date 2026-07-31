package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterRealm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiameterRealmRepository extends JpaRepository<DiameterRealm, Integer> {
    @Query("""
            SELECT dg.id FROM DiameterRealm dr
            JOIN DiameterGateway dg ON dr.diameterGateway.id = dg.id
            WHERE dr.id = :peerId
            """)
    Integer findDiameterGatewayIdByRealmId(Integer peerId);
}
