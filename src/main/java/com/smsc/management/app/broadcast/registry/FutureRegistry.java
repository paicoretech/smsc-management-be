package com.smsc.management.app.broadcast.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Slf4j
@Component
public class FutureRegistry {
    private final Map<Integer, Future<?>> futures = new ConcurrentHashMap<>();

    public void register(Integer broadcastId, Future<?> processingFuture) {
        log.info("Registering task group for broadcastId {}", broadcastId);
        futures.put(broadcastId, processingFuture);
    }

    public boolean cancel(Integer broadcastId) {
        Future<?> future = futures.get(broadcastId);
        if (future != null) {
            log.warn("Cancelling task group for broadcastId {}", broadcastId);
            boolean cancelledProcessing = future.cancel(true);
            futures.remove(broadcastId);
            return cancelledProcessing;
        } else {
            log.warn("No task group found to cancel for broadcastId {}", broadcastId);
            return false;
        }
    }

    public boolean exists(Integer broadcastId) {
        return futures.containsKey(broadcastId);
    }

    public void remove(Integer broadcastId) {
        futures.remove(broadcastId);
    }
}