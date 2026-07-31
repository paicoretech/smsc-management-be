package com.smsc.management.app.mno.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.mno.model.entity.OperatorMno;

public interface OperatorMnoRepository extends JpaRepository<OperatorMno, Integer> {
	OperatorMno findById(int id);
	List<OperatorMno> findByEnabledTrue();
	OperatorMno findByIdAndEnabledTrue(int id);
}
