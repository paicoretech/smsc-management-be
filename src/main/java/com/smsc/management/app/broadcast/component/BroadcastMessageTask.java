package com.smsc.management.app.broadcast.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.exception.RTException;
import com.paicbd.smsc.interpreter.PayloadFormat;
import com.paicbd.smsc.interpreter.PayloadInterpreter;
import com.paicbd.smsc.utils.BroadcastMessageStatus;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.dto.BroadcastParamsDTO;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastMessageConverter;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.StaticMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_COLUMN_MAPPING;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_COMMENT;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_DESTINATION_ADDR;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_MESSAGE_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_FILTER_STATUS;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE_ID;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_SOURCE_ADDR;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_STATUS;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.PAGINATION_BROADCAST_DEVICES;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.PAGINATION_BROADCAST_DEVICES_INIT;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.PROCESSING_BATCH_SIZE;
import static com.smsc.management.utils.Constants.CHUNK_SIZE;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastMessageTask {
    private final BroadcastDevicesRepository broadcastDevicesRepository;
    private final BroadcastRepository broadcastRepository;
    private final AppProperties appProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BroadcastQueryExecutor broadcastQueryExecutor;

    public void processBroadcastAsync(
            int processingBatchSize,
            ServiceProvider serviceProvider,
            Broadcast broadcast) throws InterruptedException {

        int broadcastId = broadcast.getId();
        log.info("Starting broadcast processing for id {} with systemId {}",
                broadcastId, serviceProvider.getSystemId());
        final long startTimeMillis = System.currentTimeMillis();
        int totalPages = 0;

        try {
            if (!Thread.currentThread().isInterrupted()) {
                totalPages = processAllBatches(broadcastId, processingBatchSize, serviceProvider, broadcast, true);
                broadcastRepository.changeStatus(broadcastId, BroadcastStatus.COMPLETED, LocalDateTime.now(), "Successfully completed");
                log.debug("Broadcast id {} completed successfully, totalPages {}", broadcastId, totalPages);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Broadcast processing interrupted for broadcastId {} on page {}", broadcastId, totalPages, e);
        } catch (Exception e) {
            log.error("Error processing broadcast on page {}: {}", totalPages, e.getMessage(), e);
            broadcastRepository.changeStatus(broadcastId, BroadcastStatus.FAILED, LocalDateTime.now(), "Error during execution: " + e.getMessage());
        } finally {
            if (Thread.currentThread().isInterrupted()) {
                broadcastRepository.changeStatus(broadcastId, BroadcastStatus.CANCELED, LocalDateTime.now(), "Canceled by user");
            }
            log.info("Broadcast id {} processed in {} seconds, totalPages {}", broadcastId,
                    (System.currentTimeMillis() - startTimeMillis) / 1000, totalPages);
        }
    }

    public void updateMessageTemplateAsync(int processingBatchSize, Broadcast broadcast) {

        int broadcastId = broadcast.getId();
        log.info("Updating broadcast for id {}", broadcastId);
        final long startTimeMillis = System.currentTimeMillis();
        int totalPages = 0;

        try {
            if (!Thread.currentThread().isInterrupted()) {
                totalPages = processAllBatches(broadcastId, processingBatchSize, null, broadcast, false);
                broadcastRepository.changeStatus(broadcastId, BroadcastStatus.PENDING, LocalDateTime.now(), "Message template updated");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Broadcast updating interrupted for broadcastId {} on page {}", broadcastId, totalPages, e);
        } catch (Exception e) {
            log.error("Error updating broadcast on page {}: {}", totalPages, e.getMessage(), e);
            broadcastRepository.changeStatus(broadcastId, BroadcastStatus.FAILED, LocalDateTime.now(), "Error during message template update: " + e.getMessage());
        } finally {
            if (Thread.currentThread().isInterrupted()) {
                broadcastRepository.changeStatus(broadcastId, BroadcastStatus.CANCELED, LocalDateTime.now(), "Canceled during message template update");
            }
            log.info("Broadcast id {} updated in {} seconds", broadcastId, (System.currentTimeMillis() - startTimeMillis) / 1000);
        }
    }

    private int processAllBatches(
            int broadcastId,
            int processingBatchSize,
            ServiceProvider serviceProvider,
            Broadcast broadcast,
            boolean isBroadcastProcessing) throws InterruptedException {
        int totalPages = 0;
        long totalMessages;
        ZonedDateTime endTime = convertUTCToLocalDateTime(broadcast.getMaxExecutionDateTime());

        this.updateInvalidMsisdn(broadcastId, isBroadcastProcessing);

        Map<String, Object> filterParameters = new HashMap<>();
        filterParameters.put(BROADCAST_FILTER_ID, broadcastId);
        filterParameters.put(BROADCAST_FILTER_STATUS, BroadcastMessageStatus.PENDING.getValue());
        filterParameters.put(PROCESSING_BATCH_SIZE, processingBatchSize);

        List<Map<String, Object>> dataBatchInit = broadcastQueryExecutor.executeQueryAndGetListMap(PAGINATION_BROADCAST_DEVICES_INIT, filterParameters);
        if (!dataBatchInit.isEmpty() && !verifyAndStopBroadcast(endTime, broadcastId)) {
            log.info("Reading broadcast device data started successfully.");

            totalMessages = processBatch(dataBatchInit, serviceProvider, broadcast, isBroadcastProcessing);
            filterParameters.put(BROADCAST_FILTER_MESSAGE_ID, dataBatchInit.getLast().get(BROADCAST_MESSAGE_ID).toString());
            totalPages = 1;

            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Broadcast processing for ID {} was interrupted. Stopping execution at page {}", broadcastId, totalPages);
                    break;
                }

                List<Map<String, Object>> nextBatch = broadcastQueryExecutor.executeQueryAndGetListMap(PAGINATION_BROADCAST_DEVICES, filterParameters);
                if(verifyAndStopBroadcast(endTime, broadcastId)) {
                    break;
                }

                if (nextBatch.isEmpty()) {
                    log.info("There is no more data to process for broadcast id {}, total records processed {}", broadcastId, totalMessages);
                    break;
                }

                totalMessages = totalMessages + processBatch(nextBatch, serviceProvider, broadcast, isBroadcastProcessing);
                filterParameters.put(BROADCAST_FILTER_MESSAGE_ID, nextBatch.getLast().get(BROADCAST_MESSAGE_ID).toString());
                totalPages++;
            }
            log.info("BroadcastMessageTask: Finish broadcastId: {} processingBatchSize: {} totalBatch: {} totalElementsProcessed: {}",
                    broadcastId, processingBatchSize, totalPages, totalMessages);

            if (totalMessages == 0) {
                throw new SmscBackendException("No batch processed successfully");
            }
        }

        return totalPages;
    }

    private long processBatch(List<Map<String, Object>> batch, ServiceProvider serviceProvider, Broadcast broadcast, boolean isBroadcastProcessing) {
        if (isBroadcastProcessing) {
            return processBatch(batch, serviceProvider, broadcast);
        }

        return processBatch(batch, broadcast);
    }

    private long processBatch(List<Map<String, Object>> batch, ServiceProvider serviceProvider, Broadcast broadcast) {
        long bathMessages = 0;
        long broadcastId = broadcast.getId();
        log.debug("Starting batch processing for broadcastId {}, batchSize {}", broadcastId, batch.size());
        try {
            List<String> messageEventsList = batch.stream()
                    .flatMap(device -> {
                        try {
                            MessageEvent messageEvent = new MessageEvent();
                            Map<String, String> columnMapping = Converter.stringToObject(broadcast.getColumnMapping(), new TypeReference<>() {});
                            String interpreter = BroadcastMessageConverter.createPayloadInterpreter(columnMapping);
                            PayloadInterpreter.interpreterPayloadForReceive(device.get(BROADCAST_COLUMN_MAPPING).toString(), interpreter, messageEvent, PayloadFormat.JSON);
                            messageEvent.setShortMessage(device.get(BROADCAST_MESSAGE).toString());
                            messageEvent.setBroadcastId(broadcast.getId());
                            BroadcastParamsDTO paramsDTO = new BroadcastParamsDTO(
                                    broadcast.getSourceAddrTon(),
                                    broadcast.getSourceAddrNpi(),
                                    broadcast.getDestAddrTon(),
                                    broadcast.getDestAddrNpi(),
                                    broadcast.getDataCoding()
                            );
                            BroadcastMessageConverter.completeMessageEvent(messageEvent, broadcast.getSenderId(), serviceProvider, broadcast.isRequestDlr(), device.get(BROADCAST_MESSAGE_ID).toString(), paramsDTO);

                            device.put(BROADCAST_STATUS, BroadcastMessageStatus.ENQUEUE.getValue());
                            device.put(BROADCAST_SOURCE_ADDR, messageEvent.getSourceAddr());
                            device.put(BROADCAST_DESTINATION_ADDR, messageEvent.getDestinationAddr());

                            return Stream.of(messageEvent.toString());
                        } catch (RTException | JsonProcessingException e) {
                            device.put(BROADCAST_STATUS, BroadcastMessageStatus.FAILED.getValue());
                            device.put(BROADCAST_COMMENT, "Message was not sent to kafka: " + e.getMessage());
                            log.error("Message was not created: {}", e.getMessage());
                            return Stream.empty();
                        }
                    })
                    .toList();
            log.debug("MessageEvent list built for broadcastId {}, successCount {}, failedCount {}", broadcastId, messageEventsList.size(), batch.size() - messageEventsList.size());
            if (!messageEventsList.isEmpty()) {
                long dbStart = System.currentTimeMillis();
                this.updateBatch(batch, broadcast.getId(), true);
                long dbTime = System.currentTimeMillis() - dbStart;
                log.debug("DB updated successfully for broadcastId {}, records {}, took {}ms", broadcastId, messageEventsList.size(), dbTime);
                if (dbTime > 5000) {
                    log.warn("Slow DB update for broadcastId {}, took {}ms", broadcastId, dbTime);
                }
                String kafkaTopic = StaticMethods.getKafkaTopicByPriority(serviceProvider.getMessagePriority());
                log.debug("Pushing {} messages to Kafka topic {} for broadcastId {}", messageEventsList.size(), kafkaTopic, broadcastId);
                messageEventsList.forEach(messageEvent -> kafkaTemplate.send(kafkaTopic, messageEvent));
                log.debug("Successfully pushed {} messages to Kafka for broadcastId {}", messageEventsList.size(), broadcastId);
            }
            bathMessages = messageEventsList.size();
        } catch (Exception e) {
            log.error("Error processing batch for broadcast: {}", e.getMessage(), e);
        }
        log.debug("Batch processing completed for broadcastId {}, messagesSent {}", broadcastId, bathMessages);
        return bathMessages;
    }

    private long processBatch(List<Map<String, Object>> batch, Broadcast broadcast) {
        AtomicLong bathMessages = new AtomicLong();
        try {
            batch.forEach(device -> {
                try {
                    Map<String, Object> columnMappingData = Converter.stringToObject(
                            device.get(BROADCAST_COLUMN_MAPPING).toString(),
                            new TypeReference<>() {
                            }
                    );

                    String newMessage = BroadcastMessageConverter.createShortMessage(
                            broadcast.getMessageTemplate(),
                            columnMappingData
                    );

                    device.put(BROADCAST_STATUS, BroadcastMessageStatus.PENDING.getValue());
                    device.put(BROADCAST_MESSAGE, newMessage);
                    bathMessages.getAndIncrement();
                } catch (Exception e) {
                    device.put(BROADCAST_STATUS, BroadcastMessageStatus.FAILED.getValue());
                    device.put(BROADCAST_COMMENT, "Message template was not updated: " + e.getMessage());
                    log.error("Error updating broadcast device {}", device, e);
                }
            });
            if (bathMessages.get() > 0) {
                this.updateBatch(batch, broadcast.getId(), false);
            }
        } catch (Exception e) {
            log.error("Error processing batch to update the message template for broadcast: {} -> {}", broadcast.getId(), e.getMessage(), e);
        }
        return bathMessages.get();
    }

    private ZonedDateTime convertUTCToLocalDateTime(LocalDateTime dateTime) {
        ZonedDateTime endUtc = dateTime.atZone(ZoneId.of("UTC"));
        return endUtc.withZoneSameInstant(ZoneId.systemDefault());
    }

    private boolean verifyAndStopBroadcast(ZonedDateTime endTime, int broadcastId) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime.toLocalDateTime())) {
            log.warn("Broadcast processing stopped due to reaching the max execution time");
            int totalUpdated = broadcastQueryExecutor.massiveUpdateForStoppingBroadcast(broadcastId, BroadcastMessageStatus.PENDING);
            log.warn("Updating status to FAILED for all remaining devices, for broadcastId {}, total devices {}", broadcastId, totalUpdated);

            return true;
        }

        return false;
    }

    private void updateInvalidMsisdn(int broadcastId, boolean isBroadcastProcessing) {
        if (isBroadcastProcessing) {
            log.info("Updating status for invalid numbers for broadcastId {}", broadcastId);
            broadcastDevicesRepository.updateStatusForInvalidNumbers(broadcastId, appProperties.getBroadcastValidNumberRegex());
            log.debug("Completed invalid numbers status update for broadcastId {}", broadcastId);
        }
    }

    @Retryable(retryFor = {CannotAcquireLockException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200, multiplier = 2))
    public void updateBatch(List<Map<String, Object>> batch, int broadcastId, boolean isBroadcastProcessing) {
        LocalDateTime now = LocalDateTime.now();
        for (int start = 0; start < batch.size(); start += CHUNK_SIZE) {
            List<Map<String, Object>> chunk =
                    batch.subList(start, Math.min(start + CHUNK_SIZE, batch.size()));

            if (isBroadcastProcessing) {
                broadcastQueryExecutor.executeUpdateBroadcastDevicesBatch(chunk, broadcastId, now);
            } else {
                broadcastQueryExecutor.executeUpdateBroadcastMessageTemplate(chunk, broadcastId);
            }
        }
    }

    @Recover
    public void recover(CannotAcquireLockException ex, List<Map<String, Object>> batch) {
        log.error("Retries failed for broadcast devices batch of size {}", batch.size(), ex);
    }
}
