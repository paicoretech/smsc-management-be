package com.smsc.management.app.dnd.model.repository;

import com.smsc.management.app.dnd.model.entity.DndEntryMsisdn;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DndEntryMsisdnRepository extends JpaRepository<DndEntryMsisdn, Integer> {
    boolean existsByMsisdnAndParentId(String msisdn, Integer parentId);

    @Query("SELECT d FROM DndEntryMsisdn d WHERE d.parentId = :parentId")
    Page<DndEntryMsisdn> findByParentId(@Param("parentId") Integer parentId, Pageable pageable);

    long countByParentId(Integer parentId);

    List<DndEntryMsisdn> findAllByParentId(Integer parentId);

    @Modifying
    @Transactional
    long deleteAllByParentId(Integer parentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM DndEntryMsisdn d WHERE d.parentId = :parentId AND d.msisdn = :msisdn")
    int deleteByParentIdAndMsisdn(@Param("parentId") Integer parentId, @Param("msisdn") String msisdn);

    @Query("select d.msisdn from DndEntryMsisdn d where d.parentId = :parentId and d.msisdn in :msisdns")
    List<String> findExistingMsisdns(@Param("parentId") Integer parentId,
                                     @Param("msisdns") List<String> msisdns);
}
