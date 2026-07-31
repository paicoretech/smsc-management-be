package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.kafka.KafkaConsumerConstants;
import com.paicbd.smsc.kafka.KafkaTopicsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastStatisticsListener {
    private final BroadcastStatisticsTask broadcastStatisticsTask;

    @KafkaListener(topics = KafkaTopicsConstants.BROADCAST_STATISTIC_TOPIC, groupId = KafkaConsumerConstants.BACKEND_API_GROUP_ID_PREFIX, concurrency = "1")
    public void handleBroadcastStatistics(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            log.debug("Message list is empty");
            return;
        }
        log.debug("Batch listener for broadcast statistics {}", messages.size());
        this.broadcastStatisticsTask.processBatchBroadcastStatistics(messages);
    }
}
