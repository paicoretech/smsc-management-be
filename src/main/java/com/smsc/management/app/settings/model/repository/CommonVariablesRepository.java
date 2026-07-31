package com.smsc.management.app.settings.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.settings.model.entity.CommonVariables;

public interface CommonVariablesRepository extends JpaRepository<CommonVariables, String> {
	CommonVariables findByKey(String key);
	
	List<CommonVariables> findAll();
	
	List<CommonVariables> findByRedisReplicatedTrue();
}
