package com.smsc.management.app.provider.security;

import com.smsc.management.app.provider.dto.GenerateServiceProviderSecurityTokenRequestDTO;
import com.smsc.management.app.provider.dto.GeneratedServiceProviderSecurityTokenDTO;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceProviderHttpSecurityService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderJwt serviceProviderJwt;
    private final BearerTokenBlacklistService bearerTokenBlacklistService;

    @Transactional
    public GeneratedServiceProviderSecurityTokenDTO generateAndPersistToken(int networkId, GenerateServiceProviderSecurityTokenRequestDTO request) {
        ServiceProvider serviceProvider = getHttpServiceProvider(networkId);

        if (request == null || !StringUtils.hasText(request.getSecurityAuthenticationType())) {
            throw new SmscBackendException("security_authentication_type is required");
        }

        if (Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(request.getSecurityAuthenticationType())) {
            return generateAndPersistBearerToken(serviceProvider, request.getBearerTokenExpirationSeconds());
        }

        if (Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(request.getSecurityAuthenticationType())) {
            return generateAndPersistApiKeyToken(serviceProvider);
        }

        throw new SmscBackendException(
                "Token generation is only supported for Bearer or Api-key authentication"
        );
    }

    @Transactional
    public GeneratedServiceProviderSecurityTokenDTO generateAndPersistApiKeyToken(int networkId) {
        ServiceProvider serviceProvider = getHttpServiceProvider(networkId);

        if (!Constants.AUTH_TYPE_API_KEY.equalsIgnoreCase(serviceProvider.getSecurityAuthenticationType())) {
            throw new SmscBackendException(Constants.ERROR_API_KEY_AUTH_NOT_CONFIGURED);
        }

        return generateAndPersistApiKeyToken(serviceProvider);
    }

    @Transactional
    public GeneratedServiceProviderSecurityTokenDTO generateAndPersistBearerToken(int networkId) {
        ServiceProvider serviceProvider = getHttpServiceProvider(networkId);

        if (!Constants.AUTH_TYPE_BEARER.equalsIgnoreCase(serviceProvider.getSecurityAuthenticationType())) {
            throw new SmscBackendException(Constants.ERROR_BEARER_AUTH_NOT_CONFIGURED);
        }

        Long expirationSeconds = serviceProvider.getBearerTokenExpirationSeconds();
        if (expirationSeconds == null || expirationSeconds <= 0) {
            throw new SmscBackendException(Constants.ERROR_BEARER_EXPIRATION_REQUIRED);
        }

        blacklistOldBearerTokenIfValid(serviceProvider);

        GeneratedServiceProviderJwt generated = serviceProviderJwt.generateBearerToken(serviceProvider, expirationSeconds);

        serviceProvider.setBearerSecurityTokenJti(generated.jti());
        serviceProvider.setBearerSecurityTokenExpiresAt(generated.expiresAt());
        serviceProviderRepository.save(serviceProvider);

        return GeneratedServiceProviderSecurityTokenDTO.builder()
                .headerName(resolveHeaderName(serviceProvider, Constants.DEFAULT_AUTHORIZATION_HEADER))
                .authenticationType(Constants.AUTH_TYPE_BEARER)
                .token(generated.token())
                .build();
    }

    private GeneratedServiceProviderSecurityTokenDTO generateAndPersistBearerToken(ServiceProvider serviceProvider, Long expirationSeconds) {
        if (expirationSeconds == null || expirationSeconds <= 0) {
            throw new SmscBackendException(Constants.ERROR_BEARER_EXPIRATION_REQUIRED);
        }

        blacklistOldBearerTokenIfValid(serviceProvider);

        serviceProvider.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        serviceProvider.setBearerTokenExpirationSeconds(expirationSeconds);
        serviceProvider.setApiKeySecurityToken(null);
        serviceProvider.setBasicSecurityPassword(null);

        GeneratedServiceProviderJwt generated = serviceProviderJwt.generateBearerToken(serviceProvider, expirationSeconds);

        serviceProvider.setBearerSecurityTokenJti(generated.jti());
        serviceProvider.setBearerSecurityTokenExpiresAt(generated.expiresAt());
        serviceProviderRepository.save(serviceProvider);

        return GeneratedServiceProviderSecurityTokenDTO.builder()
                .headerName(Constants.DEFAULT_AUTHORIZATION_HEADER)
                .authenticationType(Constants.AUTH_TYPE_BEARER)
                .token(generated.token())
                .build();
    }

    private GeneratedServiceProviderSecurityTokenDTO generateAndPersistApiKeyToken(ServiceProvider serviceProvider) {
        serviceProvider.setSecurityAuthenticationType(Constants.AUTH_TYPE_API_KEY);
        serviceProvider.setBearerTokenExpirationSeconds(null);
        serviceProvider.setBearerSecurityTokenJti(null);
        serviceProvider.setBearerSecurityTokenExpiresAt(null);
        serviceProvider.setBasicSecurityPassword(null);

        GeneratedServiceProviderJwt generated = serviceProviderJwt.generateApiKeyToken(serviceProvider);

        serviceProvider.setApiKeySecurityToken(generated.token());
        serviceProviderRepository.save(serviceProvider);

        return GeneratedServiceProviderSecurityTokenDTO.builder()
                .headerName(Constants.DEFAULT_API_KEY_HEADER)
                .authenticationType(Constants.AUTH_TYPE_API_KEY)
                .token(generated.token())
                .build();
    }

    private void blacklistOldBearerTokenIfValid(ServiceProvider serviceProvider) {
        String oldJti = serviceProvider.getBearerSecurityTokenJti();
        Long oldExpiresAt = serviceProvider.getBearerSecurityTokenExpiresAt();

        if (StringUtils.hasText(oldJti) && oldExpiresAt != null) {
            bearerTokenBlacklistService.addToBlacklist(oldJti, oldExpiresAt);
        }
    }

    private ServiceProvider getHttpServiceProvider(int networkId) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(networkId);
        if (serviceProvider == null) {
            throw new SmscBackendException(Constants.ERROR_SERVICE_PROVIDER_NOT_FOUND);
        }
        if (!Constants.PROTOCOL_HTTP.equalsIgnoreCase(serviceProvider.getProtocol())) {
            throw new SmscBackendException(Constants.ERROR_PROTOCOL_HTTP_ONLY);
        }
        return serviceProvider;
    }

    private String resolveHeaderName(ServiceProvider serviceProvider, String defaultHeader) {
        return StringUtils.hasText(serviceProvider.getHeaderSecurityName())
                ? serviceProvider.getHeaderSecurityName()
                : defaultHeader;
    }
}
