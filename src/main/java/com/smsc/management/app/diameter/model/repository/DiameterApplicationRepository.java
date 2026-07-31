package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiameterApplicationRepository extends JpaRepository<DiameterApplication, Integer> {
}
