package com.smsc.management.app.broadcast.config;

import com.smsc.management.app.broadcast.service.BroadcastService;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Generated
@NoArgsConstructor
public class BroadcastJob extends QuartzJobBean {
    private BroadcastService broadcastService;

    @Autowired
    public void setBroadcastService(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        int broadcastId = jobDataMap.getInt("broadcastId");
        try {
            broadcastService.startBroadcast(broadcastId);
        } catch (Exception ex) {
            log.error("Error on start broadcast with id {}", broadcastId, ex);
        }
    }
}

