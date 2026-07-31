package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.model.entity.M3uaAppServersRoutes;

public interface M3uaAppServersRouteRepository extends JpaRepository<M3uaAppServersRoutes, Integer> {
	List<M3uaAppServersRoutes> findByRouteId(int routeId);
	
	@Query("SELECT (ms.applicationServerId) " +
	           "FROM M3uaAppServersRoutes ms " +
	           "WHERE ms.routeId = :m3uaRouteId " +
	           "ORDER BY ms.id")
	List<Integer> fetchAppServersRoute(@Param("m3uaRouteId") int m3uaRouteId);
}
