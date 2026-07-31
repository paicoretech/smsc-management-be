package com.smsc.management.app.settings.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.settings.model.entity.GeneralSmscRetry;

public interface GeneralSmscRetryRepository extends JpaRepository<GeneralSmscRetry, Integer> {
	GeneralSmscRetry findById(int id);
}
