package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.model.entity.SccpRules;

public interface SccpRulesRepository extends JpaRepository<SccpRules, Integer> {
	SccpRules findById(int id);

	@Query("""
       SELECT new com.smsc.management.app.ss7.dto.SccpRulesDTO(
           sr.id, sr.name, sr.mask, sr.addressIndicator, sr.pointCode, sr.subsystemNumber,
           sr.gtIndicator, sr.translationType, sr.numberingPlanId, COALESCE(np.name, ''), 
           sr.natureOfAddressId, COALESCE(na.name, ''), sr.globalTittleDigits, sr.ruleTypeId,
           COALESCE(rt.name, ''), sr.primaryAddressId, COALESCE(pa.name, ''), 
           sr.secondaryAddressId, COALESCE(sa.name, ''), sr.loadSharingAlgorithmId, 
           COALESCE(lsa.name, ''), sr.originationTypeId, COALESCE(ot.name, ''), 
           sr.newCallingPartyAddress, sr.callingAddressIndicator, sr.callingPointCode,
           sr.callingSubsystemNumber, sr.callingTranslatorType, sr.callingNumberingPlanId, 
           COALESCE(cnp.name, ''), sr.callingNatureOfAddressId, COALESCE(cna.name, ''), 
           sr.callingGtIndicator, sr.callingGlobalTittleDigits) 
       FROM SccpRules sr
       LEFT JOIN sr.numberingPlan np 
       LEFT JOIN sr.natureOfAddress na
       LEFT JOIN sr.ruleType rt
       LEFT JOIN sr.primaryAddress pa
       LEFT JOIN sr.secondaryAddress sa
       LEFT JOIN sr.loadSharingAlgorithm lsa
       LEFT JOIN sr.originationType ot
       LEFT JOIN sr.callingNumberingPlan cnp
       LEFT JOIN sr.callingNatureOfAddress cna
       WHERE (pa.ss7SccpId = :sccpId OR sa.ss7SccpId = :sccpId)
       ORDER BY sr.id
       """)
	List<SccpRulesDTO> fetchSccpRules(@Param("sccpId") int sccpId);

}
