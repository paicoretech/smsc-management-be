package com.smsc.management.app.ss7.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.model.entity.SccpLongMessageRules;

@Repository
public interface SccpLongMessageRulesRepository extends JpaRepository<SccpLongMessageRules, Integer> {
	@Query("SELECT slmr FROM SccpLongMessageRules slmr WHERE slmr.id = :id")
	SccpLongMessageRules findById(@Param("id") int id);

	@Query("""
		SELECT new com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO(
			lmr.id, lmr.firstPointCode, lmr.lastPointCode, lmr.longMessageRuleType, lmr.sccpSapId
		)
		FROM SccpLongMessageRules lmr
		JOIN lmr.sccpSap sap
		WHERE sap.ss7SccpId = :sccpId
	""")
	List<SccpLongMessageRulesDTO> fetchLongMessageRulesBySccpId(@Param("sccpId") int sccpId);

	@Query("""
		SELECT new com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO(
			lmr.id, lmr.firstPointCode, lmr.lastPointCode, lmr.longMessageRuleType, lmr.sccpSapId
		)
		FROM SccpLongMessageRules lmr
		WHERE lmr.sccpSapId = :sccpSapId
	""")
	List<SccpLongMessageRulesDTO> fetchLongMessageRulesBySapId(@Param("sccpSapId") int sccpSapId);
}
