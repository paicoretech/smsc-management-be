package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SccpAddressesRepository extends JpaRepository<SccpAddresses, Integer> {
	SccpAddresses findById(int id);
	List<SccpAddresses> findBySs7SccpId(int sccpId);

	@Query("""
       SELECT new com.smsc.management.app.ss7.dto.SccpAddressesDTO(
           sa.id, sa.name, sa.addressIndicator, sa.pointCode, sa.subsystemNumber,
           sa.gtIndicator, sa.translationType, sa.numberingPlanId, np.name,
           sa.natureOfAddressId, na.name, sa.digits, sa.ss7SccpId)
       FROM SccpAddresses sa
       INNER JOIN FETCH NumberingPlan np ON sa.numberingPlanId = np.id
       INNER JOIN FETCH NatureOfAddress na ON sa.natureOfAddressId = na.id
       WHERE sa.ss7SccpId = :sccpId
       """)
	List<SccpAddressesDTO> findDetailedBySs7SccpId(@Param("sccpId") int sccpId);
}
