package com.smsc.management.app.settings.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.settings.model.entity.GeneralSettingsSmppHttp;

public interface GeneralSettingsSmppHttpRepository extends JpaRepository<GeneralSettingsSmppHttp, Integer> {
	GeneralSettingsSmppHttp findById(int id);
}
