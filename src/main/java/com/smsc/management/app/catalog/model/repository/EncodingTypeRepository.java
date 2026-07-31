package com.smsc.management.app.catalog.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.catalog.model.entity.EncodingType;

public interface EncodingTypeRepository extends JpaRepository<EncodingType, Integer> {

}
