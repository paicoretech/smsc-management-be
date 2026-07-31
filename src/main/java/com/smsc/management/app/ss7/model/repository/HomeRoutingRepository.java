package com.smsc.management.app.ss7.model.repository;


import com.smsc.management.app.ss7.model.entity.HomeRouting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeRoutingRepository extends JpaRepository<HomeRouting, Integer> {
    HomeRouting findById(int id);

    HomeRouting findByNetworkId(int networkId);

    HomeRouting findByExternalId(String externalId);
}
