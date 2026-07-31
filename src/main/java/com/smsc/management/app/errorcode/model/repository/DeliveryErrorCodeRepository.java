package com.smsc.management.app.errorcode.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.errorcode.model.entity.DeliveryErrorCode;


public interface DeliveryErrorCodeRepository extends JpaRepository<DeliveryErrorCode, Integer> {
	DeliveryErrorCode findById(int id);
}
