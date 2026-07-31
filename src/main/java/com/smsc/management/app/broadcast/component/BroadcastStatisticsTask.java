package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.dto.UtilsRecords;
import com.smsc.management.utils.StaticMethods;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.paicbd.smsc.utils.GeneralSmscConstants.SMSC_DATETIME_FORMATTER;
import static com.smsc.management.utils.Constants.CHUNK_SIZE;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastStatisticsTask {
    private final JdbcTemplate jdbcTemplate;
    private final UtilsBase utilsBase;

    public void processBatchBroadcastStatistics(List<String> messages) {
        try {
            this.processStatistics(messages);
        } catch (Exception e) {
            log.error("Error to process statistics batch.", e);
        }
    }

    private void processStatistics(List<String> messages) {
        List<UtilsRecords.BroadcastStatistic> data = messages
                .parallelStream()
                .map(StaticMethods::stringAsStatisticsEvent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (data.isEmpty()) {
            return;
        }
        updateBatch(data);
    }

    @Retryable(retryFor = {CannotAcquireLockException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200, multiplier = 2))
    public void updateBatch(List<UtilsRecords.BroadcastStatistic> statistics) {
        statistics.sort(Comparator.comparing(UtilsRecords.BroadcastStatistic::messageId));
        String sql = """
            UPDATE broadcast_devices
            SET status = ?,
                sent_at = (?)::timestamp,
                comment = ?,
                dest_name = COALESCE((
                    SELECT name
                    FROM (
                        SELECT sni.id, g."name"
                        FROM sequence_networks_id sni
                        INNER JOIN gateways g ON sni.id = g.network_id
                        UNION ALL
                        SELECT sni.id, g."name"
                        FROM sequence_networks_id sni
                        INNER JOIN ss7_gateways g ON sni.id = g.network_id
                        UNION ALL
                        SELECT sni.id, g."name"
                        FROM sequence_networks_id sni
                        INNER JOIN service_provider g ON sni.id = g.network_id
                    ) AS data_name
                    WHERE id = ?
                ), ''),
                dest_network_id = ?,
                dest_protocol = COALESCE(?, ''),
                dest_network_type = COALESCE(?, '')
            WHERE broadcast_id = ? and message_id = ?;
            """;

        for (int start = 0; start < statistics.size(); start += CHUNK_SIZE) {
            List<UtilsRecords.BroadcastStatistic> chunk =
                    statistics.subList(start, Math.min(start + CHUNK_SIZE, statistics.size()));

            this.executeUpdateBatch(chunk, sql);
        }
    }

    @Recover
    public void recover(CannotAcquireLockException ex, List<UtilsRecords.BroadcastStatistic> statistics) {
        log.error("Retries failed for statistics batch of size {}", statistics.size(), ex);
    }

    private void executeUpdateBatch(List<UtilsRecords.BroadcastStatistic> chunk, String sql) {
        log.debug("Updating statistics chunk in DB, size {}", chunk.size());
        long statsStart = System.currentTimeMillis();
        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {

                UtilsRecords.BroadcastStatistic values = chunk.get(i);
                ps.setInt(1, values.status());
                ps.setString(2, utilsBase.convertStringUTCToLocalTimeZone(values.date(), SMSC_DATETIME_FORMATTER));
                ps.setString(3, values.comment());
                ps.setInt(4, values.destNetworkId());
                ps.setInt(5, values.destNetworkId());
                ps.setString(6, values.destProtocol());
                ps.setString(7, values.destNetworkType());
                ps.setInt(8, values.broadcastId());
                ps.setString(9, values.messageId());
            }

            @Override
            public int getBatchSize() {
                return chunk.size();
            }
        });
        int updated = Arrays.stream(results).sum();
        long statsTime = System.currentTimeMillis() - statsStart;
        log.debug("Broadcast statistics chunk updated, rows {}, took {}ms", updated, statsTime);
        if (statsTime > 5000) {
            log.warn("Slow broadcast statistics chunk, size {}, took {}ms", chunk.size(), statsTime);
        }
    }
}
