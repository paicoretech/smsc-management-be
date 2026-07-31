package com.smsc.management.app.ss7.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.model.entity.SccpMtp3Destinations;
import java.util.List;


public interface SccpMtp3DestinationsRepository extends JpaRepository<SccpMtp3Destinations, Integer> {
	SccpMtp3Destinations findById(int id);
	List<SccpMtp3Destinations> findBySccpSapId(int sccpSapId);
	
	@Query("SELECT new com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO(smd.id, smd.name, smd.firstPointCode, smd.lastPointCode, smd.firstSls, smd.lastSls, smd.slsMask, smd.sccpSapId) " +
	           "FROM SccpMtp3Destinations smd " +
	           "JOIN smd.sccpSap ss " +
	           "WHERE ss.ss7SccpId = :sccpId " +
	           "ORDER BY smd.id")
	List<SccpMtp3DestinationsDTO> fetchMtp3Destinations(@Param("sccpId") int sccpId);
}
