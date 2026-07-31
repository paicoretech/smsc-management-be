package com.smsc.management.app.catalog.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.catalog.model.entity.TonCatalog;

public interface TonCatalogRepository extends CrudRepository<TonCatalog, Integer> {
	List<TonCatalog> findByIdNot(int id);
}
