package com.smsc.management.app.errorcode.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.smsc.management.app.errorcode.dto.ParseErrorCodeDTO;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;

public interface ErrorCodeRepository extends CrudRepository<ErrorCode, Integer> {
	ErrorCode findById(int id);
	List<ErrorCode> findByMnoId(int mnoId);
	@Query("SELECT new com.smsc.management.app.errorcode.dto.ParseErrorCodeDTO(ecm.id, ecm.code, ecm.description, ecm.mnoId, ec.name)" +
	           "FROM ErrorCode ecm " +
	           "JOIN ecm.operatorMno ec " +
	           "ORDER BY ecm.id")
	List<ParseErrorCodeDTO> fetchErrorCode();
}
