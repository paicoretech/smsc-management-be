package com.smsc.management.app.errorcode.model.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.errorcode.dto.RedisErrorCodeMappingDTO;
import com.smsc.management.app.errorcode.model.entity.ErrorCodeMapping;

import java.util.List;
import java.util.Map;

public interface ErrorCodeMappingRepository extends CrudRepository<ErrorCodeMapping, Integer> {
    ErrorCodeMapping findById(int id);

    @Query("SELECT NEW map(ecm.id as errorCodeMappingId, om.id as operatorMnoId, om.name as operatorMno," +
            "ec.id as errorCodeId, ec.code as errorCode," +
            "sec.id as deliveryErrorCodeId, sec.code as deliveryErrorCode, " +
            "ecm.deliveryStatusId as deliveryStatus) " +
            "FROM ErrorCodeMapping ecm " +
            "JOIN ecm.errorCode ec " +
            "JOIN ecm.deliveryErrorCode sec " +
            "JOIN ec.operatorMno om " +
            "ORDER BY ecm.id")
    List<Map<String, Object>> fetchErrorCodeMappings();

    @Query("SELECT new com.smsc.management.app.errorcode.dto.RedisErrorCodeMappingDTO(" +
            "CAST(ec.code AS int) as errorCode, CAST(sec.code AS int) as deliveryErrorCode, ecm.deliveryStatusId as deliveryStatus) " +
            "FROM ErrorCodeMapping ecm " +
            "JOIN ecm.errorCode ec " +
            "JOIN ecm.deliveryErrorCode sec " +
            "WHERE ec.mnoId=:mnoId " +
            "ORDER BY ec.id")
    List<RedisErrorCodeMappingDTO> findByMnoId(@Param("mnoId") int mnoId);

}
