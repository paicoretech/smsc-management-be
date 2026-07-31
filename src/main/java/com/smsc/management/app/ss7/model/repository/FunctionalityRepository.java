package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.Functionality;

public interface FunctionalityRepository extends JpaRepository<Functionality, String> {

}
