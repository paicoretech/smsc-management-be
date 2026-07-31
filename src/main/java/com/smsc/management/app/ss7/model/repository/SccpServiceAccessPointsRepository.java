package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;

public interface SccpServiceAccessPointsRepository extends JpaRepository<SccpServiceAccessPoints, Integer> {
	SccpServiceAccessPoints findById(int id);
	List<SccpServiceAccessPoints> findBySs7SccpId(int sccpId);
}
