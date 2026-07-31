package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterLocalPeer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiameterLocalPeerRepository extends JpaRepository<DiameterLocalPeer, Integer> {
}
