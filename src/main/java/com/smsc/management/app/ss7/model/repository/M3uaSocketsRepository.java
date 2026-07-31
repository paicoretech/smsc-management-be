package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.model.entity.M3uaSockets;

public interface M3uaSocketsRepository extends JpaRepository<M3uaSockets, Integer> {
	M3uaSockets findById(int id);
	List<M3uaSockets> findBySs7M3uaId(int m3uaId);
	
	@Query("SELECT sg.networkId " +
		"FROM M3uaSockets ms " +
		"JOIN ms.ss7Mu3a m3 " +
		"JOIN m3.ss7Gateway sg " +
		"WHERE ms.id = :m3uaSocketId " +
		"ORDER BY ms.id")
	int findNetworkIdById(@Param("m3uaSocketId") int m3uaSocketId);
}
