package com.smsc.management.app.broadcast.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.component.BroadcastFileTask;
import com.smsc.management.app.broadcast.component.BroadcastMessageTask;
import com.smsc.management.app.broadcast.component.BroadcastQueryExecutor;
import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse;
import com.smsc.management.app.broadcast.dto.BroadcastStatusRequestDTO;
import com.smsc.management.app.broadcast.dto.BroadcastTestDTO;
import com.smsc.management.app.broadcast.mapper.BroadcastMapper;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.registry.FutureRegistry;
import com.smsc.management.app.broadcast.utils.BroadcastMessageConverter;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utils.Utils;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.StaticMethods;
import com.smsc.management.app.user.utils.UtilsUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.smsc.management.utils.Constants.ACTION_START;
import static com.smsc.management.utils.Constants.ACTION_UPDATE;
import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.INVALID_STATUS_TO_DUPLICATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {
    private final BroadcastRepository broadcastRepository;
    private final BroadcastDevicesRepository broadcastDevicesRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final UserServiceProviderRepository userServiceProviderRepository;
    private final BroadcastMapper broadcastMapper;
    private final BroadcastFileService broadcastFileService;
    private final BroadcastMessageTask broadcastProcessingService;
    private final BroadcastFileTask broadcastFileTask;
    private final AppProperties appProperties;
    private final BroadcastSchedulerService broadcastSchedulerService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FutureRegistry futureRegistry;
    private final ScheduledExecutorService scheduledBroadcastExecutor;
    private final BroadcastQueryExecutor broadcastQueryExecutor;

    @EventListener(ApplicationReadyEvent.class)
    public void broadcast() {
        List<Broadcast> broadcastsEnqueueList = broadcastRepository.findByStatus(BroadcastStatus.SCHEDULED);
        if (!broadcastsEnqueueList.isEmpty()) {
            broadcastsEnqueueList.forEach(broadcast -> {
                log.info("Scheduling broadcast id {}, startTime {} with maxExecutionDateTime {}",
                        broadcast.getId(), broadcast.getStartDateTime(), broadcast.getMaxExecutionDateTime());
                broadcastSchedulerService
                        .scheduleBroadcast(broadcast.getId(), broadcast.getStartDateTime());
            });
        }
    }

    public ApiResponse getAll() {
        try {
            List<BroadcastRecordsResponse.BroadcastViewer> broadcastList = broadcastRepository.getAllBroadcast();
            return ResponseMapping.successMessage("Get broadcast request successfully.", broadcastList);
        } catch (Exception e) {
            log.error("Get broadcast request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get broadcast request with error", e);
        }
    }

    public ApiResponse getById(int broadcastId) {
        try {
            Broadcast broadcast = broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED)
                    .orElseThrow(() -> new SmscBackendException("Configuration was not found for broadcast id " + broadcastId));

            BroadcastFile broadcastFile = broadcastFileService.getFileById(broadcast.getFileId());

            BroadcastRecordsResponse.BroadcastStatistics broadcastStatistics = broadcastQueryExecutor.getStatistics(broadcastId);

            BroadcastDTO dto = broadcastMapper.toDto(broadcast);
            if (broadcast.getColumnMapping() != null) {
                Map<String, String> columnMap = Converter.stringToObject(broadcast.getColumnMapping(),
                        new TypeReference<>() {
                        });
                dto.setColumnMapping(columnMap);
            }

            Map<String, Object> columnMappingData = Converter.stringToObject(broadcast.getFirstRecordMapping(), new TypeReference<>() {});
            String preview = BroadcastMessageConverter.createShortMessage(broadcast.getMessageTemplate(), columnMappingData);

            BroadcastRecordsResponse.BroadcastReader broadcastReader = new BroadcastRecordsResponse.BroadcastReader(
                    dto,
                    broadcastFile,
                    broadcastStatistics,
                    columnMappingData,
                    preview);

            return ResponseMapping.successMessage("Broadcast was found successfully.", broadcastReader);
        } catch (Exception e) {
            log.error("Get broadcast by id request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get broadcast by id request with error", e);
        }
    }

    public ApiResponse create(BroadcastDTO newBroadcast) {
        try {
            if (newBroadcast.getMaxExecutionDateTime().isBefore(newBroadcast.getStartDateTime())) {
                throw new SmscBackendException("MaxExecutionDateTime must be after StartDateTime");
            }
            if (broadcastRepository.existsByName(newBroadcast.getName())) {
                throw new SmscBackendException("Broadcast name must be unique");
            }

            this.validateServiceProvider(newBroadcast.getNetworkId());
            this.validateUserSenderId(newBroadcast);
            BroadcastFile broadcastFile = broadcastFileService.getFileById(newBroadcast.getFileId());
            Broadcast broadcast = this.broadcastMapper.toEntity(newBroadcast);
            broadcast.setStatus(BroadcastStatus.CREATING);

            if (newBroadcast.getColumnMapping() != null && !newBroadcast.getColumnMapping().isEmpty()) {
                String mappingJson = Converter.valueAsString(newBroadcast.getColumnMapping());
                broadcast.setColumnMapping(mappingJson);
            }
            var broadcastResult = broadcastRepository.save(broadcast);

            InputStream inputStream = StaticMethods.getInputStreamFromFile(broadcastFile.getFilename(), appProperties.getUploadBroadcastDir());
            this.startFileReader(inputStream, broadcastFile.getFilename(), broadcastFile.getId(), broadcastResult, broadcastFile.isHasHeader(), broadcastFile.getDelimiter());

            return ResponseMapping.successMessage("Creating new broadcast successfully.",
                    this.broadcastMapper.toDto(broadcastResult));
        } catch (Exception e) {
            log.error("New broadcast request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New broadcast request with error", e);
        }
    }

    public ApiResponse update(int broadcastId, BroadcastDTO newBroadcast, boolean updatedFile) {
        try {
            Broadcast currentBroadcast = broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED)
                    .orElseThrow(() -> new SmscBackendException("Broadcast with id " + broadcastId + " not found"));
            this.validateBroadcast(currentBroadcast, ACTION_UPDATE);

            if (broadcastRepository.existsByNameAndIdNot(newBroadcast.getName(), broadcastId)) {
                throw new SmscBackendException("Broadcast name must be unique");
            }

            this.validateServiceProvider(newBroadcast.getNetworkId());
            this.validateUserSenderId(newBroadcast);
            newBroadcast.setStatus(currentBroadcast.getStatus());
            boolean isMessageTemplateUpdated = !Objects.equals(newBroadcast.getMessageTemplate(), currentBroadcast.getMessageTemplate());
            this.broadcastMapper.updateEntityFromDTO(newBroadcast, currentBroadcast);

            Utils.validateBroadcastStatusAllowed(currentBroadcast.getStatus(), BroadcastStatus.UPDATING);
            currentBroadcast.setStatus(BroadcastStatus.UPDATING);

            if (updatedFile) {
                BroadcastFile savedFile = broadcastFileService.getFileById(currentBroadcast.getFileId());
                InputStream inputStream = StaticMethods.getInputStreamFromFile(savedFile.getFilename(), appProperties.getUploadBroadcastDir());
                this.startFileReader(inputStream, savedFile.getFilename(), savedFile.getId(), currentBroadcast, savedFile.isHasHeader(), savedFile.getDelimiter());
            } else if (isMessageTemplateUpdated) {
                scheduledBroadcastExecutor.submit(() -> {
                    try {
                        broadcastProcessingService.updateMessageTemplateAsync(
                                appProperties.getProcessingBatchSize(),
                                currentBroadcast);
                    } catch (Exception e) {
                        log.error("Error updating message template async for broadcastId {}: {}", broadcastId,
                                e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                });
            } else {
                currentBroadcast.setStatus(BroadcastStatus.PENDING);
            }
            Broadcast broadcastResult = broadcastRepository.save(currentBroadcast);

            return ResponseMapping.successMessage("Updating new broadcast successfully.", this.broadcastMapper.toDto(broadcastResult));
        } catch (Exception e) {
            log.error("Update broadcast request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update broadcast request with error", e);
        }
    }

    public ApiResponse changeStatus(int broadcastId, BroadcastStatusRequestDTO broadcastStatusRequestDTO) {
        try {
            Broadcast currentBroadcast = broadcastRepository.findById(broadcastId)
                    .orElseThrow(() -> new SmscBackendException("Broadcast not found"));

            BroadcastStatus newStatus = BroadcastStatus.valueOf(broadcastStatusRequestDTO.getBroadcastStatus());

            if (newStatus == BroadcastStatus.DELETED) {
                validateDeletionPermission(currentBroadcast);
            }

            Utils.validateBroadcastStatusAllowed(currentBroadcast.getStatus(), newStatus);
            currentBroadcast.setComment(broadcastStatusRequestDTO.getComment());
            broadcastSchedulerService.triggerByChangeStatus(newStatus, currentBroadcast);
            if (newStatus == BroadcastStatus.CANCELED) {
                cancelBroadcast(broadcastId);
            }
            return ResponseMapping.successMessage("Changing status of broadcast successfully.", this.broadcastMapper.toDto(currentBroadcast));
        } catch (Exception e) {
            log.error("Change broadcast request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Change broadcast request with error", e);
        }
    }

    public void startBroadcast(int broadcastId) {
        try {
            Broadcast broadcast = broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED)
                    .orElseThrow(() -> new SmscBackendException("Broadcast id " + broadcastId + " was not found"));
            this.validateBroadcast(broadcast, ACTION_START);
            this.existsServiceProvider(broadcast.getNetworkId());
            log.info("Starting broadcast with id {}, currentTime {}, startTime {}, maxExecutionTime {}", broadcastId, LocalDateTime.now(), broadcast.getStartDateTime(), broadcast.getMaxExecutionDateTime());

            ServiceProvider sp = serviceProviderRepository.findById(broadcast.getNetworkId());
            Utils.validateBroadcastStatusAllowed(broadcast.getStatus(), BroadcastStatus.PROCESSING);
            broadcast.setStatus(BroadcastStatus.PROCESSING);
            broadcastRepository.save(broadcast);

            Future<?> processingFuture = scheduledBroadcastExecutor.submit(() -> {
                try {
                    broadcastProcessingService.processBroadcastAsync(
                            appProperties.getProcessingBatchSize(),
                            sp,
                            broadcast);
                } catch (InterruptedException e) {
                    log.error("Broadcast processing task interrupted for broadcastId {}", broadcastId, e);
                    Thread.currentThread().interrupt();
                } finally {
                    futureRegistry.remove(broadcastId);
                }
            });

            futureRegistry.register(broadcastId, processingFuture);
        } catch (Exception e) {
            log.error("Start broadcast request with error: {}", e.getMessage());
        }
    }

    public void cancelBroadcast(int broadcastId) {
        if (futureRegistry.exists(broadcastId)) {
            boolean cancelled = futureRegistry.cancel(broadcastId);
            if (cancelled) {
                log.warn("Broadcast {} successfully cancelled.", broadcastId);
            }
        } else {
            log.warn("No running process found to cancel for broadcast {}", broadcastId);
        }
    }

    public ApiResponse sendTestMessage(BroadcastTestDTO broadcastTest) {
        try {
            ServiceProvider sp = serviceProviderRepository.findByNetworkIdAndProtocolAndEnabledNot(broadcastTest.getNetworkId(), "HTTP", DELETED_ENABLED_STATUS);
            if (Objects.isNull(sp)) {
                throw new SmscBackendException("Invalid service provider");
            }

            String messageId = System.currentTimeMillis() + "-" + System.nanoTime();
            MessageEvent event = BroadcastMessageConverter.createMessageEventTest(broadcastTest, messageId, sp);
            String payload = Converter.valueAsString(event);

            // Route test message to priority-based Kafka topic
            String kafkaTopic = StaticMethods.getKafkaTopicByPriority(sp.getMessagePriority());
            kafkaTemplate.send(kafkaTopic, messageId, payload);

            return ResponseMapping.successMessage("Sending broadcast test message successfully.", null);
        } catch (Exception e) {
            log.error("Sending broadcast test request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Sending broadcast test request with error", e);
        }
    }

    @Transactional
    public ApiResponse cloneBroadcast(int broadcastId) {
        try {
            Broadcast originalBroadcast = broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED)
                    .orElseThrow(() -> new SmscBackendException("No broadcast found with id " + broadcastId));
            boolean invalidStatusDuplicate = INVALID_STATUS_TO_DUPLICATE.contains(originalBroadcast.getStatus().getName());
            if (invalidStatusDuplicate) {
                throw new SmscBackendException(
                        "Invalid broadcast status " + originalBroadcast.getStatus() + " for duplicating action");
            }

            Broadcast broadcastCopy = broadcastRepository.save(createBroadcastCopy(originalBroadcast));
            broadcastDevicesRepository.copyBroadcastDevicesWithNewMessageIds(originalBroadcast.getId(), broadcastCopy.getId());
            return ResponseMapping.successMessage(
                    "Broadcast with id " + broadcastId + " was cloned successfully, new cloned broadcast id is "
                            + broadcastCopy.getId(),
                    "{}");
        } catch (Exception e) {
            log.error("Cloning broadcast has finished with error: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Cloning broadcast has finished with error", e);
        }
    }

    public ApiResponse getFailureReasons(int broadcastId) {
        try {
            broadcastRepository.findByIdAndStatusNot(broadcastId, BroadcastStatus.DELETED)
                    .orElseThrow(() -> new SmscBackendException("Broadcast not found"));

            var rows = broadcastDevicesRepository.getFailureReasons(broadcastId);
            return ResponseMapping.successMessage("Failure reasons retrieved", rows);
        } catch (Exception e) {
            log.error("Error retrieving failure reasons for broadcastId={}", broadcastId, e);
            return ResponseMapping.exceptionMessage("Error retrieving failure reasons", e);
        }
    }

    private void validateBroadcast(Broadcast broadcast, String action) {
        if (action.equals(ACTION_UPDATE) &&
                (BroadcastStatus.CREATING.isEqual(broadcast.getStatus())
                        || BroadcastStatus.PROCESSING.isEqual(broadcast.getStatus())
                        || BroadcastStatus.UPDATING.isEqual(broadcast.getStatus())
                        || BroadcastStatus.COMPLETED.isEqual(broadcast.getStatus()))) {
            throw new SmscBackendException("Current status is " + broadcast.getStatus() + " cannot be edited.");
        }
    }

    private void existsServiceProvider(int networkId) {
        if (!serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(networkId, "HTTP", DELETED_ENABLED_STATUS)) {
            throw new SmscBackendException("Invalid systemId");
        }
    }

    private void startFileReader(InputStream inputStream, String filename, int fileId, Broadcast broadcast, boolean hasHeaders, String delimiter) {
        scheduledBroadcastExecutor.submit(() -> broadcastFileTask.processFileAsync(
                inputStream,
                filename,
                fileId,
                broadcast,
                appProperties.getLoadCsvBatchSize(),
                hasHeaders,
                delimiter));
    }

    @Transactional
    public Broadcast createBroadcastCopy(Broadcast originalBroadcast) {
        String baseName = originalBroadcast.getName() + "-copy";
        String uniqueName = baseName;
        int counter = 1;

        while (broadcastRepository.existsByName(uniqueName)) {
            uniqueName = baseName + "-" + counter;
            counter++;
        }

        Broadcast newBroadcast = new Broadcast();
        newBroadcast.setName(uniqueName);
        newBroadcast.setDescription(originalBroadcast.getDescription());
        newBroadcast.setNetworkId(originalBroadcast.getNetworkId());
        newBroadcast.setStatus(BroadcastStatus.DRAFT);
        newBroadcast.setColumnMapping(originalBroadcast.getColumnMapping());
        newBroadcast.setMessageTemplate(originalBroadcast.getMessageTemplate());
        newBroadcast.setSenderId(originalBroadcast.getSenderId());
        newBroadcast.setFileId(originalBroadcast.getFileId());
        newBroadcast.setStartDateTime(originalBroadcast.getStartDateTime());
        newBroadcast.setMaxExecutionDateTime(originalBroadcast.getMaxExecutionDateTime());
        newBroadcast.setRequestDlr(originalBroadcast.isRequestDlr());
        newBroadcast.setFirstRecordMapping(originalBroadcast.getFirstRecordMapping());
        newBroadcast.setSourceAddrNpi(originalBroadcast.getSourceAddrNpi());
        newBroadcast.setSourceAddrTon(originalBroadcast.getSourceAddrTon());
        newBroadcast.setDestAddrNpi(originalBroadcast.getDestAddrNpi());
        newBroadcast.setDestAddrTon(originalBroadcast.getDestAddrTon());
        newBroadcast.setDataCoding(originalBroadcast.getDataCoding());
        return broadcastRepository.save(newBroadcast);
    }

    private void validateServiceProvider(int networkId) {
        if (!serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(networkId, "HTTP", DELETED_ENABLED_STATUS)) {
            throw new SmscBackendException("Invalid systemId");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthorized access attempt to service provider validation - no valid authentication found");
            throw new SmscBackendException("Authentication required to access this resource");
        }

        String username = auth.getName();
        Users currentUser = userRepository.findByUserName(username).orElse(null);

        if (currentUser == null) {
            log.warn("Unauthorized access attempt - user not found: {}", username);
            throw new SmscBackendException("User not found");
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.equals("ROOT") || role.equals("ADMINISTRATOR"));

        if (!isAdmin && !currentUser.isAllServiceProviders()) {
            boolean hasAccess = userServiceProviderRepository
                    .existsByUserIdAndServiceProviderNetworkId(currentUser.getId(), networkId);

            if (!hasAccess) {
                throw new SmscBackendException("You do not have access to this service provider");
            }
        }
    }

    private void validateUserSenderId(BroadcastDTO broadcastDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticatedUser = Objects.nonNull(auth) && auth.isAuthenticated();

        if (isAuthenticatedUser) {
            String username = auth.getName();
            userRepository.findByUserName(username).ifPresent(user -> {

                // getting the user's assigned Sender Ids
                List<String> userAssignedSenderIds = user.getSenderIds();

                boolean hasRestrictedSenderIds = Objects.nonNull(userAssignedSenderIds)
                        && !userAssignedSenderIds.isEmpty();

                if (!hasRestrictedSenderIds) {
                   log.info("User {} has no assigned sender IDs, skipping sender ID validation", username);
                   return;
                }

                String requestedSenderId = broadcastDTO.getSenderId();

                // checking for both missing and invalid sender ID
                boolean isValidSenderId = Objects.nonNull(requestedSenderId)
                        && !requestedSenderId.trim().isEmpty()
                        && userAssignedSenderIds.stream()
                        .anyMatch(senderId -> senderId.equals(requestedSenderId));

                if (!isValidSenderId) {
                    throw new SmscBackendException("Invalid Sender ID. A valid Sender ID must be selected to proceed");
                }

            });
        }
    }

    private void validateDeletionPermission(Broadcast broadcast) {
        if (!BroadcastStatus.DRAFT.isEqual(broadcast.getStatus())) {
            throw new SmscBackendException(
                    "Cannot delete broadcast with status " + broadcast.getStatus() +
                            ". Only DRAFT campaigns can be deleted for audit compliance.");
        }

        if (UtilsUser.isRoot()) {
            return;
        }

        Integer currentUserId = UtilsUser.getCurrentUserId(userRepository);
        if (currentUserId == null) {
            throw new SmscBackendException("Unable to determine current user");
        }

        if (broadcast.getCreatedById() == null || !broadcast.getCreatedById().equals(currentUserId)) {
            throw new SmscBackendException(
                    "You can only delete broadcast campaigns you created");
        }
    }
}
