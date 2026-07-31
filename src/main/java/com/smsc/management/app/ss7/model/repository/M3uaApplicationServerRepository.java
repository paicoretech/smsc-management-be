package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.model.entity.M3uaApplicationServer;

public interface M3uaApplicationServerRepository extends JpaRepository<M3uaApplicationServer, Integer> {
	M3uaApplicationServer findById(int id);
	
	@Query("SELECT DISTINCT new com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO(mas.id, mas.name, mas.state, mas.functionality, mas.exchange, mas.routingContext, mas.networkAppearance, mas.trafficModeId, mas.minimumAspForLoadshare) " +
	           "FROM M3uaAssAppServers msa " +
	           "JOIN msa.m3uaAppServer mas " +
	           "JOIN msa.mu3aAssociations ass " +
	           "JOIN ass.mu3aSocket ms " +
	           "WHERE ms.ss7M3uaId = :m3uaId " +
	           "ORDER BY mas.id")
	List<M3uaApplicationServerDTO> fetchM3uaAppServer(@Param("m3uaId") int m3uaId);
	
	@Query("SELECT DISTINCT mas.id " +
	           "FROM M3uaAssAppServers msa " +
	           "JOIN msa.m3uaAppServer mas " +
	           "JOIN msa.mu3aAssociations ass " +
	           "JOIN ass.mu3aSocket ms " +
	           "WHERE ms.ss7M3uaId = :m3uaId " +
	           "ORDER BY mas.id")
	List<Integer> fetchM3uaAppServerId(@Param("m3uaId") int m3uaId);
}
