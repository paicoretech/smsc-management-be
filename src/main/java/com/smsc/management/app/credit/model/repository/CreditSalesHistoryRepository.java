package com.smsc.management.app.credit.model.repository;

import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smsc.management.app.credit.model.entity.CreditSalesHistory;
import java.util.List;

public interface CreditSalesHistoryRepository extends JpaRepository<CreditSalesHistory, Integer> {
	List<CreditSalesHistory> findByNetworkId(int networkId);

	@Query("SELECT new com.smsc.management.app.credit.dto.CreditSalesHistoryDTO(cs.id, cs.networkId, cs.credit, cs.description, cs.createdAt, cs.updatedAt, u.userName) "
			+ "FROM CreditSalesHistory cs "
			+ "JOIN cs.user u "
			+ "WHERE cs.networkId = :networkId "
			+ "ORDER BY cs.createdAt DESC"
	)
	List<CreditSalesHistoryDTO> fetchByNetworkId(@Param("networkId") int networkId);
}
