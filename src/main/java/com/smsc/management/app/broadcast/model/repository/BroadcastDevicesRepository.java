package com.smsc.management.app.broadcast.model.repository;

import com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevicesId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface BroadcastDevicesRepository extends JpaRepository<BroadcastDevices, BroadcastDevicesId> {
    @Transactional
    @Modifying
    void deleteAllByBroadcastId(int broadcastId);

    @Query("SELECT new com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse$BroadcastStatistics( " +
            "COALESCE(COUNT(brcd), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 1 THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 2 THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 3 THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 4 THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 5 THEN 1 ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN brcd.status = 6 THEN 1 ELSE 0 END), 0) " +
            ") " +
            "FROM BroadcastDevices brcd " +
            "WHERE brcd.broadcastId = :broadcastId ")
    BroadcastRecordsResponse.BroadcastStatistics getStatistics(@Param("broadcastId") int broadcastId);

    Page<BroadcastDevices> findByBroadcastIdAndStatus(int broadcastId, int status, Pageable pageable);


    @Modifying
    @Query(value = """
            INSERT INTO broadcast_devices (
                broadcast_id, message_id, message, column_mapping_data_str, source_addr, destination_addr, status
            )
            SELECT
                :newBroadcastId, CAST(extract(epoch FROM clock_timestamp()) * 1000 AS bigint) || '-' ||
                CAST(floor(random() * 9000000000000) + 1000000000000 AS bigint) AS message_id, message, column_mapping_data_str, source_addr, destination_addr, 1
            FROM broadcast_devices bd
            WHERE bd.broadcast_id = :originalBroadcastId
            """, nativeQuery = true)
    void copyBroadcastDevicesWithNewMessageIds(
            @Param("originalBroadcastId") int originalBroadcastId,
            @Param("newBroadcastId") int newBroadcastId);

    List<BroadcastDevices> findByBroadcastIdAndStatus(int broadcastId, int status);

    @Modifying
    @Transactional
    @Query(value = """
        WITH ranked_duplicates AS (
            SELECT
                message_id, ROW_NUMBER() OVER (PARTITION BY destination_addr, message ORDER BY message_id) AS rn
            FROM broadcast_devices bd
            WHERE broadcast_id = :broadcastId
        )
        UPDATE broadcast_devices
        SET status = 5
        WHERE message_id IN (
            SELECT message_id
            FROM ranked_duplicates
            WHERE rn > 1
        )
        """, nativeQuery = true)
    void updateDuplicateStatus(@Param("broadcastId") int broadcastId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE broadcast_devices bd " +
            "SET status = 6 " +
            "WHERE broadcast_id = :broadcastId " +
            "AND bd.destination_addr !~ :regex",
            nativeQuery = true)
    void updateStatusForInvalidNumbers(
            @Param("broadcastId") int broadcastId,
            @Param("regex") String regex);

    @Query("""
            SELECT new com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse$FailureReasonSummary(
                COALESCE(bd.comment, ''), 
                COUNT(bd)
            )
            FROM BroadcastDevices bd
            WHERE bd.broadcastId = :broadcastId
              AND bd.status = 4
            GROUP BY COALESCE(bd.comment, '')
            ORDER BY COUNT(bd) DESC, COALESCE(bd.comment, '')   
            """)
    List<BroadcastRecordsResponse.FailureReasonSummary> getFailureReasons(
            @Param("broadcastId") int broadcastId);


}
