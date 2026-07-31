package com.smsc.management.app.dnd.service;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.component.DndActivationTask;
import com.smsc.management.app.dnd.component.DndData;
import com.smsc.management.app.dnd.component.DndFileTask;
import com.smsc.management.app.dnd.dto.DndEntryListsResponseDTO;
import com.smsc.management.app.dnd.dto.DndEntryMsisdnDTO;
import com.smsc.management.app.dnd.dto.DndEntryMsisdnFilterDataDTO;
import com.smsc.management.app.dnd.dto.DndRequestDTO;
import com.smsc.management.app.dnd.mapper.DndMapper;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.entity.DndEntryMsisdn;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.processor.DndFileProcessor;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.ResponseMapping;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DndService {

    private final DndFileTask dndFileTask;
    private final SequenceNetworksIdRepository sequenceNetworksIdRepository;
    private final DndEntryListRepository dndEntryListRepository;
    private final DndEntryMsisdnRepository dndEntryMsisdnRepository;
    private final DndMapper dndMapper;
    private final List<DndFileProcessor> dndFileProcessor;
    private final DndScyllaService dndScyllaService;
    private final DndData dndData;
    private final DndActivationTask dndActivationTask;
    private final AppProperties appProperties;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public ApiResponse getAll() {
        try {
            List<DndEntryList> dndList = dndEntryListRepository.findAllByOrderByIdDesc();
            List<DndEntryListsResponseDTO> responseList = dndMapper.toDtoList(dndList);
            return ResponseMapping.successMessage("DND names retrieved successfully.", responseList);
        } catch (Exception e) {
            log.error("Error retrieving DND names: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("An error occurred while retrieving DND names.", e);
        }
    }

    public ApiResponse getDndEntries(Map<String, Object> filters) {
        try {
            Object parentIdObj = filters.get("parent_id");
            if (parentIdObj == null) {
                return ResponseMapping.errorMessage("parent_id parameter is required");
            }
            int parentId = this.validateParentId(parentIdObj);

            boolean parentExists = dndEntryListRepository.existsById(parentId);
            if (!parentExists) {
                return ResponseMapping.errorMessageNoFound("DND list with id " + parentId + " was not found");
            }
            DndEntryMsisdnFilterDataDTO result = dndData.filterDndEntries(filters);
            return ResponseMapping.successMessage("DND entries retrieved successfully", result);
        } catch (Exception e) {
            log.error("Error retrieving DND entries: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error retrieving DND entries", e);
        }
    }

    public ApiResponse saveDndFile(DndRequestDTO dto, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseMapping.errorMessage("File is required");
            }

            final String originalName = file.getOriginalFilename();
            boolean supported = dndFileProcessor.stream()
                    .anyMatch(p -> p.supports(originalName));
            if (!supported) {
                return ResponseMapping.errorMessage("Unsupported file format: " + originalName);
            }

            if (DndType.NETWORK_ID.equals(dto.getDndType())) {
                validateNetworkId(dto.getDndValue());
            }
            if (dndEntryListRepository.existsByName(dto.getName())) {
                return ResponseMapping.errorMessage("A DND list with this name already exists.");
            }

            DndEntryList dndEntryList = dndMapper.toEntity(dto);
            dndEntryList.setStatus(DndStatus.CREATING);
            dndEntryList.setComment(null);
            DndEntryList saved = dndEntryListRepository.save(dndEntryList);

            String uniqueFilename = UUID.randomUUID() + "_" + originalName;
            String filePath = appProperties.getUploadFileDir() + uniqueFilename;
            file.transferTo(new File(filePath));
            dndFileTask.startProcessing(uniqueFilename, saved);

            DndEntryListsResponseDTO responseDTO = dndMapper.toDTO(saved);
            return ResponseMapping.successMessage("DND name saved successfully.", responseDTO);

        } catch (DataIntegrityViolationException e) {
            return ResponseMapping.errorMessage("A DND list with this name already exists.");
        } catch (Exception e) {
            return ResponseMapping.exceptionMessage("An error occurred while saving the DND name.", e);
        }
    }

    private void validateNetworkId(String dndValue) {
        try {
            int networkId = Integer.parseInt(dndValue);

            if (!sequenceNetworksIdRepository.existsById(networkId)) {
                throw new SmscBackendException("Invalid NETWORK_ID: " + networkId);
            }

        } catch (NumberFormatException e) {
            throw new SmscBackendException("DND Value must be a numeric NETWORK_ID", e);
        }
    }

    @Transactional
    public ApiResponse saveDndEntry(DndEntryMsisdnDTO dto) {
        try {
            Integer parentId = dto.getParentId();
            List<String> msisdns = dto.getMsisdns();

            if (msisdns == null || msisdns.isEmpty()) {
                return ResponseMapping.errorMessage("MSISDN list cannot be empty.");
            }
            DndEntryList parent = validateAndGetParent(dto.getParentId());
            if (parent.getStatus() != DndStatus.ACTIVE) {
                return ResponseMapping.errorMessageNoFound("Cannot add MSISDNs to an inactive DND list.");
            }
            boolean hadDuplicate = false;
            int inserted = 0;
            for (String m : msisdns) {
                if (m == null) continue;
                String msisdn = m.trim();
                if (msisdn.isEmpty()) continue;

                if (dndEntryMsisdnRepository.existsByMsisdnAndParentId(msisdn, parentId)) {
                    hadDuplicate = true;
                    log.warn("Skipping duplicate MSISDN: {}, ID : {}", msisdn, parentId);
                    continue;
                }

                DndEntryMsisdn entry = new DndEntryMsisdn();
                entry.setMsisdn(msisdn);
                entry.setParentId(parentId);
                dndEntryMsisdnRepository.save(entry);

                dndScyllaService.insertDndEntry(
                        parentId,
                        parent.getDndValue(),
                        parent.getDndType().name(),
                        msisdn
                );
                inserted++;
            }
            Map<String, Object> data = Map.of("had_duplicate", hadDuplicate, "inserted", inserted);
            return ResponseMapping.successMessage("DND entry saved successfully.", data);
        } catch (Exception e) {
            log.error("Error saving DND entry: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while saving the DND entry.", e);
        }
    }

    @Transactional
    public ApiResponse changeStatus(Integer parentId, boolean enable) {
        try {
            DndEntryList entry = validateAndGetParent(parentId);

            if (enable) {
                entry.setStatus(DndStatus.ACTIVATING);
                dndEntryListRepository.save(entry);

                executor.submit(() -> {
                    try {
                        dndActivationTask.startDndActivationSync(parentId);
                    } catch (Exception e) {
                        log.error("Error updating DND list async for parentId {}: {}", parentId, e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                });

                return ResponseMapping.successMessage("DND activation started. Status set to ACTIVATING.", null);

            } else {
                entry.setStatus(DndStatus.DISABLED);
                dndEntryListRepository.save(entry);
                dndScyllaService.deleteDndEntriesByParentId(parentId);
                return ResponseMapping.successMessage("DND entry list status changed to DISABLED.", null);
            }

        } catch (SmscBackendException e) {
            return ResponseMapping.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Error changing DND entry list status: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while changing DND entry list status.", e);
        }
    }

    @Transactional
    public ApiResponse deleteDndEntryList(Integer parentId) {
        try {
            validateAndGetParent(parentId);
            dndEntryMsisdnRepository.deleteAllByParentId(parentId);
            dndScyllaService.deleteDndEntriesByParentId(parentId);
            dndEntryListRepository.deleteById(parentId);
            return ResponseMapping.successMessage("DND entry list deleted successfully.", null);
        } catch (SmscBackendException e) {
            return ResponseMapping.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting DND entry list: {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while deleting the DND entry list.", e);
        }
    }

    @Transactional
    public ApiResponse renameList(Integer parentId, String newName) {
        try {
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseMapping.errorMessage("Name cannot be empty.");
            }
            DndEntryList entry = validateAndGetParent(parentId);
            if (entry.getStatus() != DndStatus.ACTIVE) {
                return ResponseMapping.errorMessage("Rename is allowed only when status is ACTIVE.");
            }
            String trimmed = newName.trim();
            if (!trimmed.equalsIgnoreCase(entry.getName()) && dndEntryListRepository.existsByName(trimmed)) {
                return ResponseMapping.errorMessage("Duplicated DND list name.");
            }
            entry.setName(trimmed);
            dndEntryListRepository.save(entry);
            return ResponseMapping.successMessage("DND name updated successfully.", null);
        } catch (Exception e) {
            log.error("Error renaming DND list {}: {}", parentId, e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while renaming the DND list.", e);
        }
    }

    @Transactional
    public ApiResponse deleteSingleMsisdn(Integer parentId, String msisdn) {
        try {
            if (msisdn == null || msisdn.trim().isEmpty()) {
                return ResponseMapping.errorMessage("MSISDN cannot be empty.");
            }
            DndEntryList entry = validateAndGetParent(parentId);
            if (entry.getStatus() != DndStatus.ACTIVE) {
                return ResponseMapping.errorMessage("Deleting MSISDN is allowed only when status is ACTIVE.");
            }
            int pgDeleted = dndEntryMsisdnRepository.deleteByParentIdAndMsisdn(parentId, msisdn);
            if (pgDeleted == 0) {
                log.warn("MSISDN {} not found in Postgres for parentId {}", msisdn, parentId);
            }
            dndScyllaService.deleteSingleDndEntry(entry.getDndType().name(), msisdn);
            return ResponseMapping.successMessage("MSISDN deleted successfully.", null);
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException stale) {
            log.warn("Stale delete for parentId {}, msisdn {} (already gone). Returning success.", parentId, msisdn);
            return ResponseMapping.successMessage("MSISDN deleted successfully.", null);
        } catch (Exception e) {
            log.error("Error deleting MSISDN {} for parentId {}: {}", msisdn, parentId, e.getMessage(), e);
            return ResponseMapping.exceptionMessage("An error occurred while deleting the MSISDN.", e);
        }
    }

    private DndEntryList validateAndGetParent(Integer parentId) {
        return dndEntryListRepository.findById(parentId)
                .orElseThrow(() -> new SmscBackendException("Parent DND name with ID " + parentId + " does not exist."));
    }

    private int validateParentId(Object parentIdObj) {
        try {
            return Integer.parseInt(parentIdObj.toString());
        } catch (NumberFormatException e) {
            throw new SmscBackendException("parent_id must be a valid integer");
        }
    }
}
