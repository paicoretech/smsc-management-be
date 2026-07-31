package com.smsc.management.app.broadcast.config;

import com.smsc.management.app.broadcast.service.BroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastJobTest {

    private BroadcastJob broadcastJob;
    private BroadcastService broadcastService;
    private JobExecutionContext jobExecutionContext;

    @BeforeEach
    void setUp() {
        broadcastJob = new BroadcastJob();
        broadcastService = mock(BroadcastService.class);
        jobExecutionContext = mock(JobExecutionContext.class);
        broadcastJob.setBroadcastService(broadcastService);
    }

    @Test
    void testExecuteInternalShouldInvokeStartBroadcast() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("broadcastId", 123);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        broadcastJob.executeInternal(jobExecutionContext);

        verify(broadcastService, times(1)).startBroadcast(123);
    }

    @Test
    void testExecuteInternalShouldHandleExceptionGracefully() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("broadcastId", 456);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        doThrow(new RuntimeException("Simulated failure"))
                .when(broadcastService).startBroadcast(456);

        // Should not throw despite exception
        broadcastJob.executeInternal(jobExecutionContext);

        verify(broadcastService).startBroadcast(456);
    }
}