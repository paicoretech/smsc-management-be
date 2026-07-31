package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastStatisticsListenerTest {

    @InjectMocks
    private BroadcastStatisticsListener broadcastStatisticsListener;

    @Mock
    private BroadcastStatisticsTask broadcastStatisticsTask;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private UtilsBase utilsBase;

    @BeforeEach
    void setUp() {
        // For functional tests, inject real task with mocked dependencies
        BroadcastStatisticsTask realTask = new BroadcastStatisticsTask(jdbcTemplate, utilsBase);
        ReflectionTestUtils.setField(broadcastStatisticsListener, "broadcastStatisticsTask", realTask);
    }

    @Test
    @DisplayName("Should process valid messages and execute database update with same data")
    void handleBroadcastStatisticsWithValidMessagesShouldProcessAndUpdateDatabase() throws SQLException {
        // Arrange - Create proper BroadcastStatistic records
        var bs1 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                123, "msg-1", 1, "2025-01-01T00:00:00", "ok", "SMPP", 2, "GW"
        );
        var bs2 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                124, "msg-2", 2, "2025-01-01T00:01:00", "failed", "HTTP", 3, "SP"
        );

        List<String> messages = Arrays.asList(
                Converter.valueAsString(bs1),
                Converter.valueAsString(bs2)
        );

        when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        // Act - Full flow from Kafka listener to database
        broadcastStatisticsListener.handleBroadcastStatistics(messages);

        // Assert - Verify database update was called and capture the setter to verify data
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), captor.capture());
        
        BatchPreparedStatementSetter setter = captor.getValue();
        assertNotNull(setter);
        assertEquals(2, setter.getBatchSize()); // Should match the number of messages
        
        // Verify that the actual data from messages reaches the database by checking PreparedStatement calls
        PreparedStatement mockPs = org.mockito.Mockito.mock(PreparedStatement.class);
        
        // Test first message data
        setter.setValues(mockPs, 0);
        verify(mockPs).setInt(1, 1); // status from bs1
        verify(mockPs).setString(2, "2025-01-01 00:00:00"); // converted date
        verify(mockPs).setString(3, "ok"); // comment from bs1
        verify(mockPs).setInt(4, 2); // destNetworkId from bs1
        verify(mockPs).setInt(8, 123); // broadcast id from bs1
        verify(mockPs).setString(9, "msg-1"); // messageId from bs1
    }

    @Test
    @DisplayName("Should handle empty message list by skipping processing")
    void handleBroadcastStatisticsWithEmptyMessagesShouldNotCallDatabase() {
        List<String> emptyMessages = Collections.emptyList();

        broadcastStatisticsListener.handleBroadcastStatistics(emptyMessages);

        // Should not perform any database operations
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle null message list by skipping processing")
    void handleBroadcastStatisticsWithNullMessagesShouldNotCallDatabase() {
        broadcastStatisticsListener.handleBroadcastStatistics(null);

        // Should not perform any database operations
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should handle single message correctly and update database")
    void handleBroadcastStatisticsWithSingleMessageShouldUpdateDatabase() throws SQLException {
        // Arrange - Use proper BroadcastStatistic format
        var bs = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                125, "single-msg", 1, "2025-01-01T00:00:00", "delivered", "SMPP", 1, "GW"
        );

        List<String> singleMessage = Collections.singletonList(Converter.valueAsString(bs));

        when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        // Act - Full end-to-end flow
        broadcastStatisticsListener.handleBroadcastStatistics(singleMessage);

        // Assert - Verify the same data reaches the database
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), captor.capture());
        
        BatchPreparedStatementSetter setter = captor.getValue();
        assertNotNull(setter);
        assertEquals(1, setter.getBatchSize()); // Should match single message
        
        // Verify the actual data integrity
        PreparedStatement mockPs = org.mockito.Mockito.mock(PreparedStatement.class);
        setter.setValues(mockPs, 0);
        verify(mockPs).setInt(1, 1); // status from bs
        verify(mockPs).setString(3, "delivered"); // comment from bs
        verify(mockPs).setInt(8, 125); // broadcast id from bs
        verify(mockPs).setString(9, "single-msg"); // messageId from bs
    }

    @Test
    @DisplayName("Should handle database exceptions by catching and logging them")
    void handleBroadcastStatisticsWithDatabaseExceptionShouldCatchAndLog() {
        var bs = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(
                126, "error-msg", 1, "2025-01-01T00:00:00", "error", "SMPP", 1, "GW"
        );

        List<String> messages = Arrays.asList(Converter.valueAsString(bs));

        lenient().when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        doThrow(new RuntimeException("Database processing failed"))
                .when(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        // The task implementation catches all exceptions and logs them without rethrowing
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> 
            broadcastStatisticsListener.handleBroadcastStatistics(messages));
    }

    @Test
    @DisplayName("Should process large message batches correctly")
    void handleBroadcastStatisticsWithLargeMessageBatchShouldUpdateDatabase() throws SQLException {
        // Arrange - Create multiple proper BroadcastStatistic records
        var bs1 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(127, "batch-msg-1", 1, "2025-01-01T00:00:00", "processed", "SMPP", 1, "GW");
        var bs2 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(128, "batch-msg-2", 2, "2025-01-01T00:01:00", "processed", "HTTP", 2, "SP");
        var bs3 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(129, "batch-msg-3", 1, "2025-01-01T00:02:00", "failed", "SMPP", 1, "GW");
        var bs4 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(130, "batch-msg-4", 3, "2025-01-01T00:03:00", "delivered", "HTTP", 3, "SP");
        var bs5 = new com.paicbd.smsc.dto.UtilsRecords.BroadcastStatistic(131, "batch-msg-5", 2, "2025-01-01T00:04:00", "pending", "SMPP", 2, "GW");

        List<String> largeMessageBatch = Arrays.asList(
                Converter.valueAsString(bs1),
                Converter.valueAsString(bs2),
                Converter.valueAsString(bs3),
                Converter.valueAsString(bs4),
                Converter.valueAsString(bs5)
        );

        when(utilsBase.convertStringUTCToLocalTimeZone(anyString(), any()))
                .thenReturn("2025-01-01 00:00:00");

        ArgumentCaptor<BatchPreparedStatementSetter> captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        // Act - Full end-to-end flow
        broadcastStatisticsListener.handleBroadcastStatistics(largeMessageBatch);

        // Assert - Verify all messages are processed in single batch
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), captor.capture());
        
        BatchPreparedStatementSetter setter = captor.getValue();
        assertNotNull(setter);
        assertEquals(5, setter.getBatchSize()); // Should match all 5 messages
        
        // Verify first message data integrity in the batch
        PreparedStatement mockPs = org.mockito.Mockito.mock(PreparedStatement.class);
        setter.setValues(mockPs, 0);
        verify(mockPs).setInt(1, 1); // status from bs1
        verify(mockPs).setString(3, "processed"); // comment from bs1
        verify(mockPs).setInt(8, 127); // broadcast id from bs1
        verify(mockPs).setString(9, "batch-msg-1"); // messageId from bs1
    }
}