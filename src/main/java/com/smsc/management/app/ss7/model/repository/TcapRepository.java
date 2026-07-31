package com.smsc.management.app.ss7.model.repository;

import com.smsc.management.app.ss7.model.entity.Tcap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TcapRepository extends JpaRepository<Tcap, Integer> {
    Tcap findById(int id);

    Tcap findByNetworkId(int networkId);

    Tcap findByExternalId(String externalId);
}
