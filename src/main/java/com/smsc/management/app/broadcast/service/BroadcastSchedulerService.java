package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.config.BroadcastJob;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.exception.SmscBackendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastSchedulerService {
    private static final String GROUP_NAME = "broadcasts";
    private final Scheduler scheduler;
    private final BroadcastRepository broadcastRepository;

    public boolean scheduleBroadcast(Integer broadcastId, LocalDateTime startDateTime) {
        boolean result = false;
        log.info("Scheduling a broadcast of id {} and startTime is {}", broadcastId, startDateTime);
        try {
            JobDetail jobDetail = JobBuilder.newJob(BroadcastJob.class)
                    .withIdentity("broadcastJob_" + broadcastId, GROUP_NAME)
                    .storeDurably(true)
                    .usingJobData("broadcastId", broadcastId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("broadcastTrigger_" + broadcastId, GROUP_NAME)
                    .startAt(Date.from(startDateTime.atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .toInstant()))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            result = true;
        } catch (SchedulerException e) {
            log.error("Failed to schedule broadcast job for broadcastId {}", broadcastId, e);
        }
        return result;
    }

    public boolean cancelScheduledBroadcast(Integer broadcastId) {
        boolean result = false;
        log.info("Cancelling the scheduled broadcast job for broadcastId {}", broadcastId);
        try {
            JobKey jobKey = new JobKey("broadcastJob_" + broadcastId, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                log.info("Broadcast job exists in scheduler, deleting the job...");
                scheduler.interrupt(jobKey);
                scheduler.deleteJob(jobKey);
            }

            result = true;
        } catch (SchedulerException e) {
            log.error("Failed to cancel broadcast job for broadcastId {}", broadcastId, e);
        }
        return result;
    }

    public void triggerByChangeStatus(BroadcastStatus newStatus, Broadcast broadcast) {
        BroadcastStatus oldStatus = broadcast.getStatus();
        this.updateStatus(BroadcastStatus.APPROVED.equals(newStatus) ? BroadcastStatus.SCHEDULED : newStatus, broadcast);

        if (BroadcastStatus.APPROVED.isEqual(newStatus)) {
            boolean scheduled = this.scheduleBroadcast(broadcast.getId(), broadcast.getStartDateTime());
            if (!scheduled) {
                this.updateStatus(oldStatus, broadcast);
                throw new SmscBackendException("Schedule broadcast failed");
            }
        } else if (BroadcastStatus.CANCELED.isEqual(newStatus)) {
            boolean stopped = this.cancelScheduledBroadcast(broadcast.getId());
            if (!stopped) {
                this.updateStatus(oldStatus, broadcast);
                throw new SmscBackendException("Broadcast stop programming failed");
            }
        }
    }

    private void updateStatus(BroadcastStatus newStatus, Broadcast broadcast) {
        if (BroadcastStatus.DELETED.equals(newStatus)) {
            broadcast.setName(broadcast.getName() + "-" + broadcast.getId());
        }
        broadcast.setStatus(newStatus);
        broadcastRepository.save(broadcast);
    }
}

