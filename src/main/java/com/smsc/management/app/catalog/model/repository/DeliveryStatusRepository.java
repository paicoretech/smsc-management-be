package com.smsc.management.app.catalog.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.catalog.model.entity.DeliveryStatus;

public interface DeliveryStatusRepository extends CrudRepository<DeliveryStatus, String> {
	DeliveryStatus findByName(String name);
	List<DeliveryStatus> findAll();
}
