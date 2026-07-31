package com.smsc.management.app.ss7.service;

import java.util.List;
import java.util.Objects;

import com.smsc.management.exception.SmscBackendException;
import org.springframework.stereotype.Service;

import com.smsc.management.utils.ApiResponse;

import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import com.smsc.management.app.ss7.dto.SccpDTO;
import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRemoteResourcesDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.SccpServiceAccessPointsDTO;
import com.smsc.management.app.ss7.model.entity.Sccp;
import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import com.smsc.management.app.ss7.model.entity.SccpLongMessageRules;
import com.smsc.management.app.ss7.model.entity.SccpMtp3Destinations;
import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;
import com.smsc.management.app.ss7.model.entity.SccpRules;
import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;
import com.smsc.management.app.ss7.mapper.SccpMapper;
import com.smsc.management.app.ss7.model.repository.SccpAddressesRepository;
import com.smsc.management.app.ss7.model.repository.SccpLongMessageRulesRepository;
import com.smsc.management.app.ss7.model.repository.SccpMtp3DestinationsRepository;
import com.smsc.management.app.ss7.model.repository.SccpRemoteResourcesRepository;
import com.smsc.management.app.ss7.model.repository.SccpRepository;
import com.smsc.management.app.ss7.model.repository.SccpRulesRepository;
import com.smsc.management.app.ss7.model.repository.SccpServiceAccessPointsRepository;
import com.smsc.management.utils.ResponseMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SccpService {
	private final SccpRepository sccpRepo;
	private final SccpRemoteResourcesRepository sccpRemoteResourcesRepo;
	private final SccpServiceAccessPointsRepository sccpSapRepo;
	private final SccpMtp3DestinationsRepository sccpMtp3DestRepo;
	private final SccpLongMessageRulesRepository sccpLongMessageRulesRepo;
	private final SccpAddressesRepository sccpAddressRepo;
	private final SccpRulesRepository sccpRulesRepo;
	private final SccpMapper sccpMapper;

	/**
	 * Retrieves sccp configuration by network ID.
	 *
	 * @param id The network ID or external id for which sccp configuration is being retrieved.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse getSccp(Object id) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "sccp configuration not found for external_id= " : "sccp configuration not found for network_id= ";
			Sccp sccp = searchByExternalId ? sccpRepo.findByExternalId((String) id) : sccpRepo.findByNetworkId((int) id);
			if (sccp != null) {
				return ResponseMapping.successMessage("get sccp request successful.", sccpMapper.toDTO(sccp));
			}

			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
		} catch (Exception e) {
			log.error("Sccp request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Sccp was end with error", e);
		}
	}
	
	/**
	 * Creates a new Sccp general configuration.
	 * 
	 * @param newSccp The SccpDTO object containing details of the new Sccp configuration.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse create(SccpDTO newSccp) {
		try {
			Sccp sccpEntity = sccpMapper.toEntity(newSccp);
			var result = sccpRepo.save(sccpEntity);

			return ResponseMapping.successMessage("sccp created successful.", sccpMapper.toDTO(result));
		} catch (Exception e) {
			log.error("New sccp request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New sccp was end with error", e);
		}
	}

	/**
	 * Updates an existing SCCP configuration.
	 *
	 * @param id   The ID or external id of the Sccp configuration to be updated.
	 * @param sccp The SccpDTO object containing the new details of the Sccp configuration.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse update(Object id, SccpDTO sccp) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "sccp configuration not found with external id = " : "sccp configuration not found with id = ";
			Sccp currentSccp = searchByExternalId ? sccpRepo.findByExternalId((String) id) : sccpRepo.findById((int) id);
			if (currentSccp != null) {
				sccpMapper.updateEntityFromSCCPDTO(sccp, currentSccp);
				Sccp result = sccpRepo.save(currentSccp);
				return ResponseMapping.successMessage("sccp update successful.", sccpMapper.toDTO(result));
			}

			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
		} catch (Exception e) {
			log.error("Update sccp request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Update sccp was end with error", e);
		}
	}

	/**
	 * Deletes an existing sccp configuration by its ID.
	 *
	 * @param id The ID of the sccp configuration to be deleted.
	 * @return A ResponseDTO object containing the request response.
	 */
	public ApiResponse delete(Object id) {
		try {
			boolean searchByExternalId = id instanceof String;
			String notFoundMessagePrefix = searchByExternalId ? "sccp configuration not found with external id = " : "sccp configuration not found with id = ";
			Sccp sccp = searchByExternalId ? sccpRepo.findByExternalId((String) id) : sccpRepo.findById((int) id);
			if (sccp != null) {
				sccpRepo.delete(sccp);
				return ResponseMapping.successMessage("sccp deleted successful.", null);
			}

			return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id);
		} catch (Exception e) {
			log.error("Delete sccp request with error: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Delete sccp was end with error", e);
		}
	}
	
	/*
	 * Remote Resources
	 */
	
	/**
	 * Gets the SCCP remote resources associated with a specific SCCP ID.
	 * This method retrieves all SCCP remote resources from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId the ID of the SCCP to get remote resources for
	 * @return a ResponseDTO object containing the retrieved SCCP remote resources or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing a list of SCCP remote resource DTOs.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse getSccpRemoteResources(int sccpId) {
		try {
			List<SccpRemoteResources> sccpRemoteResources = sccpRemoteResourcesRepo.findBySs7SccpId(sccpId);
			if (sccpRemoteResources != null) {
				return ResponseMapping.successMessage("Get sccp remote resources successful.", sccpMapper.toDTORemoteResources(sccpRemoteResources));
			}

			return ResponseMapping.errorMessageNoFound("Sccp remote resources configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp remote resources to sccp_id = {} -> {}", sccpId, e.getMessage());
			return ResponseMapping.exceptionMessage("Get sccp remote resources was end with error", e);
		}
	}
	
	/**
	 * Creates a new SCCP remote resource.
	 * This method takes an SCCP remote resource DTO object as input, converts it to an entity object,
	 * saves it to the repository, and returns the saved entity as a DTO.
	 *
	 * @param newSccpRemoteSource the SCCP remote resource information to create
	 * @return a ResponseDTO object containing the created SCCP remote resource or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the created SCCP remote resource DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse createSccpRemoteResources(SccpRemoteResourcesDTO newSccpRemoteSource) {
		try {
			SccpRemoteResources sccpRemoteResources = sccpMapper.toEntityRemoteResources(newSccpRemoteSource);
			var result = sccpRemoteResourcesRepo.save(sccpRemoteResources);
			return ResponseMapping.successMessage("New sccp remote resources successful.", sccpMapper.toDTORemoteResources(result));
		} catch (Exception e) {
			log.error("Error to create sccp remote resource -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create sccp remote resource", e);
		}
	}
	
	/**
	 * Updates an existing SCCP remote resource.
	 *
	 * @param id the ID of the SCCP remote resource to update
	 * @param newSccpRemoteSource the updated SCCP remote resource information
	 * @return a ResponseDTO object containing the updated SCCP remote resource or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the updated SCCP remote resource DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse updateSccpRemoteResources(int id, SccpRemoteResourcesDTO newSccpRemoteSource) {
		try {
			SccpRemoteResources currentSccpRemoteResource = sccpRemoteResourcesRepo.findById(id);
			if (currentSccpRemoteResource != null) {
				sccpMapper.updateEntityFromRemoteResourcesDTO(newSccpRemoteSource, currentSccpRemoteResource);
				var result = sccpRemoteResourcesRepo.save(currentSccpRemoteResource);
				return ResponseMapping.successMessage("Sccp remote resource updated successful.", sccpMapper.toDTORemoteResources(result));
			}

			return ResponseMapping.errorMessageNoFound("Sccp remote resource configuration not found to update with id = " + id);
		} catch (Exception e) {
			log.error("Error to update a sccp remote resources -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp remote resource", e);
		}
	}
	
	/**
	 * Deletes an SCCP remote resource by ID.
	 * This method retrieves the SCCP remote resource from the repository by ID and deletes it.
	 *
	 * @param id the ID of the SCCP remote resource to delete
	 * @return a ResponseDTO object indicating success or failure of the deletion
	 *      * On success, the ResponseDTO will have a success status code and a success message.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse deleteSccpRemoteResources(int id) {
		try {
			SccpRemoteResources currentSccpRemoteResource = sccpRemoteResourcesRepo.findById(id);
			if (currentSccpRemoteResource != null) {
				sccpRemoteResourcesRepo.delete(currentSccpRemoteResource);
				return ResponseMapping.successMessage("Sccp remote resource deleted successful.", null);
			}

			return ResponseMapping.errorMessageNoFound("Sccp remote resource configuration not found to delete with id = " + id);
		} catch (Exception e) {
			log.error("Error to delete sccp remote resource -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp remote resource", e);
		}
	}
	
	/*
	 * service_access_points
	 */
	
	/**
	 * Gets the SCCP service access points associated with a specific SCCP ID.
	 * This method retrieves all SCCP service access points from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId the ID of the SCCP to get service access points for
	 * @return a ResponseDTO object containing the retrieved SCCP service access points or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing a list of SCCP service access point DTOs.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse getSccpServiceAccessPoints(int sccpId) {
		try {
			List<SccpServiceAccessPoints> sccpSap = sccpSapRepo.findBySs7SccpId(sccpId);
			if (sccpSap != null) {
				return ResponseMapping.successMessage("Get sccp service access points successful.", sccpMapper.toDTOSap(sccpSap));
			}

			return ResponseMapping.errorMessageNoFound("Sccp service access points configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp service access points -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get sccp service access points", e);
		}
	}
	
	/**
	 * Creates a new SCCP service access point.
	 * This method takes an SCCP service access point DTO object as input, converts it to an entity object, saves it to the repository,
	 * and returns the saved entity as a DTO.
	 *
	 * @param newSccpSapDTO the SCCP service access point information to create
	 * @return a ResponseDTO object containing the created SCCP service access point or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the created SCCP service access point DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse createSccpServiceAccessPoints(SccpServiceAccessPointsDTO newSccpSapDTO) {
		try {
			SccpServiceAccessPoints newSccpSapEntity = sccpMapper.toEntitySap(newSccpSapDTO);
			var result = sccpSapRepo.save(newSccpSapEntity);

			return ResponseMapping.successMessage("New sccp service access point successful.", sccpMapper.toDTOSap(result));
		} catch (Exception e) {
			log.error("Error to create new sccp service access point -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create new sccp service access point", e);
		}
	}
	
	/**
	 * Updates an existing SCCP service access point.
	 * This method takes an ID and an SCCP service access point DTO object as input.
	 * It retrieves the existing SCCP service access point from the repository by ID, updates its information with the provided DTO data,
	 * saves the updated entity back to the repository, and returns the saved entity as a DTO.
	 *
	 * @param id the ID of the SCCP service access point to update
	 * @param sccpSapDTO the updated SCCP service access point information
	 * @return a ResponseDTO object containing the updated SCCP service access point or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the updated SCCP service access point DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse updateSccpServiceAccessPoints(int id, SccpServiceAccessPointsDTO sccpSapDTO) {
		try {
			SccpServiceAccessPoints currentSccpSap = sccpSapRepo.findById(id);
			if (currentSccpSap != null) {
				sccpMapper.updateEntityFromSapDTO(sccpSapDTO, currentSccpSap);
				var result = sccpSapRepo.save(currentSccpSap);
				return ResponseMapping.successMessage("Sccp service access point updated successful.", sccpMapper.toDTOSap(result));
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp service access point configuration not found with id = " + id + " to update");
		} catch (Exception e) {
			log.error("Error to update sccp service access point -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp service access point", e);
		}
	}
	
	/**
	 * Deletes an SCCP service access point by ID.
	 * This method retrieves the SCCP service access point from the repository by ID and deletes it.
	 *
	 * @param id the ID of the SCCP service access point to delete
	 * @return a ResponseDTO object indicating success or failure of the deletion
	 *      * On success, the ResponseDTO will have a success status code and a success message.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse deleteSccpServiceAccessPoints(int id) {
		try {
			SccpServiceAccessPoints currentSccpSap = sccpSapRepo.findById(id);
			if (currentSccpSap != null) {
				sccpSapRepo.delete(currentSccpSap);
				return ResponseMapping.successMessage("Sccp service access point deleted successful.", null);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp service access point configuration not found to delete with id = " + id);
		} catch (Exception e) {
			log.error("Error to delete sccp service access point -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp service access point", e);
		}
	}
	
	/*
	 * MTP3 Destinations
	 */
	
	/**
	 * Gets the SCCP MTP3 destinations associated with a specific SCCP ID.
	 * This method retrieves all SCCP MTP3 destinations from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId the ID of the SCCP to get MTP3 destinations for
	 * @return a ResponseDTO object containing the retrieved SCCP MTP3 destinations or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing a list of SCCP MTP3 destination DTOs.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse getSccpMtp3Destinations(int sccpId) {
		try {
			List<SccpMtp3DestinationsDTO> sccpMtpDestinations = sccpMtp3DestRepo.fetchMtp3Destinations(sccpId);
			if (sccpMtpDestinations != null) {
				return ResponseMapping.successMessage("Get sccp mtp3 destinations successful.", sccpMtpDestinations);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp mtp3 destinations configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp MTP3 destinations -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get sccp MTP3 destinations", e);
		}
	}
	
	/**
	 * Creates a new SCCP MTP3 destination.
	 * This method takes an SCCP MTP3 destination DTO object as input, converts it to an entity object, saves it to the repository, and returns the saved entity as a DTO.
	 *
	 * @param newSccpMtp3Dest the SCCP MTP3 destination information to create
	 * @return a ResponseDTO object containing the created SCCP MTP3 destination or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the created SCCP MTP3 destination DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse createSccpMtp3Destinations(SccpMtp3DestinationsDTO newSccpMtp3Dest) {
		try {
			SccpMtp3Destinations sccpMtp3Dest = sccpMapper.toEntityMTP3(newSccpMtp3Dest);
			var result = sccpMtp3DestRepo.save(sccpMtp3Dest);
			
			return ResponseMapping.successMessage("New sccp mtp3 destinations successful.", sccpMapper.toDTOMTP3(result));
		} catch (Exception e) {
			log.error("Error to create sccp mtp3 destination -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create sccp mtp3 destination", e);
		}
	}
	
	/**
	 * Updates an existing SCCP MTP3 destination.
	 * This method takes an ID and an SCCP MTP3 destination DTO object as input. It retrieves the existing SCCP MTP3 destination from the repository by ID,
	 * updates its information with the provided DTO data, saves the updated entity back to the repository, and returns the saved entity as a DTO.
	 *
	 * @param id the ID of the SCCP MTP3 destination to update
	 * @param sccpMtp3Dest the updated SCCP MTP3 destination information
	 * @return a ResponseDTO object containing the updated SCCP MTP3 destination or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the updated SCCP MTP3 destination DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse updateSccpMtp3Destinations(int id, SccpMtp3DestinationsDTO sccpMtp3Dest) {
		try {
			SccpMtp3Destinations currentSccpMtp = sccpMtp3DestRepo.findById(id);
			if (currentSccpMtp != null) {
				sccpMapper.updateEntityFromMtp3DTO(sccpMtp3Dest, currentSccpMtp);
				var result = sccpMtp3DestRepo.save(currentSccpMtp);
				return ResponseMapping.successMessage("sccp mtp3 destinations updated successful.", sccpMapper.toDTOMTP3(result));
			}
			
			return ResponseMapping.errorMessageNoFound("sccp mtp3 destinations configuration not found with id = " + id + " to update");
		} catch (Exception e) {
			log.error("Error to update sccp mtp3 destinations -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp mtp3 destinations", e);
		}
	}
	
	/**
	 * Deletes an SCCP MTP3 destination by ID.
	 * This method retrieves the SCCP MTP3 destination from the repository by ID and deletes it.
	 *
	 * @param id the ID of the SCCP MTP3 destination to delete
	 * @return a ResponseDTO object indicating success or failure of the deletion
	 *      * On success, the ResponseDTO will have a success status code and a success message.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse deleteSccpMtp3Destinations(int id) {
		try {
			SccpMtp3Destinations sccpMtp = sccpMtp3DestRepo.findById(id);
			if (sccpMtp != null) {
				sccpMtp3DestRepo.delete(sccpMtp);
				return ResponseMapping.successMessage("Sccp mtp3 destination deleted successful.", null);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp mtp3 destination configuration not found with id = " + id + " to delete.");
		} catch (Exception e) {
			log.error("Error to delete sccp mtp3 destinations -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp mtp3 destinations", e);
		}
	}

	/*
	 * Long Message Rules
	 */

	/**
	 * Gets the SCCP Long Message Rules associated with a specific SCCP ID.
	 * This method retrieves all Long Message Rules from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId the ID of the SCCP to get Long Message Rules for
	 * @return a ResponseDTO object containing the retrieved Long Message Rules or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing a list of Long Message Rule DTOs.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse getSccpLongMessageRules(int sccpId) {
		try {
			List<SccpLongMessageRulesDTO> longMessageRules = sccpLongMessageRulesRepo.fetchLongMessageRulesBySccpId(sccpId);
			if (longMessageRules != null) {
				return ResponseMapping.successMessage("Get sccp long message rules successful.", longMessageRules);
			}

			return ResponseMapping.errorMessageNoFound("Sccp long message rules configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp long message rules -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get sccp long message rules", e);
		}
	}

	/**
	 * Creates a new SCCP Long Message Rule.
	 * This method takes a Long Message Rule DTO object as input, converts it to an entity object, saves it to the repository, and returns the saved entity as a DTO.
	 *
	 * @param newLongMessageRule the Long Message Rule information to create
	 * @return a ResponseDTO object containing the created Long Message Rule or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the created Long Message Rule DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse createSccpLongMessageRules(SccpLongMessageRulesDTO newLongMessageRule) {
		try {
			SccpLongMessageRules longMessageRule = sccpMapper.toEntityLongMessageRules(newLongMessageRule);
			var result = sccpLongMessageRulesRepo.save(longMessageRule);

			return ResponseMapping.successMessage("New sccp long message rule successful.", sccpMapper.toDTOLongMessageRules(result));
		} catch (Exception e) {
			log.error("Error to create sccp long message rule -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create sccp long message rule", e);
		}
	}

	/**
	 * Updates an existing SCCP Long Message Rule.
	 * This method takes an ID and a Long Message Rule DTO object as input. It retrieves the existing Long Message Rule from the repository by ID,
	 * updates its information with the provided DTO data, saves the updated entity back to the repository, and returns the saved entity as a DTO.
	 *
	 * @param id the ID of the Long Message Rule to update
	 * @param longMessageRule the updated Long Message Rule information
	 * @return a ResponseDTO object containing the updated Long Message Rule or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the updated Long Message Rule DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse updateSccpLongMessageRules(int id, SccpLongMessageRulesDTO longMessageRule) {
		try {
			SccpLongMessageRules currentLongMessageRule = sccpLongMessageRulesRepo.findById(id);
			if (currentLongMessageRule != null) {
				sccpMapper.updateEntityFromLongMessageRulesDTO(longMessageRule, currentLongMessageRule);
				var result = sccpLongMessageRulesRepo.save(currentLongMessageRule);
				return ResponseMapping.successMessage("sccp long message rule updated successful.", sccpMapper.toDTOLongMessageRules(result));
			}

			return ResponseMapping.errorMessageNoFound("sccp long message rule configuration not found with id = " + id + " to update");
		} catch (Exception e) {
			log.error("Error to update sccp long message rule -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp long message rule", e);
		}
	}

	/**
	 * Deletes an SCCP Long Message Rule by ID.
	 * This method retrieves the Long Message Rule from the repository by ID and deletes it.
	 *
	 * @param id the ID of the Long Message Rule to delete
	 * @return a ResponseDTO object indicating success or failure of the deletion
	 *      * On success, the ResponseDTO will have a success status code and a success message.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
	 */
	public ApiResponse deleteSccpLongMessageRules(int id) {
		try {
			SccpLongMessageRules longMessageRule = sccpLongMessageRulesRepo.findById(id);
			if (longMessageRule != null) {
				sccpLongMessageRulesRepo.delete(longMessageRule);
				return ResponseMapping.successMessage("Sccp long message rule deleted successful.", null);
			}

			return ResponseMapping.errorMessageNoFound("Sccp long message rule configuration not found with id = " + id + " to delete.");
		} catch (Exception e) {
			log.error("Error to delete sccp long message rule -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp long message rule", e);
		}
	}

	/*
	 * SCCP Address
	 */
	
	/**
	 * Gets SCCP addresses associated with a specific SCCP ID.
	 * This method retrieves all SCCP addresses from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId the ID of the SCCP to get addresses for
	 * @return a ResponseDTO object containing the retrieved SCCP addresses or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing a list of SCCP address DTOs.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse getSccpAddress(int sccpId) {
		try {
			List<SccpAddressesDTO> sccpAddresses = sccpAddressRepo.findDetailedBySs7SccpId(sccpId);
			if (sccpAddresses != null) {
				return ResponseMapping.successMessage("Get sccp addresses successful.", sccpAddresses);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp sccp addresses configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp address -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get sccp address ", e);
		}
	}
	
	/**
	 * Creates a new SCCP address.
	 * This method takes an SCCP address DTO object as input, validates the address indicator, converts the DTO to an entity object, saves it to the repository, and returns the saved entity as a DTO.
	 *
	 * @param newSccpAddress the SCCP address information to create
	 * @return a ResponseDTO object containing the created SCCP address or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the created SCCP address DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse createSccpAddress(SccpAddressesDTO newSccpAddress) {
		try {
			newSccpAddress.setGtIndicator(this.createGtIndicator(
					"",
					newSccpAddress.getAddressIndicator(),
					newSccpAddress.getPointCode(),
					newSccpAddress.getSubsystemNumber(),
					newSccpAddress.getNatureOfAddressId(),
					newSccpAddress.getNumberingPlanId(),
					newSccpAddress.getTranslationType()
					));
			SccpAddresses sccpAddress = sccpMapper.toEntityAddress(newSccpAddress);
			var result = sccpAddressRepo.save(sccpAddress);
			
			return ResponseMapping.successMessage("New sccp address successful.", sccpMapper.toDTOAddress(result));
		} catch (Exception e) {
			log.error("Error to create sccp address -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create sccp address ", e);
		}
	}
	
	/**
	 * Updates an existing SCCP address.
	 * This method takes an ID and an SCCP address DTO object as input. It retrieves the existing SCCP address from the repository by ID,
	 * validates the address indicator, updates its information with the provided DTO data, saves the updated entity back to the repository,
	 * and returns the saved entity as a DTO.
	 *
	 * @param id the ID of the SCCP address to update
	 * @param sccpAddress the updated SCCP address information
	 * @return a ResponseDTO object containing the updated SCCP address or an error message
	 *      * On success, the ResponseDTO will have a success status code and a data object containing the updated SCCP address DTO.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse updateSccpAddress(int id, SccpAddressesDTO sccpAddress) {
		try {
			SccpAddresses currentSccpAddress = sccpAddressRepo.findById(id);
			if (currentSccpAddress != null) {
				sccpAddress.setGtIndicator(this.createGtIndicator(
						"",
						sccpAddress.getAddressIndicator(),
						sccpAddress.getPointCode(),
						sccpAddress.getSubsystemNumber(),
						sccpAddress.getNatureOfAddressId(),
						sccpAddress.getNumberingPlanId(),
						sccpAddress.getTranslationType())
						);
				sccpMapper.updateEntityFromAddressDTO(sccpAddress, currentSccpAddress);
				var result = sccpAddressRepo.save(currentSccpAddress);
				return ResponseMapping.successMessage("sccp address updated successful.", sccpMapper.toDTOAddress(result));
			}
			
			return ResponseMapping.errorMessageNoFound("sccp address configuration not found with id = " + id + " to update.");
		} catch (Exception e) {
			log.error("Error to update sccp address -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp address ", e);
		}
	}
	
	/**
	 * Deletes an SCCP address by ID.
	 * This method retrieves the SCCP address from the repository by ID and deletes it.
	 *
	 * @param id the ID of the SCCP address to delete
	 * @return a ResponseDTO object indicating success or failure of the deletion
	 *      * On success, the ResponseDTO will have a success status code and a success message.
	 *      * On error, the ResponseDTO will have an error status code and an error message.
     */
	public ApiResponse deleteSccpAddress(int id) {
		try {
			SccpAddresses sccpAddress = sccpAddressRepo.findById(id);
			if (sccpAddress != null) {
				sccpAddressRepo.delete(sccpAddress);
				return ResponseMapping.successMessage("Sccp address deleted successful.", null);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp address configuration to delete with id = " + id);
		} catch (Exception e) {
			log.error("Error to delete sccp address -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp address ", e);
		}
	}
	
	/*
	 * SCCP RULES
	 */
	
	/**
	 * Retrieves SCCP rules associated with a specific SCCP ID.
	 * This method fetches all SCCP rules from the repository that are linked to the provided `sccpId`.
	 *
	 * @param sccpId The ID of the SCCP to retrieve rules for
	 * @return A `ResponseDTO` containing:
	 *      - Success status code and a list of SCCP rule DTOs on success
	 *      - Error status code and an informative error message on error
     */
	public ApiResponse getSccpRules(int sccpId) {
		try {
			List<SccpRulesDTO> sccpAddresses = sccpRulesRepo.fetchSccpRules(sccpId);
			if (sccpAddresses != null) {
				return ResponseMapping.successMessage("Get sccp rules successful.", sccpAddresses);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp sccp rules configuration not found with sccp_id = " + sccpId);
		} catch (Exception e) {
			log.error("Error to get sccp rules -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to get sccp rules ", e);
		}
	}
	
	/**
	 * Creates a new SCCP rule.
	 * This method takes an SCCP rule DTO object as input, performs the following actions:
	 *   1. Validates and sets the `gtIndicator` field using `createGtIndicator`.
	 *   2. Validates and sets the `callingGtIndicator` field using `createGtIndicator`.
	 *   3. Converts the DTO to an SCCP rule entity object.
	 *   4. Saves the entity object to the repository.
	 *   5. Returns the saved entity as an SCCP rule DTO.
	 *
	 * @param newSccpRules The SCCP rule information to create
	 * @return A `ResponseDTO` containing:
	 *      - Success status code and the created SCCP rule DTO on success
	 *      - Error status code and an informative error message on error
     */
	public ApiResponse createSccpRules(SccpRulesDTO newSccpRules) {
		try {
			this.validateAddressRules(newSccpRules);
			String gtIndicator = this.createGtIndicator(
					"",
					newSccpRules.getAddressIndicator(),
					newSccpRules.getPointCode(),
					newSccpRules.getSubsystemNumber(),
					newSccpRules.getNatureOfAddressId(),
					newSccpRules.getNumberingPlanId(),
					newSccpRules.getTranslationType()
			);

			newSccpRules.setGtIndicator(gtIndicator);
			
			if (newSccpRules.getCallingAddressIndicator() != null) {
				newSccpRules.setCallingGtIndicator(this.createGtIndicator(
					"calling",
					newSccpRules.getCallingAddressIndicator(),
					newSccpRules.getCallingPointCode(),
					newSccpRules.getCallingSubsystemNumber(),
					newSccpRules.getCallingNatureOfAddressId(),
					newSccpRules.getCallingNumberingPlanId(),
					newSccpRules.getCallingTranslatorType()
				));
			}
			newSccpRules.setMask(newSccpRules.getMask().toUpperCase());
			SccpRules sccpRules = sccpMapper.toEntityRules(newSccpRules);
			var result = sccpRulesRepo.save(sccpRules);
			
			return ResponseMapping.successMessage("New sccp rules successful.", sccpMapper.toDTORules(result));
		} catch (Exception e) {
			log.error("Error to create sccp rules -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to create sccp rules ", e);
		}
	}
	
	/**
	 * Updates an existing SCCP rule.
	 * This method takes an ID and an SCCP rule DTO object as input:
	 *   1. Retrieves the existing SCCP rule entity by ID from the repository.
	 *   2. Validates and sets the `gtIndicator` field using `createGtIndicator`.
	 *   3. Validates and sets the `callingGtIndicator` field using `createGtIndicator`.
	 *   4. Updates the retrieved entity's information with the provided DTO data.
	 *   5. Saves the updated entity back to the repository.
	 *   6. Returns the saved entity as an SCCP rule DTO.
	 *
	 * @param id The ID of the SCCP rule to update
	 * @param sccpRules The updated SCCP rule information
	 * @return A `ResponseDTO` containing:
	 *      - Success status code and the updated SCCP rule DTO on success
	 *      - Error status code and an informative error message on error
     */
	public ApiResponse updateSccpRules(int id, SccpRulesDTO sccpRules) {
		try {
			this.validateAddressRules(sccpRules);
			SccpRules currentSccpRules = sccpRulesRepo.findById(id);
			if (currentSccpRules != null) {
				sccpRules.setGtIndicator(this.createGtIndicator(
						"",
						sccpRules.getAddressIndicator(),
						sccpRules.getPointCode(),
						sccpRules.getSubsystemNumber(),
						sccpRules.getNatureOfAddressId(),
						sccpRules.getNumberingPlanId(),
						sccpRules.getTranslationType())
						);
				
				if (Objects.nonNull(sccpRules.getCallingAddressIndicator())) {
					sccpRules.setCallingGtIndicator(this.createGtIndicator(
						"calling",
						sccpRules.getCallingAddressIndicator(),
						sccpRules.getCallingPointCode(),
						sccpRules.getCallingSubsystemNumber(),
						sccpRules.getCallingNatureOfAddressId(),
						sccpRules.getCallingNumberingPlanId(),
						sccpRules.getCallingTranslatorType()
					));
				}
				
				sccpRules.setMask(sccpRules.getMask().toUpperCase());
				sccpMapper.updateEntityFromRulesDTO(sccpRules, currentSccpRules);
				var result = sccpRulesRepo.save(currentSccpRules);
				return ResponseMapping.successMessage("sccp rules updated successful.", sccpMapper.toDTORules(result));
			}
			
			return ResponseMapping.errorMessageNoFound("sccp rules configuration not found with id = " + id + " to update.");
		} catch (Exception e) {
			log.error("Error to update sccp rules -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to update sccp rules ", e);
		}
	}
	
	/**
	 * Deletes an SCCP rule by ID.
	 * This method locates the SCCP rule entity in the repository by ID and removes it.
	 *
	 * @param id The ID of the SCCP rule to delete
	 * @return A `ResponseDTO` indicating success or failure of the deletion:
	 *      - Success status code and a success message on success
	 *      - Error status code and an informative error message on error
     */
	public ApiResponse deleteSccpRules(int id) {
		try {
			SccpRules sccpRules = sccpRulesRepo.findById(id);
			if (sccpRules != null) {
				sccpRulesRepo.delete(sccpRules);
				return ResponseMapping.successMessage("Sccp rules deleted successful.", null);
			}
			
			return ResponseMapping.errorMessageNoFound("Sccp rules configuration not found to delete with id = " + id);
		} catch (Exception e) {
			log.error("Error to delete sccp rules -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Error to delete sccp rules ", e);
		}
	}
	
	/**
	 * Validates and analyzes the Address Indicator field of an SCCP address or rule.
	 * This method takes various SCCP address/rule fields as input and performs the following actions:
	 * To understand above Address Indicator follow below rules
	 * --------------------------------
	 * | 8 | 7 | 6 | 5 | 4 |3 | 2 | 1 |
	 * --------------------------------
	 * Bit '1' : PC Indicator (1 = included)
	 * Bit '2' : SSN Indicator (1 = included)
	 * Bit '3 - 6' : GT Indicator
	 * (0000 = GT not included)
	 * (0001 = GT includes Nature of Address)
	 * (0010 = GT includes Translation Type)
	 * (0011 = GT includes Translation Type, Numbering Plan and Encoding Scheme)
	 * (0100 = GT includes Translation Type, Numbering Plan and Encoding Scheme and Nature of Address)
	 * Bit '7' : Routing Indicator (0 = Route on GT, 1 = Route on PC + SSN)
	 * Bit '8' : Reserved for National use.
	 * This method is likely used internally by methods that create or update SCCP addresses or rules.
	 *
	 * @param prefixVar A string prefix used for exception messages ("sccp" or "calling" depending on context)
	 * @param addressIndicator The value of the Address Indicator field
	 * @param pointCode The value of the Point Code field
	 * @param subsystemNumber The value of the Subsystem Number field
	 * @param natureOfAddress The value of the Nature of Address field
	 * @param numberingPlan The value of the Numbering Plan field
	 * @param translationType The value of the Translation Type field
	 * @return A string in the format "GT" followed by the GT Indicator value
	 */
	public String createGtIndicator (String prefixVar, int addressIndicator, int pointCode, int subsystemNumber, int natureOfAddress, int numberingPlan, int translationType) {
		
		// validate valid value to address indicator
		this.validAddressIndicator(addressIndicator, prefixVar);
		
		String binaryString = Integer.toBinaryString(addressIndicator);
        String binary = String.format("%8s", binaryString).replace(' ', '0');
        
        // Analyze the binary string
        char pcIndicator = binary.charAt(7); // Bit 1
        char ssnIndicator = binary.charAt(6); // Bit 2
        String gtIndicator = binary.substring(2, 6); // Bits 3-6
        
        if ('1' == pcIndicator && pointCode < 1) {
			String errorMessage = String.format(
					"%s Address Indicator value %d indicates that %s point code is present, however %s Point Code passed is < 1",
					prefixVar, addressIndicator, prefixVar, prefixVar
			);

			throw new SmscBackendException(errorMessage);
		}
        
        if ('1' == ssnIndicator && subsystemNumber < 1) {
			String errorMessage = String.format(
					"%s Address Indicator value %d indicates that %s Subsystem Number is present, however %s Subsystem Number passed is < 1",
					prefixVar, addressIndicator, prefixVar, prefixVar
			);

			throw new SmscBackendException(errorMessage);
		}
        
        switch(gtIndicator) {
        	case "0001" -> this.validNatureOfAddress(addressIndicator, natureOfAddress, prefixVar);
        	case "0010" -> this.validTranslationType(addressIndicator, translationType, prefixVar);
        	case "0011" -> {
					this.validTranslationType(addressIndicator, translationType, prefixVar);
        			this.validNumberingPlan(addressIndicator, numberingPlan, prefixVar);
				}
        	case "0100" -> {
					this.validTranslationType(addressIndicator, translationType, prefixVar);
					this.validNumberingPlan(addressIndicator, numberingPlan, prefixVar);
					this.validNatureOfAddress(addressIndicator, natureOfAddress, prefixVar);
				}
			default -> log.info("Default gtIndicator {}", gtIndicator);
        }
        
        return "GT" + gtIndicator; 
	}
	
	public void validateAddressRules(SccpRulesDTO sccpRules) {
        if (!(sccpRules.getPrimaryAddressId() != null || sccpRules.getSecondaryAddressId() != null)) {
            throw new SmscBackendException("At least one Address must be associated");
        }
    }

	private void validAddressIndicator(int addressIndicator, String prefixVar) {
		if (!((addressIndicator >= 0 && addressIndicator <= 19) || (addressIndicator >= 64 && addressIndicator <= 83))) {
			throw new SmscBackendException("Invalid " + prefixVar + " Address Indicator value");
		}
	}

	private void validTranslationType(int addressIndicator, int translationType, String prefixVar) {
		if (translationType < 0 || translationType > 255) {
			String errorMessage = String.format(
					"%s Address Indicator value %d indicates that %s Translation Type is present, however %s Translation Type passed is invalid",
					prefixVar, addressIndicator, prefixVar, prefixVar
			);

			throw new SmscBackendException(errorMessage);
		}
	}

	private void validNatureOfAddress(int addressIndicator, int natureOfAddress, String prefixVar) {
		if (natureOfAddress  == -1) {
			String errorMessage = String.format(
					"%s Address Indicator value %d indicates that %s Nature of Address is present, however %s Nature of Address passed is invalid",
					prefixVar, addressIndicator, prefixVar, prefixVar
			);
			throw new SmscBackendException(errorMessage);
		}
	}

	private void validNumberingPlan(int addressIndicator, int numberingPlan, String prefixVar) {
		if (numberingPlan  == -1) {
			String errorMessage = String.format(
					"%s Address Indicator value %d indicates that %s Numbering Plan is present, however %s Numbering Plan passed is invalid",
					prefixVar, addressIndicator, prefixVar, prefixVar
			);

			throw new SmscBackendException(errorMessage);
		}
	}

} 
