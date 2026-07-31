package com.smsc.management.app.settings.service;


import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.paicbd.smsc.utils.Generated;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.settings.dto.ServerPropertiesDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.paicbd.smsc.utils.RedisManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.smsc.management.app.settings.service.StatusHandlerService.Status.STOPPED;
import static com.smsc.management.app.settings.service.StatusHandlerService.UpdateParam.SESSIONS;
import static com.smsc.management.app.settings.service.StatusHandlerService.UpdateParam.STATUS;
import static com.smsc.management.app.settings.service.StatusHandlerService.Status.FORCE_STOPPED;
import static com.smsc.management.utils.Constants.UPDATE_HTTP_SERVER_HANDLER_ENDPOINT;

/**
 * Service responsible for handling status updates for service providers, gateways,
 * and servers.
 */
@Generated // Waiting for networkId handling implementation
@Service
@RequiredArgsConstructor
@Slf4j(topic = "StatusHandlerService")
public class StatusHandlerService {
    private final ServiceProviderRepository spRepository;
    private final GatewaysRepository gatewaysRepository;
    private final RedisManager redisManager;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UtilsBase utilsBase;

    enum Status {
        STOPPED,
        STARTED,
        BINDING,
        BOUND,
        UNBINDING,
        FORCE_STOPPED
    }

    enum UpdateParam {
        STATUS,
        SESSIONS
    }

    /**
     * Updates the status of a service provider in the database.
     *
     * @param networkId The system ID of the service provider
     * @param param The parameter to update
     * @param value The new value
     */
    public void updateSpStatusOnDatabase(int networkId, String param, String value) {
        log.info("Handling service provider status: {}", networkId);
        ServiceProvider serviceProvider = findServiceProvider(networkId);
        if (serviceProvider == null) {
            return;
        }

        try {
            if (STATUS.toString().equals(param.toUpperCase())) {
                // Handle STATUS updates
                if (FORCE_STOPPED.toString().equals(value.toUpperCase())) {
                    serviceProvider.setStatus(value.toUpperCase());
                    serviceProvider.setEnabled(1);
                    serviceProvider.setActiveSessionsNumbers(0);
                    spRepository.save(serviceProvider);
                } else if (STOPPED.toString().equals(value.toUpperCase())) {
                    spRepository.updateActiveSessionToZeroAndEnabledToZero(serviceProvider.getNetworkId());
                    spRepository.saveStatusSp(serviceProvider.getNetworkId(), value.toUpperCase());
                } else {
                    spRepository.saveStatusSp(serviceProvider.getNetworkId(), value.toUpperCase());
                }
            } else if (SESSIONS.toString().equals(param.toUpperCase())) {
                // Handle SESSIONS updates
                spRepository.saveSessionsSp(serviceProvider.getNetworkId(), Integer.parseInt(value));
                log.info("Service Provider {} active sessions updated to {}", networkId, value);
            } else {
                log.error("Invalid param: {}", param);
            }

            log.info("Service Provider {} status updated", networkId);
        } catch (IllegalArgumentException e) {
            log.error("An error has occurred on update: {}", e.getMessage());
        }
    }

    /**
     * Updates the status of a gateway in the database.
     *
     * @param networkId The system ID of the gateway
     * @param param The parameter to update
     * @param value The new value
     */
    public void updateGwStatusOnDatabase(int networkId, String param, String value) {
        log.info("Handling gateway status: {}", networkId);
        Gateways gateway = findGateway(networkId);
        if (gateway == null) {
            return;
        }

        try {
            if (STATUS.toString().equals(param.toUpperCase())) {
                if (STOPPED.toString().equals(value.toUpperCase())) {
                    gatewaysRepository.updateGatewayToStatusStoppedEnabledStoppedAndSessionsZero(gateway.getNetworkId());
                }
                gatewaysRepository.saveStatusGateway(gateway.getNetworkId(), value.toUpperCase());
                log.info("Gateway {} status updated", networkId);
            } else if (SESSIONS.toString().equals(param.toUpperCase())) {
                gatewaysRepository.saveSessionsGateway(gateway.getNetworkId(), Integer.parseInt(value));
            } else {
                log.error("Invalid param: {}", param);
            }
        } catch (IllegalArgumentException e) {
            log.error("An error has occurred: {}", e.getMessage());
        }
    }

    /**
     * Finds a service provider by its system ID.
     *
     * @param networkId The system ID of the service provider to find
     * @return The service provider if found, null otherwise
     */
    private ServiceProvider findServiceProvider(int networkId) {
        ServiceProvider serviceProvider = spRepository.findByNetworkId(networkId);
        if (serviceProvider == null) {
            log.error("Service provider with network id {} not found", networkId);
        }
        return serviceProvider;
    }

    /**
     * Finds a gateway by its system ID.
     *
     * @param networkId The system ID of the gateway to find
     * @return The gateway if found, null otherwise
     */
    private Gateways findGateway(int networkId) {
        Gateways gateway = gatewaysRepository.findByNetworkIdAndEnabledNot(networkId, 2); // Enabled = 2 means that the gateway is not deleted
        if (gateway == null) {
            log.error("Gateway with network id {} not found", networkId);
        }
        return gateway;
    }

    /**
     * Updates the status of an HTTP server in the Redis.
     *
     * @param applicationName The name of the HTTP server application.
     * @param status          The new status to set for the HTTP server.
     * @return A ResponseDTO indicating the result of the status update operation.
     * If successful, returns a success message along with the updated server properties.
     * If the instance is not found in Redis, returns an error message indicating the absence.
     * If an exception occurs during the update process, returns an exception message.
     */
    public ApiResponse updateStatusServerHttp(String applicationName, String status) {
        try {
            String redisDataStr = utilsBase.getInRedis(GeneralSmscConstants.CONFIGURATIONS_HASH_NAME, applicationName);
            if (redisDataStr == null) {
                return ResponseMapping.errorMessageNoFound("Instance " + applicationName + " was not found");
            }

            // update server status
            ServerPropertiesDTO httpServerPropertiesDTO = Converter.stringToObject(redisDataStr, ServerPropertiesDTO.class);
            httpServerPropertiesDTO.setState(status);
            redisManager.hset(GeneralSmscConstants.CONFIGURATIONS_HASH_NAME, applicationName, Converter.valueAsString(httpServerPropertiesDTO));

            // notify cors http per socket.
            simpMessagingTemplate.convertAndSend(UPDATE_HTTP_SERVER_HANDLER_ENDPOINT, httpServerPropertiesDTO.getName());

            return ResponseMapping.successMessage("successful state change", httpServerPropertiesDTO);
        } catch (Exception e) {
            log.error("Error to update status http server {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error to update status http server", e);
        }
    }

    /**
     * Updates the status of all HTTP servers in Redis.
     *
     * @param newStatus The new status to set for all HTTP servers.
     * @return A {@link ApiResponse} indicating the result of the operation.
     */
    public ApiResponse updateStatusAllServerHttp(String newStatus) {
        try {
            // Retrieve all HTTP servers from Redis
            Map<String, String> mapHttpServers = utilsBase.getAllInRedis(GeneralSmscConstants.CONFIGURATIONS_HASH_NAME);

            // update server status
            int instancesUpdates = 0;
            for (Map.Entry<String, String> entry : mapHttpServers.entrySet()) {
                ServerPropertiesDTO httpServerPropertiesDTO = Converter.stringToObject(entry.getValue(), ServerPropertiesDTO.class);
                if (httpServerPropertiesDTO.getProtocol() != null && httpServerPropertiesDTO.getProtocol().equalsIgnoreCase("http")) {
                    httpServerPropertiesDTO.setState(newStatus);
                    redisManager.hset(GeneralSmscConstants.CONFIGURATIONS_HASH_NAME, httpServerPropertiesDTO.getName(), Converter.valueAsString(httpServerPropertiesDTO));
                    simpMessagingTemplate.convertAndSend(UPDATE_HTTP_SERVER_HANDLER_ENDPOINT, httpServerPropertiesDTO.getName());
                    instancesUpdates++;
                }
            }

            if (instancesUpdates == 0) {
                return ResponseMapping.errorMessageNoFound("Instances HTTP were not found");
            }

            return ResponseMapping.successMessage("Updated to the new " + newStatus + " state at " + instancesUpdates + " HTTP instances successfully.", null);
        } catch (Exception e) {
            log.error("Error to update status to all http server -> {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error to update status to all http server", e);
        }
    }

    /**
     * Retrieves information about all HTTP servers stored in Redis.
     *
     * @return A ResponseDTO containing information about all HTTP servers.
     * If HTTP server instances are found in Redis, returns a success message
     * along with the list of HTTP server properties.
     * If no HTTP server instances are found in Redis, returns an error message.
     * If an exception occurs during the retrieval process, returns an exception message.
     */
    public ApiResponse getAllHttpServers() {
        try {
            Map<String, String> mapHttpServers = utilsBase.getAllInRedis(GeneralSmscConstants.CONFIGURATIONS_HASH_NAME);
            List<ServerPropertiesDTO> listHttpServers = new ArrayList<>();
            for (Map.Entry<String, String> entry : mapHttpServers.entrySet()) {
                ServerPropertiesDTO resultDTO = Converter.stringToObject(entry.getValue(), ServerPropertiesDTO.class);
                if (resultDTO.getProtocol() != null && resultDTO.getProtocol().equalsIgnoreCase("http")) {
                    listHttpServers.add(resultDTO);
                }
            }

            if (listHttpServers.isEmpty()) {
                return ResponseMapping.errorMessageNoFound("Instances HTTP were not found");
            }

            return ResponseMapping.successMessage("Get HTTP Servers successfully.", listHttpServers);
        } catch (Exception e) {
            log.error("Error on get HTTP Servers request -> {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("get HTTP Servers request with error", e);
        }
    }
}

