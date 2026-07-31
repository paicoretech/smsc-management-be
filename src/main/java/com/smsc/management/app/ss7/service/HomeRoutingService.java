package com.smsc.management.app.ss7.service;

import com.paicbd.smsc.scylla.ScyllaManager;
import com.smsc.management.app.ss7.dto.HomeRoutingCcMccMncDTO;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.mapper.HomeRoutingMapper;
import com.smsc.management.app.ss7.model.entity.HomeRouting;
import com.smsc.management.app.ss7.model.entity.HomeRoutingCcMccMnc;
import com.smsc.management.app.ss7.model.repository.HomeRoutingCcMccMncRepository;
import com.smsc.management.app.ss7.model.repository.HomeRoutingRepository;
import com.smsc.management.app.ss7.utils.ScyllaSyncOperation;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeRoutingService {
    private final HomeRoutingRepository homeRoutingRepo;
    private final HomeRoutingCcMccMncRepository ccMccMncRepo;
    private final HomeRoutingMapper homeRoutingMapper;
    private final ScyllaManager scyllaManager;

    /**
     * Retrieves a HomeRouting configuration by its networkId.
     *
     * @param id The network ID or external ID for which ss7 gateway configuration is being retrieved
     * @return ApiResponse containing the HomeRoutingDTO if found, or an error message.
     */
    public ApiResponse getHomeRouting(Object id) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "No home routing configuration found for external_id= " : "No home routing configuration found for network_id= ";
            HomeRouting homeRouting = searchByExternalId ? homeRoutingRepo.findByExternalId((String) id) : homeRoutingRepo.findByNetworkId((int) id);
            if (homeRouting != null) {
                return ResponseMapping.successMessage("Get HomeRouting successful.", homeRoutingMapper.toDTO(homeRouting));
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
        } catch (Exception e) {
            log.error("Get HomeRouting request error for networkId {}: {}", id, e.getMessage());
            return ResponseMapping.exceptionMessage("HomeRouting was end with error", e);
        }
    }

    /**
     * Creates a new HomeRouting configuration.
     *
     * @param newHomeRouting DTO containing the new HomeRouting data.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse createHomeRouting(HomeRoutingDTO newHomeRouting) {
        try {
            HomeRouting entity = homeRoutingMapper.toEntity(newHomeRouting);
            HomeRouting result = homeRoutingRepo.save(entity);
            return ResponseMapping.successMessage("HomeRouting created successfully.", homeRoutingMapper.toDTO(result));
        } catch (Exception e) {
            log.error("Create HomeRouting request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New HomeRouting was end with error.", e);
        }
    }

    /**
     * Updates an existing HomeRouting configuration.
     *
     * @param id             The ID or external id of the HomeRouting to be updated.
     * @param homeRoutingDTO The homeRoutingDTO object containing the new details of the HomeRouting configuration.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse updateHomeRouting(Object id, HomeRoutingDTO homeRoutingDTO) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "HomeRouting configuration with external id = " : "HomeRouting configuration with id = ";
            HomeRouting currentHomeRouting = searchByExternalId ? homeRoutingRepo.findByExternalId((String) id) : homeRoutingRepo.findById((int) id);
            if (currentHomeRouting != null) {
                currentHomeRouting.setMode(homeRoutingDTO.getMode());
                currentHomeRouting.setTtlCache(homeRoutingDTO.getTtlCache());
                HomeRouting result = homeRoutingRepo.save(currentHomeRouting);

                return ResponseMapping.successMessage("HomeRouting updated successfully.", homeRoutingMapper.toDTO(result));
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found to update");
        } catch (Exception e) {
            log.error("Update HomeRouting request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update HomeRouting was end with error", e);
        }
    }

    /**
     * Deletes an existing HomeRouting configuration by ID.
     *
     * @param id The ID or external id of the HomeRouting to be deleted.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse deleteHomeRouting(Object id) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "HomeRouting configuration with external id = " : "HomeRouting configuration with id = ";
            HomeRouting currentHomeRouting = searchByExternalId ? homeRoutingRepo.findByExternalId((String) id) : homeRoutingRepo.findByNetworkId((int) id);
            if (currentHomeRouting == null) {
                return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found to delete.");
            }

            List<HomeRoutingCcMccMnc> homeRoutingCcMccMncList = ccMccMncRepo.findBySs7HomeRoutingId(currentHomeRouting.getId());
            if (homeRoutingCcMccMncList != null) {
                for (HomeRoutingCcMccMnc homeRoutingCcMccMnc : homeRoutingCcMccMncList) {
                    syncScylla(ScyllaSyncOperation.DELETE, homeRoutingCcMccMnc,null);
                }
                ccMccMncRepo.deleteAll(homeRoutingCcMccMncList);
            }

            homeRoutingRepo.delete(currentHomeRouting);
            return ResponseMapping.successMessage("HomeRouting deleted successfully.", null);
        } catch (Exception e) {
            log.error("Delete HomeRouting request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete HomeRouting was end with error.", e);
        }
    }

    /*
     * -------------- HOME ROUTING CC MCC MNC ------------------------
     */

    /**
     * Retrieves HomeRoutingCcMccMnc entries by HomeRouting ID.
     *
     * @param homeRoutingId The ID of the HomeRouting.
     * @return ApiResponse with a list of HomeRoutingCcMccMncDTO or an error message.
     */
    public ApiResponse getCcMccMncByHomeRoutingId(int homeRoutingId) {
        try {
            List<HomeRoutingCcMccMnc> list = ccMccMncRepo.findBySs7HomeRoutingId(homeRoutingId);
            if (list != null) {
                return ResponseMapping.successMessage("Get HomeRoutingCcMccMnc successful.", homeRoutingMapper.toDTOCcMccMncList(list));
            }
            return ResponseMapping.errorMessageNoFound("No HomeRoutingCcMccMnc found for homeRoutingId = " + homeRoutingId);
        } catch (Exception e) {
            log.error("Get HomeRoutingCcMccMnc request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get HomeRoutingCcMccMnc failed.", e);
        }
    }

    /**
     * Creates a new HomeRoutingCcMccMnc entry.
     *
     * @param newCcMccMnc DTO containing the data to create.
     * @return ApiResponse with the created HomeRoutingCcMccMncDTO or an error message.
     */
    public ApiResponse createCcMccMnc(HomeRoutingCcMccMncDTO newCcMccMnc) {
        try {
            HomeRoutingCcMccMnc entity = homeRoutingMapper.toEntityCcMccMnc(newCcMccMnc);
            HomeRoutingCcMccMnc result = ccMccMncRepo.save(entity);
            syncScylla(ScyllaSyncOperation.CREATE, result, null);
            return ResponseMapping.successMessage("HomeRoutingCcMccMnc created successfully.", homeRoutingMapper.toDTOCcMccMnc(result));
        } catch (DataIntegrityViolationException e) {
            log.error("Create HomeRoutingCcMccMnc constraint violation: {}", e.getMessage());
            return ResponseMapping.exceptionConstrainMessage("New HomeRoutingCcMccMnc was end with error", e);
        } catch (Exception e) {
            log.error("Create HomeRoutingCcMccMnc request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Create HomeRoutingCcMccMnc failed.", e);
        }
    }

    /**
     * Updates an existing HomeRoutingCcMccMnc entry.
     *
     * @param id          The ID of the entry to update.
     * @param ccMccMncDTO DTO containing the updated data.
     * @return ApiResponse with the updated HomeRoutingCcMccMncDTO or an error message.
     */
    public ApiResponse updateCcMccMnc(int id, HomeRoutingCcMccMncDTO ccMccMncDTO) {
        try {
            HomeRoutingCcMccMnc current = ccMccMncRepo.findById(id);
            if (current == null) {
                return ResponseMapping.errorMessageNoFound("HomeRoutingCcMccMnc with id = " + id + " not found to update.");
            }
            HomeRoutingCcMccMnc oldEntity = new HomeRoutingCcMccMnc();
            BeanUtils.copyProperties(current, oldEntity);

            current.setCountryCode(ccMccMncDTO.getCountryCode());
            current.setMccMnc(ccMccMncDTO.getMccMnc());
            current.setSmsc(ccMccMncDTO.getSmsc());
            HomeRoutingCcMccMnc result = ccMccMncRepo.save(current);
            syncScylla(ScyllaSyncOperation.UPDATE, result, oldEntity);
            return ResponseMapping.successMessage("HomeRoutingCcMccMnc updated successfully.", homeRoutingMapper.toDTOCcMccMnc(result));
        } catch (DataIntegrityViolationException e) {
            log.error("Update HomeRoutingCcMccMnc constraint violation: {}", e.getMessage());
            return ResponseMapping.exceptionConstrainMessage("New HomeRoutingCcMccMnc was end with error", e);
        } catch (Exception e) {
            log.error("Update HomeRoutingCcMccMnc request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update HomeRoutingCcMccMnc failed.", e);
        }
    }

    /**
     * Deletes an existing HomeRoutingCcMccMnc entry by ID.
     *
     * @param id The ID of the entry to delete.
     * @return ApiResponse indicating success or failure.
     */
    public ApiResponse deleteCcMccMnc(int id) {
        try {
            HomeRoutingCcMccMnc current = ccMccMncRepo.findById(id);
            if (current != null) {
                syncScylla(ScyllaSyncOperation.DELETE, current, null);
                ccMccMncRepo.delete(current);
                return ResponseMapping.successMessage("HomeRoutingCcMccMnc deleted successfully.", null);
            }
            return ResponseMapping.errorMessageNoFound("HomeRoutingCcMccMnc with id = " + id + " not found to delete.");
        } catch (Exception e) {
            log.error("Delete HomeRoutingCcMccMnc request error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete HomeRoutingCcMccMnc failed.", e);
        }
    }

    private String getSs7NetworkIdStringByHomeRoutingId(int homeRoutingId) {
        HomeRouting hr = homeRoutingRepo.findById(homeRoutingId);
        return (hr != null) ? String.valueOf(hr.getNetworkId()) : null;
    }

    private void syncScylla(ScyllaSyncOperation op, HomeRoutingCcMccMnc entity, HomeRoutingCcMccMnc oldEntity) {
        try {
            String ss7NetworkId = getSs7NetworkIdStringByHomeRoutingId(entity.getSs7HomeRoutingId());
            if (ss7NetworkId == null) {
                log.warn("[ScyllaSync] HomeRouting parent not found (id={}), skipping sync. op={}",
                        entity.getSs7HomeRoutingId(), op);
                return;
            }
            switch (op) {
                case CREATE ->
                        scyllaManager.insertHomeRouting(ss7NetworkId, entity.getCountryCode(), entity.getMccMnc(), entity.getSmsc());
                case UPDATE -> {
                    scyllaManager.deleteHomeRoutingByNetworkCountryAndMcc(ss7NetworkId, oldEntity.getCountryCode(), oldEntity.getMccMnc());
                    scyllaManager.insertHomeRouting(ss7NetworkId, entity.getCountryCode(), entity.getMccMnc(), entity.getSmsc());
                }
                case DELETE ->
                        scyllaManager.deleteHomeRoutingByNetworkCountryAndMcc(ss7NetworkId, entity.getCountryCode(), entity.getMccMnc());
            }
        } catch (Exception ex) {
            log.warn("[ScyllaSync] Error syncing to Scylla. op={}, entityId={}, cause={}",
                    op, entity.getId(), ex.getMessage(), ex);
        }
    }
}
