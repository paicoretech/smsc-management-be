package com.smsc.management.app.broadcast.model.repository;

import com.smsc.management.app.analyze.cdrs.dto.BroadcastCatalog;
import com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<Broadcast, Integer> {
    Optional<Broadcast> findById(int id);

    Optional<Broadcast> findByIdAndStatusNot(int id, BroadcastStatus status);

    @Query("SELECT new com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse$BroadcastViewer(" +
            "brc.id, brc.name, brc.status, brcf.totalRows, brc.startDateTime, brc.maxExecutionDateTime, brc.comment, brc.createdById, " +
            "COALESCE(u.userName, 'Unknown')) " +
            "FROM Broadcast brc " +
            "JOIN BroadcastFile brcf ON brc.fileId = brcf.id " +
            "LEFT JOIN Users u ON brc.createdById = u.id " +
            "WHERE brc.status <> 'DELETED' " +
            "ORDER BY brc.id")
    List<BroadcastRecordsResponse.BroadcastViewer> getAllBroadcast();

    @Modifying
    @Transactional
    @Query("UPDATE Broadcast brc SET brc.status = :status, brc.updatedAt = :updateAt, brc.comment = :message WHERE brc.id = :id")
    void changeStatus(@Param("id") int id, @Param("status") BroadcastStatus status, @Param("updateAt") LocalDateTime updateAt, @Param("message") String message);

    boolean existsById(int id);

    List<Broadcast> findByStatus(BroadcastStatus status);

    @Query("SELECT e.id FROM Broadcast e WHERE e.createdById = :createdById AND e.status <> 'DELETED'")
    List<Integer> findIdsByCreatedByUserId(@Param("createdById") int createdById);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, int id);

    @Query("SELECT e.id FROM Broadcast e WHERE e.createdById IN :createdByIds AND e.status <> 'DELETED'")
    List<Integer> findIdsByCreatedByUserIds(@Param("createdByIds") List<Integer> createdByIds);

    @Query("SELECT new com.smsc.management.app.analyze.cdrs.dto.BroadcastCatalog(e.id, e.name, u.id, u.userName) " +
            "FROM Broadcast e INNER JOIN Users u ON e.createdById = u.id WHERE e.status <> 'DELETED'")
    List<BroadcastCatalog> findAllBroadcastForFilter();
}
