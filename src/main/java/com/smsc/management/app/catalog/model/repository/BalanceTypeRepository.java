package com.smsc.management.app.catalog.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.catalog.model.entity.BalanceType;

public interface BalanceTypeRepository extends CrudRepository<BalanceType, String> {
	List<BalanceType> findAll();
}
