package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.utils.BroadcastMessageStatus;
import com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_COMMENT;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_STATUS;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_DESTINATION_ADDR;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_ENQUEUE_AT;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_MESSAGE_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_SOURCE_ADDR;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_STATISTICS_QUERY;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_STATUS;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.UPDATE_BROADCAST_DEVICES_BATCH_POSITIONAL;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.UPDATE_BROADCAST_DEVICES_FOR_MAX_EXECUTION;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.UPDATE_BROADCAST_MESSAGE_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastQueryExecutor {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcTemplate plainJdbcTemplate;



    public List<Map<String, Object>> executeQueryAndGetListMap(String query, Map<String, Object> filterParameters) {
        log.debug("Starting execute query {} with parameters {}", query, filterParameters);
        return jdbcTemplate.queryForList(query, filterParameters);
    }

    public int massiveUpdateForStoppingBroadcast(int broadcastId, BroadcastMessageStatus currentStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put(BROADCAST_STATUS, BroadcastMessageStatus.FAILED.getValue());
        params.put(BROADCAST_COMMENT, "Failed due to max execution time reached");
        params.put(BROADCAST_FILTER_ENQUEUE_AT, LocalDateTime.now());
        params.put(BROADCAST_FILTER_ID, broadcastId);
        params.put(BROADCAST_FILTER_STATUS, currentStatus.getValue());

        return jdbcTemplate.update(
                UPDATE_BROADCAST_DEVICES_FOR_MAX_EXECUTION,
                params
        );
    }

    @Retryable(retryFor = {CannotAcquireLockException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200, multiplier = 2))
    public void executeUpdateBroadcastDevicesBatch(List<Map<String, Object>> chunk, int broadcastId, LocalDateTime now) {
        log.debug("Updating DB chunk for broadcastId {}, size {}", broadcastId, chunk.size());
        long dbChunkStart = System.currentTimeMillis();

        int[] results = plainJdbcTemplate.batchUpdate(
                UPDATE_BROADCAST_DEVICES_BATCH_POSITIONAL,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                        Map<String, Object> item = chunk.get(i);
                        ps.setInt(1, (int) item.getOrDefault(BROADCAST_STATUS, BroadcastMessageStatus.ENQUEUE.getValue()));
                        ps.setTimestamp(2, Timestamp.valueOf(now));
                        ps.setString(3, (String) item.getOrDefault(BROADCAST_SOURCE_ADDR, ""));
                        ps.setString(4, (String) item.getOrDefault(BROADCAST_DESTINATION_ADDR, ""));
                        ps.setString(5, (String) item.getOrDefault(BROADCAST_COMMENT, ""));
                        ps.setInt(6, broadcastId);
                        ps.setString(7, (String) item.getOrDefault(BROADCAST_MESSAGE_ID, ""));
                    }

                    @Override
                    public int getBatchSize() {
                        return chunk.size();
                    }
                }
        );

        int totalUpdated = Arrays.stream(results).sum();
        long dbChunkTime = System.currentTimeMillis() - dbChunkStart;
        log.debug("DB chunk updated for broadcastId {}, rows {}, took {}ms", broadcastId, totalUpdated, dbChunkTime);
        if (dbChunkTime > 5000) {
            log.warn("Slow DB chunk for broadcastId {}, took {}ms", broadcastId, dbChunkTime);
        }
    }

    @Recover
    public void recoverUpdateBroadcastDevicesBatch(CannotAcquireLockException ex, List<Map<String, Object>> chunk, int broadcastId, LocalDateTime now) {
        log.error("Retries exhausted for DB chunk update, broadcastId {}, chunk size {}", broadcastId, chunk.size(), ex);
    }

    public void executeUpdateBroadcastMessageTemplate(List<Map<String, Object>> chunk, int broadcastId) {
        SqlParameterSource[] batchParams = chunk.stream()
                .map(item -> new MapSqlParameterSource()
                        .addValue(BROADCAST_STATUS, item.getOrDefault(BROADCAST_STATUS, BroadcastMessageStatus.PENDING.getValue()))
                        .addValue(BROADCAST_MESSAGE, item.get(BROADCAST_MESSAGE))
                        .addValue(BROADCAST_COMMENT, item.getOrDefault(BROADCAST_COMMENT, ""))
                        .addValue(BROADCAST_FILTER_ID, broadcastId)
                        .addValue(BROADCAST_FILTER_MESSAGE_ID, item.getOrDefault(BROADCAST_MESSAGE_ID, ""))
                        .addValue(BROADCAST_FILTER_STATUS, BroadcastMessageStatus.PENDING.getValue())
                )
                .toArray(SqlParameterSource[]::new);

        int[] results = jdbcTemplate.batchUpdate(
                UPDATE_BROADCAST_MESSAGE_TEMPLATE,
                batchParams
        );

        int totalUpdated = Arrays.stream(results).sum();
        log.debug("Message template updated for the {}-record chunk.: broadcastId {}", totalUpdated, broadcastId);
    }

    public BroadcastRecordsResponse.BroadcastStatistics getStatistics(int broadcastId) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(BROADCAST_FILTER_ID, broadcastId);

        return jdbcTemplate.queryForObject(BROADCAST_STATISTICS_QUERY, params, (rs, rowNum) ->
                new BroadcastRecordsResponse.BroadcastStatistics(
                        (long) rs.getInt("total") + rs.getInt("duplicated_attempts"),
                        rs.getInt("pending"),
                        rs.getInt("enqueue"),
                        rs.getInt("sent"),
                        rs.getInt("failed"),
                        (long) rs.getInt("duplicated") + rs.getInt("duplicated_attempts"),
                        rs.getInt("invalid")
                )
        );
    }
}
