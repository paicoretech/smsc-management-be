package com.smsc.management.app.catalog.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.catalog.model.entity.BindStatuses;

public interface BindStatusesRepository extends CrudRepository<BindStatuses, String> {
	List<BindStatuses> findAll();
}
