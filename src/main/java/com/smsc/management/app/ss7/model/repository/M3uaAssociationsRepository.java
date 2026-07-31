package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.model.entity.M3uaAssociations;

public interface M3uaAssociationsRepository extends JpaRepository<M3uaAssociations, Integer> {
	M3uaAssociations findById(int id);
	List<M3uaAssociations> findByM3uaSocketId(int m3uaSocketId);
	
	@Query("SELECT new com.smsc.management.app.ss7.dto.M3uaAssociationsDTO(ma.id, ma.name, ma.enabled, ma.state, ma.peer, ma.peerPort, ma.m3uaHeartbeat, ma.m3uaSocketId, ma.aspName, ms.name) " +
	           "FROM M3uaAssociations ma " +
	           "JOIN ma.mu3aSocket ms " +
	           "WHERE ms.ss7M3uaId = :m3uaId " +
	           "ORDER BY ma.id")
	List<M3uaAssociationsDTO> fetchM3uaAssociations(@Param("m3uaId") int m3uaId);
	
	@Query("SELECT sg.networkId " +
			"FROM M3uaAssociations ma " +
	           "JOIN ma.mu3aSocket ms " +
	           "JOIN ms.ss7Mu3a m3 " +
	           "JOIN m3.ss7Gateway sg " +
	           "WHERE ma.id = :m3uaAssocId " +
	           "ORDER BY ma.id")
	int findNetworkIdById(@Param("m3uaAssocId") int m3uaAssocId);
}
