package com.smsc.management.app.catalog.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.catalog.model.entity.NpiCatalog;

public interface NpiCatalogRepository extends CrudRepository<NpiCatalog, Integer> {
	List<NpiCatalog> findByIdNot(int id);
}
