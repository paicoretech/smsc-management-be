package com.smsc.management.app.ss7.model.repository;

import com.smsc.management.app.ss7.model.entity.Map;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MapRepository extends JpaRepository<Map, Integer> {
    Map findById(int id);

    Map findByNetworkId(int networkId);

    Map findByExternalId(String externalId);
}
