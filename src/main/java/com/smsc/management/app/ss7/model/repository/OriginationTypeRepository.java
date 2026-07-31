package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.OriginationType;

public interface OriginationTypeRepository extends JpaRepository<OriginationType, Integer> {

}
