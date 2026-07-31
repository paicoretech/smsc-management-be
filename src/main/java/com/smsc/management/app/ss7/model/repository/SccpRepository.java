package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.Sccp;

public interface SccpRepository extends JpaRepository<Sccp, Integer> {
    Sccp findById(int id);

    Sccp findByNetworkId(int networkId);

    Sccp findByExternalId(String externalId);
}
