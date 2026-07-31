package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;

public interface SccpRemoteResourcesRepository extends JpaRepository<SccpRemoteResources, Integer> {
	SccpRemoteResources findById(int id);
	List<SccpRemoteResources> findBySs7SccpId(int sccpId);
}
