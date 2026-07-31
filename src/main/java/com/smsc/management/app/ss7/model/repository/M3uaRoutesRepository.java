package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.model.entity.M3uaRoutes;

public interface M3uaRoutesRepository extends JpaRepository<M3uaRoutes, Integer> {
	M3uaRoutes findById(int id);
	
	@Query("SELECT DISTINCT new com.smsc.management.app.ss7.dto.M3uaRoutesDTO(mr.id, mr.originationPointCode, mr.destinationPointCode, mr.serviceIndicator, mr.trafficModeId, mr.traficModeEntity.name) " +
	           "FROM M3uaAppServersRoutes masr " +
	           "JOIN masr.m3uaRoutes mr " +
	           "WHERE masr.applicationServerId IN :m3uaAppServers " +
	           "ORDER BY mr.id")
	List<M3uaRoutesDTO> fetchM3uaRoutes(@Param("m3uaAppServers") List<Integer> m3uaAppServers);
}
