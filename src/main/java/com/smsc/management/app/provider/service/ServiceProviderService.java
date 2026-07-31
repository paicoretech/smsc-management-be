package com.smsc.management.app.provider.service;

import static com.smsc.management.utils.Constants.DEFAULT_SMPP_SERVER_NAME;
import static com.smsc.management.utils.Constants.DEFAULT_STATUS;
import static com.smsc.management.utils.Constants.FORCED_STOPPED_STATUS;
import static com.smsc.management.utils.Constants.STARTED_STATUS;
import static com.smsc.management.utils.Constants.ACTIVE_ENABLED_STATUS;


import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.provider.dto.GenerateServiceProviderSecurityTokenRequestDTO;
import com.smsc.management.app.provider.dto.GeneratedServiceProviderSecurityTokenDTO;
import com.smsc.management.app.provider.security.ServiceProviderHttpSecurityService;
import com.smsc.management.app.server.model.repository.SmppServerRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.credit.custom.HandlerServiceProvider;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.provider.dto.ParseServiceProviderDTO;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.mapper.ServiceProviderMapper;
import com.smsc.management.app.headers.model.repository.CallbackHeaderHttpRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.utils.UtilsBase;
import lombok.extern.slf4j.Slf4j;

/**
 * Service provider for processing various operations related to service providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceProviderService {
    private final ServiceProviderRepository serviceProviderRepository;
    private final CallbackHeaderHttpRepository callbackHeaderRepo;
    private final ServiceProviderMapper serviceProviderMapper;
    private final UtilsBase utilsBase;
    private final SequenceNetworksIdGenerator seqServiceProvider;
    private final SmppServerRepository smppServerRepository;
    private final PasswordEncoder passwordEncoder;

    // to handler balance
    private final HandlerCreditByServiceProvider handlerBalance;
    private final HandlerServiceProvider handlerSp;
    private final AppProperties appProperties;
    private final ServiceProviderHttpSecurityService serviceProviderHttpSecurityService;

    /**
     * Retrieves the list of service providers.
     *
     * @return ResponseDTO containing the list of service providers
     */
	public ApiResponse getServiceProvider() {
		try {
			List<ServiceProvider> servicesProvidersEntity = serviceProviderRepository.findByEnabledNot(Constants.DELETED_ENABLED_STATUS);
			List<ParseServiceProviderDTO>  servicesProviders = new ArrayList<>();
			for (ServiceProvider sp : servicesProvidersEntity) {
				ParseServiceProviderDTO serviceProvider = serviceProviderMapper.toDTO(sp);
				List<CallbackHeaderHttp> headers = callbackHeaderRepo.findByNetworkId(sp.getNetworkId());
				List<CallbackHeaderHttpDTO> headersDTO = serviceProviderMapper.toDTOCallbackHeader(headers);
				serviceProvider.setCallbackHeadersHttp(headersDTO);

				servicesProviders.add(serviceProvider);
			}
			return ResponseMapping.successMessage("get Service Provider request success", servicesProviders);
		} catch (Exception e) {
			log.error("new service provider error on getServiceProvider: {}", e.getMessage());
			return ResponseMapping.exceptionMessage("Service provider was end with error", e);
		}
	}

	/**
	 * Retrieves a concrete Service Provider.
	 *
	 * @return ResponseDTO containing the matching Service Provider by external_id field
	 */
	public ApiResponse getServiceProviderByExternalId(String externalId) {
		try
		{
			ServiceProvider foundServiceProvider =  serviceProviderRepository.findByExternalId(externalId);
			if (foundServiceProvider == null) {
				return ResponseMapping.errorMessageNoFound("The requested service provider was not found.");
			}
			ParseServiceProviderDTO parseServiceProviderDTO = serviceProviderMapper.toDTO(foundServiceProvider);
			List<CallbackHeaderHttp> headers = callbackHeaderRepo.findByNetworkId(foundServiceProvider.getNetworkId());
			List<CallbackHeaderHttpDTO> headersDTO = serviceProviderMapper.toDTOCallbackHeader(headers);
			parseServiceProviderDTO.setCallbackHeadersHttp(headersDTO);
			return ResponseMapping.successMessage("Get Service Provider by external id request success", parseServiceProviderDTO);
		} catch (Exception e) {
			log.error("Error while getting the service provider by external id # {} : {}",externalId, e.getMessage());
			return ResponseMapping.exceptionMessage("Service provider was end with error", e);
		}
	}
	
	/**
     * Creates a new service provider.
     *
     * @param newProvider The service provider data to be created
     * @return ResponseDTO indicating the outcome of the create operation
     */
    public ApiResponse create(ServiceProviderDTO newProvider) {
        try {
            this.utilsBase.validateMaxLengthSpAndGw(newProvider.getPassword(), newProvider.getSystemId(), newProvider.getProtocol());
            this.validateCallbackUrl(newProvider.getCallbackUrl(), newProvider.getProtocol());
            this.validateTokenAuthorization(newProvider);
            this.validateSecurityAuthentication(newProvider);
            this.prepareSecurityAuthenticationForCreate(newProvider);

            if (!existsSystemIdAndEnabled(newProvider.getSystemId(), Constants.DELETED_ENABLED_STATUS, 0, "create")) {
                List<CallbackHeaderHttpDTO> callHeaders = newProvider.getCallbackHeadersHttp();
                newProvider.setBalance(0L);
                var serviceProviderEntity = serviceProviderMapper.toEntity(newProvider);
                serviceProviderEntity.setNetworkId(seqServiceProvider.getNextNetworkIdSequenceValue("SP"));
                serviceProviderEntity.setStatus(Constants.DEFAULT_STATUS);
                serviceProviderEntity.setActiveSessionsNumbers(0);
                this.assignDeafultSmppServer(serviceProviderEntity);

                var resultEntity = serviceProviderRepository.save(serviceProviderEntity);

                GeneratedServiceProviderSecurityTokenDTO generatedSecurityToken = generateSecurityTokenOnCreateIfRequired(resultEntity);
                // handler callback security headers
                for (CallbackHeaderHttpDTO header : callHeaders) {
                    CallbackHeaderHttp headerEntity = serviceProviderMapper.toEntityCallbackHeader(header);
                    headerEntity.setNetworkId(resultEntity.getNetworkId());
                    callbackHeaderRepo.save(headerEntity);
                }

                socketAndRedisAction(resultEntity.getNetworkId());
                ServiceProvider refreshedEntity = serviceProviderRepository.findById(resultEntity.getNetworkId());
                ParseServiceProviderDTO spCreated = serviceProviderMapper.toDTO(refreshedEntity);
                spCreated.setCallbackHeadersHttp(callHeaders);
                spCreated.setGeneratedSecurityToken(generatedSecurityToken);

                var result = ResponseMapping.successMessage("Service Provider added successful.", spCreated);
                log.info("new service provider created: {}", result);
                return result;
            }

            return ResponseMapping.errorMessage("There is already an active system id, you must assign a different system id.");
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("new service provider with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Service Provider was end with error", e);
        }

    }

    /**
     * Updates an existing service provider.
     *
     * @param id             The ID of the service provider to update
     * @param updateProvider The updated service provider data
     * @return ResponseDTO indicating the outcome of the update operation
     */
    public ApiResponse update(Object id, ServiceProviderDTO updateProvider) {
        try {
            boolean searchByExternalId = id instanceof String;
            String notFoundMessagePrefix = searchByExternalId ?  "Service Provider with External Id = " : "Service Provider with Id = " ;
            this.utilsBase.validateMaxLengthSpAndGw(updateProvider.getPassword(), updateProvider.getSystemId(), updateProvider.getProtocol());
            this.validateCallbackUrl(updateProvider.getCallbackUrl(), updateProvider.getProtocol());
            this.validateTokenAuthorization(updateProvider);

            ServiceProvider serviceProviderEntity = searchByExternalId ? serviceProviderRepository.findByExternalId((String) id) : serviceProviderRepository.findById((int)id);
            if (serviceProviderEntity != null) {
                updateProvider.setBalance(serviceProviderEntity.getBalance());
                this.validateSecurityAuthenticationForUpdate(updateProvider, serviceProviderEntity);
                this.prepareSecurityAuthenticationForUpdate(updateProvider, serviceProviderEntity);

                if (isUniqueSystemId(updateProvider.getSystemId(), serviceProviderEntity.getNetworkId())) {
                    if (serviceProviderEntity.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
                        return ResponseMapping.errorMessage("Illegal exception it is not possible to modify a deleted account.");
                    }
                    List<CallbackHeaderHttpDTO> callHeaders = updateProvider.getCallbackHeadersHttp();
                    updateProvider.setNetworkId(serviceProviderEntity.getNetworkId());
                    this.checkStatusChangeOnUpdateServiceProvider(serviceProviderEntity, updateProvider);
                    this.updateCallbacks(serviceProviderEntity.getNetworkId(), callHeaders);
                    this.assignDeafultSmppServer(serviceProviderEntity);

                    var resultEntity = serviceProviderRepository.save(serviceProviderEntity);
                    socketAndRedisAction(resultEntity.getNetworkId());
                    ParseServiceProviderDTO spUpdated = serviceProviderMapper.toDTO(resultEntity);
                    spUpdated.setCallbackHeadersHttp(callHeaders);
                    return ResponseMapping.successMessage("Service Provider updated successfully.", spUpdated);
                }
                return ResponseMapping.errorMessage("There is already an active system id, you must assign a different system id.");
            }
            return ResponseMapping.errorMessageNoFound(notFoundMessagePrefix + id + " was not found.");
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Update service provider with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Service Provider update failed", e);
        }
    }

    /**
     * Finds a system ID and enable status.
     *
     * @param systemId  The system ID to search for
     * @param enabled   The enable status
     * @param networkId The network ID
     * @param type      The type of operation
     * @return True if the system ID and enable status are found, false otherwise
     */
    public boolean existsSystemIdAndEnabled(String systemId, int enabled, int networkId, String type) {
        try {
            List<ServiceProvider> serviceProviderFound = switch (type) {
                case "create" -> serviceProviderRepository.findBySystemIdAndEnabledNot(systemId, enabled);
                case "update" ->
                        serviceProviderRepository.findBySystemIdAndEnabledNotAndNetworkIdNot(systemId, enabled, networkId);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
            return !serviceProviderFound.isEmpty();
        } catch (Exception e) {
            log.error("An error has occurred on existsSystemIdAndEnabled{}", e.toString());
        }
        return false;
    }

    /**
     * Performs socket and Redis actions for a service provider.
     *
     * @param networkId The network ID of the service provider
     * @return True if the action was successful, false otherwise
     */
    public boolean socketAndRedisAction(int networkId) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(networkId);
        if (Objects.equals(serviceProvider.getStatus(), FORCED_STOPPED_STATUS)) {
            serviceProvider.setEnabled(ACTIVE_ENABLED_STATUS);
            serviceProviderRepository.save(serviceProvider);
        }
        List<CallbackHeaderHttp> headers = callbackHeaderRepo.findByNetworkId(networkId);
        List<CallbackHeaderHttpDTO> headersDTO = serviceProviderMapper.toDTOCallbackHeader(headers);
        RedisServiceProviderDTO serviceProviderDTO = serviceProviderMapper.toServiceProviderDTO(serviceProvider);
        serviceProviderDTO.setCallbackHeadersHttp(headersDTO);

        // making token format
        if (Objects.nonNull(serviceProviderDTO.getAuthenticationTypes())
                && Objects.nonNull(serviceProviderDTO.getToken())
                && !serviceProviderDTO.getToken().isBlank()
                && !Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(serviceProviderDTO.getAuthenticationTypes())) {
            serviceProviderDTO.setToken(
                    serviceProviderDTO.getAuthenticationTypes() + " " + serviceProviderDTO.getToken()
            );
        }

        // routing rules data
        serviceProviderDTO.setDefaultValues();

        //do not rewrite the value of hasAvailableCredit in the service provider object
        Boolean hasAvailableCredit = false;
        if (handlerBalance.isLocalCharging()) {
            try {
                hasAvailableCredit = handlerSp.getConfigForClient(networkId).getHasAvailableCredit();
            } catch (Exception e) {
                log.warn("current has available credit flag for network id {} was no found -> {}", networkId, e.getMessage());
            }
        } else {
            hasAvailableCredit = true;
        }

        String endpoint;

        //0 : STOPPED, 1: STARTED, 2: DELETED
        switch (serviceProviderDTO.getEnabled()) {
            case 0, 1:
                endpoint = utilsBase.findUpdateEndpointByProtocolToSP(serviceProviderDTO.getProtocol());
                handlerSp.addToCache(serviceProviderDTO.getNetworkId(), serviceProviderDTO);
                handlerBalance.addNewHandlerBalance(serviceProviderDTO.getNetworkId(), serviceProviderDTO.getBalance());
                break;
            case 2:
                endpoint = utilsBase.findDeleteEndpointByProtocolToSP(serviceProviderDTO.getProtocol());
                handlerSp.removeFromCache(serviceProviderDTO.getNetworkId());
                handlerBalance.removeFromCache(serviceProviderDTO.getNetworkId());
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + serviceProviderDTO.getEnabled());
        }

        serviceProviderDTO.setHasAvailableCredit(hasAvailableCredit);
        String serviceProviderString = serviceProviderDTO.toString();
        if (Objects.nonNull(serviceProviderString)) {
            utilsBase.storeInRedis(GeneralSmscConstants.SERVICE_PROVIDERS_HASH_NAME, String.valueOf(serviceProviderDTO.getNetworkId()), serviceProviderString);
            utilsBase.sendNotificationSocket(endpoint, String.valueOf(serviceProviderDTO.getNetworkId()));
            return true;
        }
        return false;
    }

    public void validateCallbackUrl(String callbackUrl, String protocol) {
        if (protocol.equalsIgnoreCase("http") && (callbackUrl == null || callbackUrl.isBlank())) {
            throw new SmscBackendException("callback url is required to HTTP protocol");
        }
    }

    public void validateTokenAuthorization(ServiceProviderDTO sp) {
        try {
            if ("http".equalsIgnoreCase(sp.getProtocol())) {
                if ("basic".equalsIgnoreCase(sp.getAuthenticationTypes())) {
                    if (sp.getPasswd() == null || sp.getPasswd().isBlank()) {
                        throw new SmscBackendException("User name is required to Basic authentication");
                    }
                    if (sp.getUserName() == null || sp.getUserName().isBlank()) {
                        throw new SmscBackendException("password is required to Basic authentication");
                    }

                    String authString = sp.getUserName() + ":" + sp.getPasswd();
                    String authEncoded = Base64.getEncoder().encodeToString(authString.getBytes());
                    sp.setToken(authEncoded);
                } else if (!"undefined".equalsIgnoreCase(sp.getAuthenticationTypes()) && (sp.getToken() == null || sp.getToken().isBlank())) {
                    throw new SmscBackendException("Token is required to " + sp.getAuthenticationTypes() + " authentication");
                }
            }
        } catch (Exception e) {
            throw new SmscBackendException(e.getMessage());
        }
    }

    private void validateSecurityAuthentication(ServiceProviderDTO sp) {
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(sp.getProtocol())) {
            return;
        }

        if (Constants.AUTH_TYPE_BASIC.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            validateSecurityBasicAuthentication(sp);
            return;
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            validateBearerAuthentication(sp);
            return;
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(sp.getSecurityAuthenticationType())
                || Constants.AUTH_TYPE_UNDEFINED.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            return;
        }

        throw new SmscBackendException(
                "Unsupported HTTP security authentication type: " + sp.getSecurityAuthenticationType()
        );
    }

    private void validateSecurityBasicAuthentication(ServiceProviderDTO sp) {
        if (sp.getBasicSecurityPassword() == null || sp.getBasicSecurityPassword().isBlank()) {
            throw new SmscBackendException("Password is required for Basic security authentication");
        }
    }


    private void validateBearerAuthentication(ServiceProviderDTO sp) {
        if (sp.getBearerTokenExpirationSeconds() == null
                || sp.getBearerTokenExpirationSeconds() <= 0) {
            throw new SmscBackendException(Constants.ERROR_BEARER_EXPIRATION_REQUIRED);
        }
    }

    public ApiResponse generateHttpSecurityToken(int networkId, GenerateServiceProviderSecurityTokenRequestDTO request) {
        try {
            GeneratedServiceProviderSecurityTokenDTO generatedToken = serviceProviderHttpSecurityService.generateAndPersistToken(networkId, request);

            socketAndRedisAction(networkId);

            return ResponseMapping.successMessage(
                    "Service Provider security token generated successfully.",
                    generatedToken
            );
        } catch (Exception e) {
            log.error(
                    "Error generating HTTP Service Provider security token for networkId {}: {}",
                    networkId,
                    e.getMessage()
            );
            return ResponseMapping.exceptionMessage(
                    "HTTP Service Provider security token generation failed",
                    e
            );
        }
    }

    private void checkStatusChangeOnUpdateServiceProvider(ServiceProvider serviceProviderEntity, ServiceProviderDTO updateProvider) {
        // Required for SMPP server: startedSp change the status to -> Started
        boolean startedSp = serviceProviderEntity.getEnabled() == Constants.DISABLED_ENABLED_STATUS && updateProvider.getEnabled() == Constants.ACTIVE_ENABLED_STATUS;
        boolean stoppedSp = serviceProviderEntity.getEnabled() == Constants.ACTIVE_ENABLED_STATUS && updateProvider.getEnabled() == Constants.DISABLED_ENABLED_STATUS;

        serviceProviderMapper.updateEntityFromDTO(updateProvider, serviceProviderEntity);
        if (startedSp) {
            serviceProviderEntity.setStatus(STARTED_STATUS);
        } else if (stoppedSp && "http".equalsIgnoreCase(serviceProviderEntity.getProtocol())) {
            // change the status to stopped for http because the core does not raise the connection dynamically.
            serviceProviderEntity.setStatus(DEFAULT_STATUS);
        }
    }

    private void updateCallbacks(int networkId, List<CallbackHeaderHttpDTO> callHeaders) {
        // handler callback security headers
        callbackHeaderRepo.deleteAllByNetworkId(networkId);

        for (CallbackHeaderHttpDTO header : callHeaders) {
            CallbackHeaderHttp headerEntity = serviceProviderMapper.toEntityCallbackHeader(header);
            headerEntity.setNetworkId(networkId);
            callbackHeaderRepo.save(headerEntity);
        }
    }

    public boolean isUniqueSystemId(String systemId, int networkId) {
        try {
            return serviceProviderRepository.findBySystemIdAndEnabledNotAndNetworkIdNot(systemId, Constants.DELETED_ENABLED_STATUS, networkId).isEmpty();
        } catch (Exception e) {
            log.error("An error has occurred on isUniqueSystemId {}", e.toString());
        }
        return false;
    }

    /**
     * This method retrieves the default SMPP server (based on a predefined name) from the repository,
     * and if the ServiceProvider (sp) uses the "SMPP" protocol and has no assigned SMPP server ID,
     * it assigns the ID of the default SMPP server to the ServiceProvider.
     *
     * @param sp The ServiceProvider to which the default SMPP server will be assigned.
     */
    private void assignDeafultSmppServer(ServiceProvider sp) {
        var smppServer = smppServerRepository.findByName(DEFAULT_SMPP_SERVER_NAME);
        if (Objects.nonNull(smppServer)
                && "SMPP".equalsIgnoreCase(sp.getProtocol())
                && Objects.isNull(sp.getSmppServerId())) {
            sp.setSmppServerId(smppServer.getId());
        }
    }

    private GeneratedServiceProviderSecurityTokenDTO generateSecurityTokenOnCreateIfRequired(
            ServiceProvider serviceProvider
    ) {
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(serviceProvider.getProtocol())) {
            return null;
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(serviceProvider.getSecurityAuthenticationType())) {
            return serviceProviderHttpSecurityService.generateAndPersistBearerToken(serviceProvider.getNetworkId());
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(serviceProvider.getSecurityAuthenticationType())) {
            return serviceProviderHttpSecurityService.generateAndPersistApiKeyToken(serviceProvider.getNetworkId());
        }

        return null;
    }

    private void prepareSecurityAuthenticationForCreate(ServiceProviderDTO sp) {
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(sp.getProtocol())) {
            return;
        }

        if (Constants.AUTH_TYPE_BASIC.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(passwordEncoder.encode(sp.getBasicSecurityPassword()));
            sp.setBearerTokenExpirationSeconds(null);
            return;
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            return;
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            sp.setBearerTokenExpirationSeconds(null);
            return;
        }

        if (Constants.AUTH_TYPE_UNDEFINED.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            sp.setBearerTokenExpirationSeconds(null);
        }
    }

    private void validateSecurityAuthenticationForUpdate(
            ServiceProviderDTO sp,
            ServiceProvider currentEntity
    ) {
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(sp.getProtocol())) {
            return;
        }

        if (Constants.AUTH_TYPE_BASIC.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            boolean hasNewPassword = sp.getBasicSecurityPassword() != null
                    && !sp.getBasicSecurityPassword().isBlank();

            boolean alreadyConfigured = currentEntity.getBasicSecurityPassword() != null
                    && !currentEntity.getBasicSecurityPassword().isBlank();

            if (!hasNewPassword && !alreadyConfigured) {
                throw new SmscBackendException("Password is required for Basic security authentication");
            }

            return;
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            validateBearerAuthentication(sp);
            return;
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(sp.getSecurityAuthenticationType())
                || Constants.AUTH_TYPE_UNDEFINED.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            return;
        }

        throw new SmscBackendException(
                "Unsupported HTTP security authentication type: " + sp.getSecurityAuthenticationType()
        );
    }

    private void prepareSecurityAuthenticationForUpdate(
            ServiceProviderDTO sp,
            ServiceProvider currentEntity
    ) {
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(sp.getProtocol())) {
            return;
        }

        if (Constants.AUTH_TYPE_BASIC.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            if (sp.getBasicSecurityPassword() != null && !sp.getBasicSecurityPassword().isBlank()) {
                sp.setBasicSecurityPassword(passwordEncoder.encode(sp.getBasicSecurityPassword()));
            } else {
                sp.setBasicSecurityPassword(currentEntity.getBasicSecurityPassword());
            }

            sp.setBearerTokenExpirationSeconds(null);
            return;
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            return;
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            sp.setBearerTokenExpirationSeconds(null);
            return;
        }

        if (Constants.AUTH_TYPE_UNDEFINED.equalsIgnoreCase(sp.getSecurityAuthenticationType())) {
            sp.setBasicSecurityPassword(null);
            sp.setBearerTokenExpirationSeconds(null);
        }
    }
}
