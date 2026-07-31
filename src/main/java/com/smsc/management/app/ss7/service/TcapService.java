package com.smsc.management.app.ss7.service;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.ss7.dto.TcapDTO;
import com.smsc.management.app.ss7.model.entity.Tcap;
import com.smsc.management.app.ss7.mapper.TcapMapper;
import com.smsc.management.app.ss7.model.repository.TcapRepository;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcapService {

    private final TcapRepository tcapRepository;
    private final TcapMapper tcapMapper;

    /**
     * Retrieves Tcap configuration by network ID.
     *
     * @param id The network ID for which ss7 gateway tcap configuration is being retrieved.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse getTcap(Object id) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "No tcap configuration found for external_id= " : "No tcap configuration found for network_id= ";
            Tcap tcap = searchByExternalId ? tcapRepository.findByExternalId((String) id) : tcapRepository.findByNetworkId((int) id);
            if (tcap != null) {
                return ResponseMapping.successMessage("get tcap request successful.", tcapMapper.toDTO(tcap));
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
        } catch (Exception e) {
            log.error("Tcap request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Tcap was end with error", e);
        }
    }

    /**
     * Creates a new Tcap configuration.
     *
     * @param newTcap The TcapDTO object containing details of the new Tcap configuration.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse create(TcapDTO newTcap) {
        try {
            Tcap tcapEntity = tcapMapper.toEntity(newTcap);

            if (tcapEntity.getInvokeTimeout() > tcapEntity.getDialogIdleTimeout()) {
                return ResponseMapping.errorMessage("invokeTimeOut must be less than or equal to dialogIdleTimeout");
            }
            var result = tcapRepository.save(tcapEntity);
            return ResponseMapping.successMessage("tcap created successful.", tcapMapper.toDTO(result));
        } catch (Exception e) {
            log.error("New Tcap request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New Tcap was end with error", e);
        }
    }

    /**
     * Updates an existing Tcap configuration.
     *
     * @param id   The ID of the Tcap configuration to be updated.
     * @param tcap The TcapDTO object containing the new details of the Tcap configuration.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse update(Object id, TcapDTO tcap) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "tcap configuration with external id = " : "tcap configuration with id = ";
            Tcap currentTcap = searchByExternalId ? tcapRepository.findByExternalId((String) id) : tcapRepository.findById((int) id);
            if (currentTcap != null) {
                tcapMapper.updateEntityFromTCapDTO(tcap, currentTcap);
                Tcap result = tcapRepository.save(currentTcap);

                return ResponseMapping.successMessage("tcap update successful.", tcapMapper.toDTO(result));
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
        } catch (Exception e) {
            log.error("Update Tcap request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update Tcap was end with error", e);
        }
    }

    /**
     * Deletes an existing Tcap configuration by its ID.
     *
     * @param id The ID of the Tcap configuration to be deleted.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse delete(Object id) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ? "tcap configuration with external id = " : "tcap configuration with id = ";
            Tcap tcap = searchByExternalId ? tcapRepository.findByExternalId((String) id) : tcapRepository.findByNetworkId((int) id);
            if (tcap != null) {
                tcapRepository.delete(tcap);
                return ResponseMapping.successMessage("tcap deleted successful.", null);
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
        } catch (Exception e) {
            log.error("Delete Tcap request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete Tcap was end with error", e);
        }
    }
}
