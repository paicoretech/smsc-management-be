package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterPeer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiameterPeerRepository extends JpaRepository<DiameterPeer, Integer> {
    @Query("""
            SELECT dg.id FROM DiameterPeer dp
            JOIN DiameterGateway dg ON dp.diameterGateway.id = dg.id
            WHERE dp.id = ?1
            """)
    Integer findDiameterGatewayIdByPeerId(@Param("peerId") Integer peerId);

    @Query("""
            SELECT dp FROM DiameterPeer dp
            WHERE dp.id = ?1
            """)
    DiameterPeer findDiameterPeerById(@Param("peerId") Integer peerId);
}
