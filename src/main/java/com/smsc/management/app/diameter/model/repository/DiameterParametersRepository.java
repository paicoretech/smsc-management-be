package com.smsc.management.app.diameter.model.repository;

import com.smsc.management.app.diameter.model.entity.DiameterParameters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiameterParametersRepository extends JpaRepository<DiameterParameters, Integer> {
}
