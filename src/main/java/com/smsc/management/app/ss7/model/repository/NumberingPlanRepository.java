package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.ss7.model.entity.NumberingPlan;

public interface NumberingPlanRepository extends CrudRepository<NumberingPlan, Integer> {

}
