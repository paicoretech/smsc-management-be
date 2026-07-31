package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.model.entity.M3uaAssAppServers;

public interface M3uaAssAppServersRepository extends JpaRepository<M3uaAssAppServers, Integer> {
	List<M3uaAssAppServers> findByApplicationServerId(int applicationServerId);
	
	@Query("SELECT ms.aspId " +
	           "FROM M3uaAssAppServers ms " +
	           "WHERE ms.applicationServerId = :m3uaAsId " +
	           "ORDER BY ms.id")
	List<Integer> fetchAssAppServers(@Param("m3uaAsId") int m3uaAsId);
	
	@Query("SELECT mat.aspName " +
	           "FROM M3uaAssAppServers ms " +
	           "JOIN ms.mu3aAssociations mat " +
	           "WHERE ms.applicationServerId = :m3uaAsId " +
	           "ORDER BY ms.id")
	List<String> fetchAspName(@Param("m3uaAsId") int m3uaAsId);
	
	@Query("SELECT DISTINCT mat.functionality " +
	           "FROM M3uaAssAppServers ms " +
	           "JOIN ms.m3uaAppServer mat " +
	           "WHERE ms.aspId = :m3uaAssId AND ms.applicationServerId != :m3uaAsId")
	String getAsFunctionality(@Param("m3uaAssId") int m3uaAssId, @Param("m3uaAsId") int m3uaAsId);
}
