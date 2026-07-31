package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.M3ua;

public interface M3uaRepository extends JpaRepository<M3ua, Integer> {
    M3ua findById(int id);

    M3ua findByNetworkId(int networkId);

    M3ua findByExternalId(String externalId);
}
