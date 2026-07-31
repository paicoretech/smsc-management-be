package com.smsc.management.app.server.service;

import com.paicbd.smsc.dto.SmppServerConfig;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.server.dto.SmppServerDTO;
import com.smsc.management.app.server.mapper.ServerMapper;
import com.smsc.management.app.server.model.entity.SmppServer;
import com.smsc.management.app.server.model.repository.SmppServerRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.smsc.management.utils.Constants.DEFAULT_SMPP_SERVER_NAME;
import static com.smsc.management.utils.Constants.FORCED_STOPPED_STATUS;
import static com.smsc.management.utils.Constants.DEFAULT_STATUS;
import static com.smsc.management.utils.Constants.DISABLED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.STARTED_STATUS;
import static com.smsc.management.utils.Constants.UPDATE_SMPP_SERVER_LISTENER;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmppServerService {
    private final SmppServerRepository smppServerRepository;
    private final ServerMapper serverMapper;
    private final UtilsBase utilsBase;

    /**
     * Retrieves a list of SMPP (Short Message Peer-to-Peer) servers from the repository
     * and returns them in a Data Transfer Object (DTO) format.
     *
     * @return ApiResponse An {@link ApiResponse} object containing the status of the response
     *         and the SMPP server data, or an error message if an exception occurs.
     *
     */
    public ApiResponse getSmppServers() {
        try {
            var result = this.smppServerRepository.getAllSmppServers();
            return ResponseMapping.successMessage("Get smpp servers request success", serverMapper.toDTO(result));
        } catch (Exception e) {
            log.error("Get smpp servers list with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Smpp servers was end with error while getting data", e);
        }
    }

    /**
     * Creates a new SMPP (Short Message Peer-to-Peer) server based on the provided configuration,
     * saves it to the repository, and stores the server data in Redis for management.
     *
     * @param smppServerConfig The configuration details of the SMPP server to be created.
     *
     * @return ApiResponse An {@link ApiResponse} object containing the status of the request
     *         and the newly created SMPP server in DTO format, or an error message if an exception occurs.
     *
     */
    public ApiResponse createSmppServer(SmppServerDTO smppServerConfig) {
        try {
            smppServerConfig.setEnabled(DISABLED_ENABLED_STATUS);
            smppServerConfig.setStatus(DEFAULT_STATUS);
            var server = this.serverMapper.toEntity(smppServerConfig);
            var result = this.smppServerRepository.saveAndFlush(server);
            return ResponseMapping.successMessage("Get smpp servers request success", serverMapper.toDTO(result));
        } catch (DataIntegrityViolationException e) {
            log.error("Error violation exception for creating smpp server listener: {}", e.getMessage());
            return ResponseMapping.exceptionConstrainMessage("New Smpp Server was end with error", e);
        } catch (Exception e) {
            log.error("Create smpp servers listener with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Create smpp servers listener with error", e);
        }
    }

    /**
     * Updates an existing SMPP (Short Message Peer-to-Peer) server's configuration based on the provided
     * configuration details. If the server exists, it updates the server's information, saves the changes
     * to the repository, and stores the updated server data in Redis. It also triggers a socket notification
     * regarding the server's enabled status change.
     *
     * @param id The ID of the SMPP server to be updated.
     * @param smppServerConfig The new configuration details for the SMPP server.
     *
     * @return ApiResponse An {@link ApiResponse} object containing the status of the request and the updated
     *         SMPP server in DTO format, or an error message if the server is not found or an exception occurs.
     *
     */
    public ApiResponse updateSmppServer(int id, SmppServerDTO smppServerConfig) {
        try {
            this.verifyParameterId(id, smppServerConfig);
            var updateSmppServer = this.smppServerRepository.findById(id);
            if (Objects.isNull(updateSmppServer)) {
                return ResponseMapping.errorMessageNoFound(String.format("Smpp server with id %s was not found.", id));
            }

            if (updateSmppServer.isDefault()) {
                if (!DEFAULT_SMPP_SERVER_NAME.equalsIgnoreCase(smppServerConfig.getName())) {
                    log.info("It is not allowed to update the name of the default Smpp Server");
                    smppServerConfig.setName(DEFAULT_SMPP_SERVER_NAME);
                }
                smppServerConfig.setDefault(true);
            }

            int previousEnabled = updateSmppServer.getEnabled();
            smppServerConfig.setStatus(updateSmppServer.getStatus()); // status is updating from smpp-server-module by websocket
            this.serverMapper.updateEntityFromDTO(smppServerConfig, updateSmppServer);
            var result = this.smppServerRepository.save(updateSmppServer);

            this.managementRedisStore(result);
            this.managementSocketNotification(id, previousEnabled, result.getEnabled());

            return ResponseMapping.successMessage("Smpp server updated successfully.", serverMapper.toDTO(result));
        } catch (DataIntegrityViolationException e) {
            log.error("Error violation exception for updating smpp server listener: {}", e.getMessage());
            return ResponseMapping.exceptionConstrainMessage("New Smpp Server was ended with error", e);
        } catch (Exception e) {
            log.error("Update smpp server with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Smpp server update was ended with error", e);
        }
    }

    /**
     * Deletes an SMPP (Short Message Peer-to-Peer) server by its ID. If the server exists, it deletes the server
     * from the repository, sets its enabled status to 2 (indicating it is disabled or deleted), and updates
     * Redis and triggers a socket notification about the status change.
     *
     * @param id The ID of the SMPP server to be deleted.
     *
     * @return ApiResponse An {@link ApiResponse} object containing the status of the request.
     *         If successful, a message indicating that the SMPP server was deleted.
     *         If the server is not found or an error occurs, an error message is returned.
     *
     */
    public ApiResponse deleteSmppServer(int id) {
        try {
            var currentSmppServer = this.smppServerRepository.findById(id);
            if (Objects.isNull(currentSmppServer)) {
                log.info("Smpp server id {} was not found", id);
                return ResponseMapping.errorMessageNoFound(String.format("Smpp server with id %s was not found.", id));
            }

            if (currentSmppServer.isDefault()) {
                log.info("Smpp server default cannot be deleted.");
                throw new SmscBackendException("Smpp server default cannot be deleted.");
            }

            this.smppServerRepository.delete(currentSmppServer);

            int previousEnabled = currentSmppServer.getEnabled();
            currentSmppServer.setEnabled(2);
            this.managementRedisStore(currentSmppServer);
            this.managementSocketNotification(id, previousEnabled, 2);

            return ResponseMapping.successMessage("Smpp server deleted successfully.", null);
        } catch (Exception e) {
            log.error("Delete Smpp server was end with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Smpp server was end with error", e);
        }
    }

    /**
     * Updates the status of an SMPP server based on the provided status.
     * This method looks up the SMPP server in the database using the provided identifier.
     * If the server is found, its status is updated in the database. If the provided status
     * is a "failed" status, additional management tasks are performed, such as updating data
     * in a Redis cache and sending a socket notification. If any error occurs during the process,
     * the exception is caught and logged.
     *
     * @param id The identifier of the SMPP server to be updated.
     * @param status The new status to be assigned to the SMPP server.
     * @throws SmscBackendException If the SMPP server with the specified identifier is not found.
     */
    public void updatingStatusFromSmppServerCors(int id, String status, String actionStatus) {
        try {
            var smppServer = this.smppServerRepository.findById(id);
            if (Objects.isNull(smppServer)) {
                throw new SmscBackendException("smpp server with id " + id + " was not found.");
            }

            int enabled = smppServer.getEnabled();
            if (DEFAULT_STATUS.equalsIgnoreCase(status)) {
                enabled = DISABLED_ENABLED_STATUS;
            }

            // updating database
            this.smppServerRepository.updateStatus(id, status, actionStatus, enabled);
            log.info("Status '{}' for smpp server id {} was updated successfully in database (enabled={})", status, id, enabled);
            smppServer.setStatus(status);
            smppServer.setActionStatus(actionStatus);
            smppServer.setEnabled(enabled);
            this.managementRedisStore(smppServer);
            log.info("Status '{}' for smpp server id {} was updated successfully in Redis", status, id);

        } catch (Exception e) {
            log.error("Updating status error: {}", e.getMessage(), e);
        }
    }

    /**
     * Stores the configuration details of an SMPP (Short Message Peer-to-Peer) server in Redis.
     * The method converts the SMPP server entity into a DTO (Data Transfer Object) and stores
     * the resulting configuration in Redis under a specified hash table.
     *
     * @param smppServer The SMPP server whose configuration is to be stored in Redis.
     *
     */
    private void managementRedisStore(SmppServer smppServer) {
        SmppServerConfig smppServerConfig = serverMapper.toRedisDTOFromEntity(smppServer);
        utilsBase.storeInRedis(GeneralSmscConstants.SMPP_SERVER_CONFIGURATIONS_HASH_NAME, String.valueOf(smppServerConfig.getId()), smppServerConfig.toString());
    }

    /**
     * Sends a socket notification when the enabled status of an SMPP (Short Message Peer-to-Peer) server is updated.
     * If the server's enabled status has changed (i.e., previous and new values are different),
     * a notification is sent to inform listeners of the update.
     *
     * @param id The ID of the SMPP server whose status has changed.
     * @param previousEnabled The previous enabled status of the SMPP server.
     * @param newEnabled The new enabled status of the SMPP server.
     *
     */
    private void managementSocketNotification(int id, int previousEnabled, int newEnabled) {
        if (previousEnabled != newEnabled) {
            utilsBase.sendNotificationSocket(
                    UPDATE_SMPP_SERVER_LISTENER,
                    String.valueOf(id)
            );
        }
    }

    /**
     * Verifies that the provided ID matches the expected ID from the SMPP server
     * (in the payload). If the IDs do not match, an exception is thrown.
     *
     * @param id The ID provided that should match the SMPP server's ID.
     * @param smppServer The SMPP server containing the ID that is to be compared.
     * @throws SmscBackendException If the provided ID does not match the SMPP server's ID.
     */
    private void verifyParameterId(int id, SmppServerDTO smppServer) {
        if (id != smppServer.getId()) {
            throw new SmscBackendException("The provided ID does not match the ID in the received payload.");
        }
    }
}
