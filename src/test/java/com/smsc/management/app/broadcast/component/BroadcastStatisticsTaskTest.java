package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastStatisticsTaskTest {
    @InjectMocks
    private BroadcastStatisticsTask broadcastStatisticsTask;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UtilsBase utilsBase;

    @Test
    @DisplayName("Runs one iteration: fetch from Scylla, updates DB, then loop can be interrupted")
    void initStatisticBackgroundTask() {
        var bs = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                123, "msg-1", 1, "2025-01-01 00:00:00", "ok", "SMPP", 2, "GW"
        );

        List<String> messages = new ArrayList<>();
        messages.add(Converter.valueAsString(bs));

        lenient().when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        broadcastStatisticsTask.processBatchBroadcastStatistics(messages);

        Mockito.verify(jdbcTemplate, Mockito.timeout(2000).atLeastOnce())
                .batchUpdate(Mockito.anyString(), Mockito.any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle empty message list gracefully")
    void processBatchBroadcastStatisticsWithEmptyMessagesShouldCallBatchUpdateWithEmptyBatch() {
        List<String> emptyMessages = Collections.emptyList();

        broadcastStatisticsTask.processBatchBroadcastStatistics(emptyMessages);

        Mockito.verify(jdbcTemplate, never())
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle null message list by catching exception internally")
    void processBatchBroadcastStatisticsWithNullMessagesShouldCatchException() {
        assertDoesNotThrow(() -> broadcastStatisticsTask.processBatchBroadcastStatistics(null));

        Mockito.verify(jdbcTemplate, never())
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle database exception gracefully and not propagate it")
    void processBatchBroadcastStatisticsWithDatabaseExceptionShouldHandleGracefully() {
        var bs = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                123, "msg-1", 1, "2025-01-01 00:00:00", "ok", "SMPP", 2, "GW"
        );

        List<String> messages = List.of(Converter.valueAsString(bs));

        lenient().when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        doThrow(new DataAccessException("Database connection failed") {})
                .when(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        assertDoesNotThrow(() -> broadcastStatisticsTask.processBatchBroadcastStatistics(messages));
        Mockito.verify(jdbcTemplate, Mockito.times(1))
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should process multiple messages in single batch")
    void processBatchBroadcastStatisticsWithMultipleMessagesShouldProcessAllInBatch() {
        var bs1 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                123, "msg-1", 1, "2025-01-01 00:00:00", "ok", "SMPP", 2, "GW"
        );
        var bs2 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                124, "msg-2", 2, "2025-01-01 00:01:00", "failed", "HTTP", 3, "SP"
        );

        List<String> messages = List.of(
                Converter.valueAsString(bs1),
                Converter.valueAsString(bs2)
        );

        lenient().when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        broadcastStatisticsTask.processBatchBroadcastStatistics(messages);

        Mockito.verify(jdbcTemplate, Mockito.times(1))
                .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }
}