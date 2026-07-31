package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class BroadcastSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private BroadcastRepository broadcastRepository;

    @InjectMocks
    private BroadcastSchedulerService broadcastSchedulerService;

    @BeforeEach
    void setUp() {
        scheduler = mock(Scheduler.class);
        broadcastSchedulerService = new BroadcastSchedulerService(scheduler, broadcastRepository);
    }

    @Test
    void testScheduleBroadcastWhenAllGoodThenDoItSuccessfully() throws Exception {
        int broadcastId = 1;
        LocalDateTime startDateTime = LocalDateTime.now().plusMinutes(10);

        Assertions.assertTrue(broadcastSchedulerService.scheduleBroadcast(broadcastId, startDateTime));

        Mockito.verify(scheduler, times(1)).scheduleJob(
                Mockito.argThat(jobDetail ->
                        jobDetail.getKey().getName().equals("broadcastJob_" + broadcastId) &&
                                jobDetail.getJobDataMap().getInt("broadcastId") == broadcastId
                ),
                Mockito.argThat(trigger ->
                        trigger.getKey().getName().equals("broadcastTrigger_" + broadcastId) &&
                                trigger.getStartTime().equals(Date.from(
                                        startDateTime.atZone(ZoneId.of("UTC"))
                                                .withZoneSameInstant(ZoneId.systemDefault())
                                                .toInstant()
                                ))
                )
        );
    }

    @Test
    void testScheduleBroadcastWhenSchedulerExceptionTheDoNothing() throws Exception {
        Integer broadcastId = 2;
        LocalDateTime startDateTime = LocalDateTime.now().plusMinutes(5);

        Mockito.doThrow(new SchedulerException("Test Exception"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        Assertions.assertFalse(broadcastSchedulerService.scheduleBroadcast(broadcastId, startDateTime));

        Mockito.verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void testCancelScheduledBroadcastWhenJobExistsThenDoItSuccessfully() throws Exception {
        Integer broadcastId = 42;
        JobKey jobKey = new JobKey("broadcastJob_" + broadcastId, "broadcasts");

        Mockito.when(scheduler.checkExists(jobKey)).thenReturn(true);

        Assertions.assertTrue(broadcastSchedulerService.cancelScheduledBroadcast(broadcastId));

        Mockito.verify(scheduler).checkExists(jobKey);
        Mockito.verify(scheduler).deleteJob(jobKey);
    }

    @Test
    void testCancelScheduledBroadcastWhenJobDoesNotExistTheDoNothing() throws Exception {
        Integer broadcastId = 43;
        JobKey jobKey = new JobKey("broadcastJob_" + broadcastId, "broadcasts");

        Mockito.when(scheduler.checkExists(jobKey)).thenReturn(false);

        Assertions.assertTrue(broadcastSchedulerService.cancelScheduledBroadcast(broadcastId));

        Mockito.verify(scheduler).checkExists(jobKey);
        Mockito.verify(scheduler, Mockito.never()).deleteJob(any());
    }

    @Test
    void testCancelScheduledBroadcastWhenSchedulerExceptionTheDoNothing() throws Exception {
        Integer broadcastId = 44;
        JobKey jobKey = new JobKey("broadcastJob_" + broadcastId, "broadcasts");

        Mockito.when(scheduler.checkExists(jobKey)).thenThrow(new SchedulerException("Simulated error"));

        Assertions.assertFalse(broadcastSchedulerService.cancelScheduledBroadcast(broadcastId));

        Mockito.verify(scheduler).checkExists(jobKey);
        Mockito.verify(scheduler, Mockito.never()).deleteJob(any());
    }
}