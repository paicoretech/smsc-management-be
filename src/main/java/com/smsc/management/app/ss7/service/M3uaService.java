package com.smsc.management.app.ss7.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.UtilsBase;
import org.springframework.stereotype.Service;

import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaAppServersRoutes;
import com.smsc.management.app.ss7.model.entity.M3uaApplicationServer;
import com.smsc.management.app.ss7.model.entity.M3uaAssAppServers;
import com.smsc.management.app.ss7.model.entity.M3uaAssociations;
import com.smsc.management.app.ss7.model.entity.M3uaRoutes;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import com.smsc.management.app.ss7.mapper.M3uaMapper;
import com.smsc.management.app.ss7.model.repository.M3uaAppServersRouteRepository;
import com.smsc.management.app.ss7.model.repository.M3uaApplicationServerRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssAppServersRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssociationsRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRoutesRepository;
import com.smsc.management.app.ss7.model.repository.M3uaSocketsRepository;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.Constants;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.smsc.management.utils.Constants.ACTIVE_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.START_SS7_ASSOCIATION_ENDPOINT;
import static com.smsc.management.utils.Constants.START_SS7_SOCKET_ENDPOINT;
import static com.smsc.management.utils.Constants.STOP_SS7_ASSOCIATION_ENDPOINT;
import static com.smsc.management.utils.Constants.STOP_SS7_SOCKET_ENDPOINT;

@Slf4j
@Service
@RequiredArgsConstructor
public class M3uaService {
	private final M3uaRepository m3uaRepo;
	private final M3uaSocketsRepository m3uaServerRepo;
	private final M3uaAssociationsRepository m3uaAssociationsRepo;
	private final M3uaApplicationServerRepository m3uaAppServerRepo;
	private final M3uaAssAppServersRepository m3uaAssAppServerRepo;
	private final M3uaRoutesRepository m3uaRouteRepo;
	private final M3uaAppServersRouteRepository m3uaAppServerRouteRepo;
	private final M3uaMapper m3uaMapper;
	private final ObjectSs7Service processObjSs7;
    private final Ss7GatewaysRepository ss7GatewaysRepository;
    private final UtilsBase utilsBase;
	
	/**
	 * Retrieves gateway configuration by network ID.
	 * 
	 * @param id The network ID or external ID for which ss7 gateway configuration is being retrieved.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse getM3ua(Object id) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "No m3ua configuration found for external_id= " : "No m3ua configuration found for network_id= ";
			M3ua m3ua = searchByExternalId ? m3uaRepo.findByExternalId((String) id) : m3uaRepo.findByNetworkId((int) id);
			if (m3ua != null) {
				return ResponseMapping.successMessage("get m3ua request successful.", m3uaMapper.toDTO(m3ua));
			}
			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
		} catch (Exception e) {
			log.error("M3ua request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("M3ua was end with error", e);
		}
	}

	/**
	 * Creates a new M3ua configuration.
	 * 
	 * @param newM3ua The M3uaDTO object containing details of the new M3UA configuration.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse create(M3uaDTO newM3ua) {
		try {
			M3ua m3uaEntity = m3uaMapper.toEntity(newM3ua);
			var result = m3uaRepo.save(m3uaEntity);
			return ResponseMapping.successMessage("m3ua created successful.", m3uaMapper.toDTO(result));
		} catch (Exception e) {
			log.error("New M3ua request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New M3ua was end with error", e);
		}
	}

	/**
	 * Updates an existing M3UA configuration.
	 *
	 * @param id   The ID or external id of the M3UA configuration to be updated.
	 * @param m3ua The M3uaDTO object containing the new details of the M3UA configuration.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse update(Object id, M3uaDTO m3ua) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "m3ua configuration with external id = " : "m3ua configuration with id = ";
			M3ua currentM3ua = searchByExternalId ? m3uaRepo.findByExternalId((String) id) : m3uaRepo.findById((int) id);
			if (currentM3ua != null) {
				currentM3ua = m3uaMapper.toEntity(m3ua);
				M3ua result = m3uaRepo.save(currentM3ua);

				return ResponseMapping.successMessage("m3ua update successful.", m3uaMapper.toDTO(result));
			}
			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found to update");
		} catch (Exception e) {
			log.error("Update M3ua request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Update M3ua was end with error", e);
		}
	}

	/**
	 * Deletes an existing M3UA configuration by its ID.
	 *
	 * @param id The ID or external id of the M3UA configuration to be deleted.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse delete(Object id) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "m3ua configuration with external id = " : "m3ua configuration with id = ";
			M3ua m3ua = searchByExternalId ? m3uaRepo.findByExternalId((String) id) : m3uaRepo.findByNetworkId((int) id);
			if (m3ua != null) {
				m3uaRepo.delete(m3ua);
				return ResponseMapping.successMessage("m3ua deleted successful.", null);
			}
			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found to delete.");
		} catch (Exception e) {
			log.error("Delete M3ua request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delete M3ua was end with error", e);
		}
	}
	
	/*
	 * -------------- SERVERS ------------------------
	 */
	
	/**
	 * Retrieves M3ua servers by M3ua ID.
	 * 
	 * @param m3uaId The M3UA ID for which M3UA servers are being retrieved.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse getM3uaServers(int m3uaId) {
		try {
			List<M3uaSockets> m3uaServers = m3uaServerRepo.findBySs7M3uaId(m3uaId);
			if (m3uaServers != null) {
				return ResponseMapping.successMessage("get m3ua servers successful.", m3uaMapper.toDTOServerList(m3uaServers));
			}
			return ResponseMapping.errorMessageNoFound("m3ua servers with m3ua_id = " + m3uaId + " was not found to get servers.");
		} catch (Exception e) {
			log.error("M3ua Servers request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("M3ua servers was end with error", e);
        }
    }

    /**
     * Creates a new M3UA server.
     *
     * @param newM3uaServer The M3uaServersDTO object containing details of the new M3UA server.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse createM3uaServers(M3uaSocketsDTO newM3uaServer) {
        try {
            newM3uaServer.setEnabled(0); // default STOPPED
            newM3uaServer.setState(utilsBase.findStatusByEnabled(newM3uaServer.getEnabled()));
            M3uaSockets newM3uaServerEntity = m3uaMapper.toEntityServer(newM3uaServer);
            var result = m3uaServerRepo.save(newM3uaServerEntity);

            return ResponseMapping.successMessage("New m3ua server successful.", m3uaMapper.toDTOServer(result));
        } catch (Exception e) {
            log.error("New M3ua Servers request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New M3ua servers was end with error", e);
        }
    }

    /**
     * Updates an existing M3UA server.
     *
     * @param id         The ID of the M3UA server to be updated.
     * @param m3uaServer The M3uaServersDTO object containing the new details of the M3UA server.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse updateM3uaServers(int id, M3uaSocketsDTO m3uaServer) {
        try {
            M3uaSockets currentM3uaServer = m3uaServerRepo.findById(id);
            if (currentM3uaServer != null) {
                this.allowSocketUpdates(currentM3uaServer, m3uaServer.getEnabled());
                int previousEnableServer = currentM3uaServer.getEnabled();
                m3uaServer.setState(utilsBase.findStatusByEnabled(m3uaServer.getEnabled()));

                m3uaMapper.updateEntityFromServersDTO(m3uaServer, currentM3uaServer);
                var result = m3uaServerRepo.save(currentM3uaServer);

                // refreshing setting
                if (previousEnableServer != result.getEnabled()) {
                    this.refreshRedisObjectByUpdateM3uaServers(id, result, previousEnableServer);
                }

                return ResponseMapping.successMessage("m3ua server updated successful.", m3uaMapper.toDTOServer(result));
            }

            return ResponseMapping.errorMessageNoFound("m3ua server with id = " + id + " was not found to update server.");
        } catch (Exception e) {
            log.error("Update M3ua Servers request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update M3ua servers was end with error", e);
        }
    }

    /**
     * Deletes an existing M3UA server by its ID.
     *
     * @param id The ID of the M3UA server to be deleted.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse deleteM3uaServers(int id) {
        try {
            M3uaSockets currentM3uaServer = m3uaServerRepo.findById(id);
            if (currentM3uaServer != null) {
                this.allowSocketUpdates(currentM3uaServer, currentM3uaServer.getEnabled());
                m3uaServerRepo.delete(currentM3uaServer);
                return ResponseMapping.successMessage("m3ua server deleted successful.", null);
            }

            return ResponseMapping.errorMessageNoFound("m3ua server with id = " + id + " was not found to delete server.");
        } catch (Exception e) {
            log.error("Delete M3ua Servers request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete M3ua servers was end with error", e);
        }
    }

    /*
     * -------------- ASSOCIATIONS ------------------------
     */

    /**
     * Retrieves M3ua associations by M3ua server ID.
     *
     * @param m3uaId The M3UA id for which M3UA associations are being retrieved.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse getM3uaAssociations(int m3uaId) {
        try {
            List<M3uaAssociationsDTO> m3uaAssociations = m3uaAssociationsRepo.fetchM3uaAssociations(m3uaId);
            if (m3uaAssociations != null) {
                return ResponseMapping.successMessage("get m3ua associations successful.", m3uaAssociations);
            }

            return ResponseMapping.errorMessageNoFound("m3ua associations with m3ua_id = " + m3uaId + " was not found to get associations.");
        } catch (Exception e) {
            log.error("Get M3ua associations request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get M3ua associations was end with error", e);
        }
    }

    /**
     * Creates a new M3UA association.
     *
     * @param newM3uaAssociation The M3uaAssociationsDTO object containing details of the new M3UA association.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse createM3uaAssociations(M3uaAssociationsDTO newM3uaAssociation) {
        try {
            newM3uaAssociation.setEnabled(0); // default STOPPED
            newM3uaAssociation.setState(utilsBase.findStatusByEnabled(newM3uaAssociation.getEnabled()));
            M3uaAssociations m3uaAssociation = m3uaMapper.toEntityAssociation(newM3uaAssociation);
            this.createAspName(m3uaAssociation);
            var result = m3uaAssociationsRepo.save(m3uaAssociation);

            return ResponseMapping.successMessage("New m3ua associations successful.", m3uaMapper.toDTOAssociation(result));
        } catch (Exception e) {
            log.error("New M3ua associations request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New M3ua associations was end with error", e);
        }
    }

    /**
     * Updates an existing M3UA association.
     *
     * @param id              The ID of the M3UA association to be updated.
     * @param m3uaAssociation The M3uaAssociationsDTO object containing the new details of the M3UA association.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse updateM3uaAssociations(int id, M3uaAssociationsDTO m3uaAssociation) {
        try {
            M3uaAssociations currentM3uaAssociation = m3uaAssociationsRepo.findById(id);
            if (currentM3uaAssociation != null) {
                this.allowAssociationUpdates(currentM3uaAssociation, m3uaAssociation.getEnabled());
                int previousEnableAssociation = currentM3uaAssociation.getEnabled();
                m3uaAssociation.setState(utilsBase.findStatusByEnabled(m3uaAssociation.getEnabled()));
                m3uaMapper.updateEntityFromAssociationsDTO(m3uaAssociation, currentM3uaAssociation);
                this.createAspName(currentM3uaAssociation);
                var result = m3uaAssociationsRepo.save(currentM3uaAssociation);

                // refreshing setting
                if (previousEnableAssociation != result.getEnabled()) {
                    this.refreshRedisObjectByUpdateM3uaAssociations(id, result, previousEnableAssociation);
                }

                return ResponseMapping.successMessage("m3ua associations updated successful.", m3uaMapper.toDTOAssociation(result));
            }
            return ResponseMapping.errorMessageNoFound("m3ua associations with id = " + id + " was not found to update associations.");
        } catch (Exception e) {
            log.error("Update M3ua associations request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update M3ua associations was end with error", e);
        }
    }

    /**
     * Deletes an existing M3UA association by its ID.
     *
     * @param id The ID of the M3UA association to be deleted.
     * @return A ResponseDTO object containing the request response.
     */
    public ApiResponse deleteM3uaAssociations(int id) {
        try {
            M3uaAssociations m3uaAssociation = m3uaAssociationsRepo.findById(id);
            if (m3uaAssociation != null) {
                this.allowAssociationUpdates(m3uaAssociation, m3uaAssociation.getEnabled());
                m3uaAssociationsRepo.delete(m3uaAssociation);
                return ResponseMapping.successMessage("m3ua association deleted successful.", null);
            }

            return ResponseMapping.errorMessageNoFound("m3ua association with id = " + id + " was not found to delete associations.");
        } catch (Exception e) {
            log.error("Delete M3ua associations request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete M3ua associations was end with error", e);
        }
    }

    public void createAspName(M3uaAssociations m3uaAssociation) {
        m3uaAssociation.setAspName("ASP" + " " + m3uaAssociation.getName());
    }

    /*
     * -------------- Applications Servers ------------------------
     */

    /**
     * Retrieves a list of M3UA application servers associated with the specified M3UA ID.
     *
     * @param m3uaId The M3UA ID for which to fetch the application servers.
     * @return A {@link ApiResponse} object containing the list of M3UA application servers
     * and their ASP factories if found, or an error message if not found or in case of an exception.
     */
    public ApiResponse getM3uaAppServer(int m3uaId) {
        try {
            List<M3uaApplicationServerDTO> m3uaAppServers = m3uaAppServerRepo.fetchM3uaAppServer(m3uaId);
            if (m3uaAppServers != null) {
                for (M3uaApplicationServerDTO m3uaAppServer : m3uaAppServers) {
                    List<Integer> aspFactories = m3uaAssAppServerRepo.fetchAssAppServers(m3uaAppServer.getId());
                    m3uaAppServer.setAspFactories(aspFactories);
                }

                return ResponseMapping.successMessage("get m3ua application servers successful.", m3uaAppServers);
            }

            return ResponseMapping.errorMessageNoFound("m3ua application servers with m3ua_id = " + m3uaId + " was not found to get app servers.");
        } catch (Exception e) {
            log.error("Get M3ua application servers request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get M3ua application server was end with error", e);
        }
    }

    /**
     * Creates a new M3UA application server with the specified details, including associated ASP factories.
     *
     * @param newM3uaApServer The {@link M3uaApplicationServerDTO} object containing the details of the new M3UA application server.
     * @return A {@link ApiResponse} object containing the created M3UA application server with its ASP factories,
     * or an error message in case of an exception.
     */
    @Transactional
    public ApiResponse createM3uaAppServer(M3uaApplicationServerDTO newM3uaApServer) {
        try {
            newM3uaApServer.setState(Constants.STARTED_STATUS);
            List<Integer> aspFactories = newM3uaApServer.getAspFactories();
            this.validateAspRelation(aspFactories, newM3uaApServer.getFunctionality(), -1);

            M3uaApplicationServer m3uaAppServer = m3uaMapper.toEntityAppServer(newM3uaApServer);
            M3uaApplicationServer result = m3uaAppServerRepo.save(m3uaAppServer);
            M3uaApplicationServerDTO appServerCreated = m3uaMapper.toDTOAppServer(result);

            List<M3uaAssAppServers> m3uaAssAppServers = new ArrayList<>();
            for (int aspId : aspFactories) {
                M3uaAssAppServers m3uaAssAppServer = new M3uaAssAppServers();
                m3uaAssAppServer.setAspId(aspId);
                m3uaAssAppServer.setApplicationServerId(appServerCreated.getId());
                m3uaAssAppServers.add(m3uaAssAppServer);
            }

            m3uaAssAppServerRepo.saveAll(m3uaAssAppServers);
            appServerCreated.setAspFactories(aspFactories);

            return ResponseMapping.successMessage("New m3ua application server successful.", appServerCreated);
        } catch (Exception e) {
            log.error("New m3ua application server request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New m3ua application server was end with error", e);
        }
    }

    /**
     * Updates an existing M3UA application server identified by the given ID with new details.
     * This includes updating its associated ASP factories, effectively replacing the old associations.
     *
     * @param id           The ID of the M3UA application server to update.
     * @param m3uaApServer The new details for the M3UA application server as a {@link M3uaApplicationServerDTO}.
     * @return A {@link ApiResponse} object indicating success and containing the updated server details,
     * or an error message if the server is not found or in case of an exception.
     */
    public ApiResponse updateM3uaAppServer(int id, M3uaApplicationServerDTO m3uaApServer) {
        try {
            m3uaApServer.setState(Constants.STARTED_STATUS);
            List<Integer> aspFactories = m3uaApServer.getAspFactories();
            this.validateAspRelation(aspFactories, m3uaApServer.getFunctionality(), id);

            M3uaApplicationServer currentM3uaAppServer = m3uaAppServerRepo.findById(id);
            if (currentM3uaAppServer != null) {
                m3uaMapper.updateEntityFromAppDTO(m3uaApServer, currentM3uaAppServer);
                M3uaApplicationServer result = m3uaAppServerRepo.save(currentM3uaAppServer);

                // update Asp factories
                List<M3uaAssAppServers> m3uaAssAppServerList = m3uaAssAppServerRepo.findByApplicationServerId(id);
                m3uaAssAppServerRepo.deleteAll(m3uaAssAppServerList);
                List<M3uaAssAppServers> m3uaAssAppServers = new ArrayList<>();
                for (Integer aspId : aspFactories) {
                    M3uaAssAppServers m3uaAssAppServer = new M3uaAssAppServers();
                    m3uaAssAppServer.setAspId(aspId);
                    m3uaAssAppServer.setApplicationServerId(id);
                    m3uaAssAppServers.add(m3uaAssAppServer);
                }
                m3uaAssAppServerRepo.saveAll(m3uaAssAppServers);
                m3uaApServer = m3uaMapper.toDTOAppServer(result);
                m3uaApServer.setAspFactories(aspFactories);

                return ResponseMapping.successMessage("m3ua application server updated successful.", m3uaApServer);
            }

            return ResponseMapping.errorMessageNoFound("m3ua application server with id = " + id + " was not found to update app server.");
        } catch (Exception e) {
            log.error("Update m3ua application server request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update m3ua application server was end with error", e);
        }
    }

    /**
     * Deletes an M3UA application server identified by the given ID from the database.
     * It also deletes all associated ASP factories' associations with this server.
     *
     * @param id The ID of the M3UA application server to delete.
     * @return A {@link ApiResponse} object indicating the success of the deletion,
     * or an error message if the server is not found or in case of an exception.
     */
    @Transactional
    public ApiResponse deleteM3uaAppServer(int id) {
        try {
            M3uaApplicationServer m3uaAppServer = m3uaAppServerRepo.findById(id);
            if (m3uaAppServer != null) {
                List<M3uaAssAppServers> m3uaAssAppServerList = m3uaAssAppServerRepo.findByApplicationServerId(id);
                m3uaAssAppServerRepo.deleteAll(m3uaAssAppServerList);
                m3uaAppServerRepo.delete(m3uaAppServer);
                return ResponseMapping.successMessage("m3ua application server deleted successful.", null);
            }

            return ResponseMapping.errorMessageNoFound("m3ua application server with id = " + id + " was not found to delete app server.");
        } catch (Exception e) {
            log.error("Delete M3ua application server request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete M3ua application server was end with error", e);
        }
    }

    /**
     * Validates if a list of associations can be related to a specific AS functionality.
     * This method iterates through the provided list of association IDs and checks if the AS functionality associated with each ID matches the provided `asFunctionality`.
     *
     * @param associations    a list of association IDs
     * @param asFunctionality the AS functionality to validate against
     * @param asId            the ID of the AS being related
     * @throws IllegalArgumentException if an association has a different AS functionality
     */
    public void validateAspRelation(List<Integer> associations, String asFunctionality, int asId) {
        for (int assId : associations) {
            String currentAsFunctionality = m3uaAssAppServerRepo.getAsFunctionality(assId, asId);
            if (currentAsFunctionality != null && !currentAsFunctionality.equals(asFunctionality)) {
                M3uaAssociations asp = m3uaAssociationsRepo.findById(assId);
                throw new IllegalArgumentException("You cannot relate <" + asp.getAspName() + "> with AS of different functionality, current relation = <" + currentAsFunctionality + "> and new relation = <" + asFunctionality + ">");
            }
        }
    }

    /*
     * -------------- routes ------------------------
     */

    /**
     * Retrieves M3ua routes for a given M3ua identifier.
     *
     * @param m3uaId The M3UA identifier for which routes will be retrieved.
     * @return A ResponseDTO object containing the retrieved M3UA routes or an error message if none are found.
     */
    @Transactional
    public ApiResponse getM3uaRoutes(int m3uaId) {
        try {
            List<Integer> m3uaAppServersId = m3uaAppServerRepo.fetchM3uaAppServerId(m3uaId);
            List<M3uaRoutesDTO> m3uaRoutes = m3uaRouteRepo.fetchM3uaRoutes(m3uaAppServersId);
            if (m3uaRoutes != null) {
                for (M3uaRoutesDTO m3uaRoute : m3uaRoutes) {
                    List<Integer> appServers = m3uaAppServerRouteRepo.fetchAppServersRoute(m3uaRoute.getId());
                    m3uaRoute.setAppServers(appServers);
                }

                return ResponseMapping.successMessage("get m3ua routes successful.", m3uaRoutes);
            }

            return ResponseMapping.errorMessageNoFound("m3ua application servers with m3ua_id = " + m3uaId + " was not found to get routes.");
        } catch (Exception e) {
            log.error("Get M3ua routes request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get M3ua routes was end with error", e);
        }
    }

    /**
     * Creates a new M3UA route.
     *
     * @param newM3uaRoute The M3uaRoutesDTO object representing the new M3UA route to be created.
     * @return A ResponseDTO object indicating whether the creation operation was successful or not.
     */
    @Transactional
    public ApiResponse createM3uaRoutes(int m3uaId, M3uaRoutesDTO newM3uaRoute) {
        try {
            M3ua currentM3ua = m3uaRepo.findById(m3uaId);
            if (currentM3ua == null) {
                return ResponseMapping.errorMessageNoFound("m3ua with id = " + m3uaId + " was not found to create routes.");
            }

            List<Integer> appServers = newM3uaRoute.getAppServers();

            M3uaRoutes m3uaRoute = m3uaMapper.toEntityRoutes(newM3uaRoute);
            m3uaRoute.setM3uaId(m3uaId);
            M3uaRoutes result = m3uaRouteRepo.save(m3uaRoute);
            M3uaRoutesDTO routeCreated = m3uaMapper.toDTORoutes(result);

            m3uaAppServerRouteRepo.saveAll(processM3uaAppRoutes(appServers, result.getId()));
            routeCreated.setAppServers(appServers);

            return ResponseMapping.successMessage("New m3ua route successful.", routeCreated);
        } catch (Exception e) {
            log.error("New m3ua route request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New m3ua route was end with error", e);
        }
    }

    /**
     * Updates an existing M3UA route.
     *
     * @param id        The identifier of the M3UA route to be updated.
     * @param m3uaRoute The M3uaRoutesDTO object containing the updated data for the M3UA route.
     * @return A ResponseDTO object indicating whether the update operation was successful or not.
     */
    public ApiResponse updateM3uaRoutes(int id, M3uaRoutesDTO m3uaRoute) {
        try {
            M3uaRoutes currentM3uaRoute = m3uaRouteRepo.findById(id);
            if (currentM3uaRoute != null) {
                List<Integer> appServers = m3uaRoute.getAppServers();

                m3uaMapper.updateEntityFromRouteDTO(m3uaRoute, currentM3uaRoute);
                M3uaRoutes result = m3uaRouteRepo.save(currentM3uaRoute);
                M3uaRoutesDTO routeUpdated = m3uaMapper.toDTORoutes(result);

                // update application servers
                List<M3uaAppServersRoutes> appServersRoutes = m3uaAppServerRouteRepo.findByRouteId(id);
                m3uaAppServerRouteRepo.deleteAll(appServersRoutes);
                m3uaAppServerRouteRepo.saveAll(processM3uaAppRoutes(appServers, result.getId()));

                routeUpdated.setAppServers(appServers);
                return ResponseMapping.successMessage("New m3ua route successful.", routeUpdated);
            }

            return ResponseMapping.errorMessageNoFound("m3ua route with id = " + id + " was not found.");
        } catch (Exception e) {
            log.error("Update m3ua route request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Update m3ua route was end with error", e);
        }
    }

    /**
     * Deletes an existing M3UA route based on the provided identifier.
     *
     * @param id The identifier of the M3UA route to be deleted.
     * @return A ResponseDTO object indicating whether the deletion operation was successful or not.
     */
    @Transactional
    public ApiResponse deleteM3uaRoute(int id) {
        try {
            M3uaRoutes m3uaRoute = m3uaRouteRepo.findById(id);
            if (m3uaRoute != null) {
                List<M3uaAppServersRoutes> m3uaAppServerRouteList = m3uaAppServerRouteRepo.findByRouteId(id);
                m3uaAppServerRouteRepo.deleteAll(m3uaAppServerRouteList);
                m3uaRouteRepo.delete(m3uaRoute);
                return ResponseMapping.successMessage("m3ua route deleted successful.", null);
            }

            return ResponseMapping.errorMessageNoFound("m3ua route with id = " + id + " was not found.");
        } catch (Exception e) {
            log.error("Delete m3ua route request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Delete m3ua route was end with error", e);
        }
    }

    private void refreshRedisObjectByUpdateM3uaServers(int id, M3uaSockets currentM3uaServer, int previousEnableServer) throws Exception {
        try {
            int networkId = m3uaServerRepo.findNetworkIdById(id);
            isGatewayEnabled(networkId);

            this.propagateChangeStatusToAssociation(currentM3uaServer.getId(), currentM3uaServer.getEnabled());
            processObjSs7.updateOrCreateJsonInRedis(networkId);
            String payload = String.join("-", networkId + "", currentM3uaServer.getId() + "");
            switch (currentM3uaServer.getEnabled()) {
                case 0 -> utilsBase.sendNotificationSocket(STOP_SS7_SOCKET_ENDPOINT, payload);
                case 1 -> utilsBase.sendNotificationSocket(START_SS7_SOCKET_ENDPOINT, payload);
                default -> throw new IllegalArgumentException("Unknown status : " + currentM3uaServer.getEnabled());
            }
        } catch (Exception e) {
            // rollback manual
            currentM3uaServer.setEnabled(previousEnableServer);
            m3uaServerRepo.save(currentM3uaServer);
            throw e;
        }
    }

    private void refreshRedisObjectByUpdateM3uaAssociations(int id, M3uaAssociations currentM3uaAssociation, int previousEnableAssociation) throws Exception {
        try {
            int networkId = m3uaAssociationsRepo.findNetworkIdById(id);
            isGatewayEnabled(networkId);

            processObjSs7.updateOrCreateJsonInRedis(networkId);
            String payload = String.join("-", networkId + "", currentM3uaAssociation.getName());
            switch (currentM3uaAssociation.getEnabled()) {
                case 0 -> utilsBase.sendNotificationSocket(STOP_SS7_ASSOCIATION_ENDPOINT, payload);
                case 1 -> utilsBase.sendNotificationSocket(START_SS7_ASSOCIATION_ENDPOINT, payload);
                default -> throw new IllegalArgumentException("Unknown status : " + currentM3uaAssociation.getEnabled());
            }
        } catch (Exception e) {
            // rollback only to enable value
            currentM3uaAssociation.setEnabled(previousEnableAssociation);
            m3uaAssociationsRepo.save(currentM3uaAssociation);
            throw e;
        }
    }

    private List<M3uaAppServersRoutes> processM3uaAppRoutes(List<Integer> appServers, int routeId) {
        List<M3uaAppServersRoutes> m3uaAppServerRoutes = new ArrayList<>();
        for (int appServer : appServers) {
            M3uaAppServersRoutes m3uaAppServerRoute = new M3uaAppServersRoutes(routeId, appServer);
            m3uaAppServerRoutes.add(m3uaAppServerRoute);
        }

        return m3uaAppServerRoutes;
    }

    public void propagateStatusForM3ua(int networkId, int enabled) {
        M3ua m3ua = m3uaRepo.findByNetworkId(networkId);
        if (Objects.nonNull(m3ua)) {
            this.propagateChangeStatusToSockets(m3ua.getId(), enabled);
        }
    }

    private void propagateChangeStatusToSockets(int m3uaId, int enabled) {
        List<M3uaSockets> m3uaSockets = m3uaServerRepo.findBySs7M3uaId(m3uaId);
        for (M3uaSockets m3uaSocket : m3uaSockets) {
            m3uaSocket.setEnabled(enabled);
            m3uaSocket.setState(utilsBase.findStatusByEnabled(m3uaSocket.getEnabled()));
            m3uaServerRepo.save(m3uaSocket);

            this.propagateChangeStatusToAssociation(m3uaSocket.getId(), enabled);
        }
    }

    private void propagateChangeStatusToAssociation(int socketId, int enabled) {
        List<M3uaAssociations> m3uaAssociations = m3uaAssociationsRepo.findByM3uaSocketId(socketId);
        for (M3uaAssociations m3uaAssociation : m3uaAssociations ) {
            m3uaAssociation.setEnabled(enabled);
            m3uaAssociation.setState(utilsBase.findStatusByEnabled(m3uaAssociation.getEnabled()));
            m3uaAssociationsRepo.save(m3uaAssociation);
        }
    }

    private void allowSocketUpdates(M3uaSockets m3uaSockets, int newEnabledStatus) {
        if(m3uaSockets.getEnabled() == ACTIVE_ENABLED_STATUS && newEnabledStatus == ACTIVE_ENABLED_STATUS) {
            throw  new SmscBackendException("Not allowed to update started socket");
        }
    }

    private void allowAssociationUpdates(M3uaAssociations m3uaAssociations, int newEnabledStatus) {
        if(m3uaAssociations.getEnabled() == ACTIVE_ENABLED_STATUS && newEnabledStatus == ACTIVE_ENABLED_STATUS) {
            throw  new SmscBackendException("Not allowed to update started association");
        }
    }

    private void isGatewayEnabled(int networkId) {
        boolean exists = ss7GatewaysRepository.existsByNetworkIdAndEnabled(networkId, ACTIVE_ENABLED_STATUS);
        if (!exists) {
            throw new SmscBackendException("Gateways is stopped, please activate and try again.");
        }
    }
}
