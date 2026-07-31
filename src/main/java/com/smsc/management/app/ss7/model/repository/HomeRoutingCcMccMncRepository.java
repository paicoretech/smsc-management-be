package com.smsc.management.app.ss7.model.repository;

import com.smsc.management.app.ss7.model.entity.HomeRoutingCcMccMnc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HomeRoutingCcMccMncRepository extends JpaRepository<HomeRoutingCcMccMnc, Long> {
    HomeRoutingCcMccMnc findById(long id);

    List<HomeRoutingCcMccMnc> findBySs7HomeRoutingId(int ss7HomeRoutingId);

    @Query("SELECT e FROM HomeRoutingCcMccMnc e JOIN e.ss7HomeRouting hr WHERE hr.networkId = :networkId")
    List<HomeRoutingCcMccMnc> findByNetworkId(@Param("networkId") int networkId);
}
