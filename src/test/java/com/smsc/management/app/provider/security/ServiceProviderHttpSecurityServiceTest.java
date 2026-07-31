package com.smsc.management.app.provider.security;

import com.smsc.management.app.provider.dto.GenerateServiceProviderSecurityTokenRequestDTO;
import com.smsc.management.app.provider.dto.GeneratedServiceProviderSecurityTokenDTO;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceProviderHttpSecurityServiceTest {

    private ServiceProviderRepository serviceProviderRepository;
    private ServiceProviderJwt serviceProviderJwt;
    private BearerTokenBlacklistService bearerTokenBlacklistService;
    private ServiceProviderHttpSecurityService service;

    @BeforeEach
    void setUp() {
        serviceProviderRepository = mock(ServiceProviderRepository.class);
        serviceProviderJwt = mock(ServiceProviderJwt.class);
        bearerTokenBlacklistService = mock(BearerTokenBlacklistService.class);
        service = new ServiceProviderHttpSecurityService(serviceProviderRepository, serviceProviderJwt, bearerTokenBlacklistService);
    }

    @Test
    void shouldGenerateAndPersistBearerToken_whenNoOldJtiExists() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerTokenExpirationSeconds(300L);
        sp.setHeaderSecurityName(Constants.DEFAULT_AUTHORIZATION_HEADER);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateBearerToken(sp, 300L)).thenReturn(new GeneratedServiceProviderJwt("jwt-bearer-token", "jti-123", 1_700_000_300L));

        GeneratedServiceProviderSecurityTokenDTO result = service.generateAndPersistBearerToken(101);

        assertEquals(Constants.DEFAULT_AUTHORIZATION_HEADER, result.getHeaderName());
        assertEquals(Constants.AUTH_TYPE_BEARER, result.getAuthenticationType());
        assertEquals("jwt-bearer-token", result.getToken());

        ArgumentCaptor<ServiceProvider> captor = ArgumentCaptor.forClass(ServiceProvider.class);
        verify(serviceProviderRepository).save(captor.capture());
        assertEquals("jti-123", captor.getValue().getBearerSecurityTokenJti());
        assertEquals(1_700_000_300L, captor.getValue().getBearerSecurityTokenExpiresAt());
        verify(bearerTokenBlacklistService, never()).addToBlacklist(any(), anyLong());
    }

    @Test
    void shouldBlacklistOldJti_whenRegeneratingBearerToken_andOldTokenIsValid() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerTokenExpirationSeconds(300L);
        sp.setHeaderSecurityName(Constants.DEFAULT_AUTHORIZATION_HEADER);
        sp.setBearerSecurityTokenJti("old-jti");
        sp.setBearerSecurityTokenExpiresAt(1_700_000_500L);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateBearerToken(sp, 300L)).thenReturn(new GeneratedServiceProviderJwt("new-token", "new-jti", 1_700_000_800L));

        service.generateAndPersistBearerToken(101);

        verify(bearerTokenBlacklistService).addToBlacklist("old-jti", 1_700_000_500L);

        ArgumentCaptor<ServiceProvider> captor = ArgumentCaptor.forClass(ServiceProvider.class);
        verify(serviceProviderRepository).save(captor.capture());
        assertEquals("new-jti", captor.getValue().getBearerSecurityTokenJti());
        assertEquals(1_700_000_800L, captor.getValue().getBearerSecurityTokenExpiresAt());
    }

    @Test
    void shouldNotBlacklist_whenOldJtiIsNull() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerTokenExpirationSeconds(300L);
        sp.setBearerSecurityTokenJti(null);
        sp.setBearerSecurityTokenExpiresAt(1_700_000_500L);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateBearerToken(sp, 300L)).thenReturn(new GeneratedServiceProviderJwt("new-token", "new-jti", 1_700_000_800L));

        service.generateAndPersistBearerToken(101);

        verify(bearerTokenBlacklistService, never()).addToBlacklist(any(), anyLong());
    }

    @Test
    void shouldNotBlacklist_whenOldExpiresAtIsNull() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerTokenExpirationSeconds(300L);
        sp.setBearerSecurityTokenJti("old-jti");
        sp.setBearerSecurityTokenExpiresAt(null);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateBearerToken(sp, 300L)).thenReturn(new GeneratedServiceProviderJwt("new-token", "new-jti", 1_700_000_800L));

        service.generateAndPersistBearerToken(101);

        verify(bearerTokenBlacklistService, never()).addToBlacklist(any(), anyLong());
    }

    @Test
    void shouldBlacklistOldJti_whenSwitchingToBearerViaRequest() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerSecurityTokenJti("old-jti");
        sp.setBearerSecurityTokenExpiresAt(1_700_000_500L);
        sp.setBasicSecurityPassword("{bcrypt}old-pass");
        sp.setApiKeySecurityToken("old-api-key");

        GenerateServiceProviderSecurityTokenRequestDTO request = new GenerateServiceProviderSecurityTokenRequestDTO();
        request.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        request.setBearerTokenExpirationSeconds(600L);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateBearerToken(sp, 600L)).thenReturn(new GeneratedServiceProviderJwt("new-bearer-token", "new-jti", 1_700_000_700L));

        GeneratedServiceProviderSecurityTokenDTO result = service.generateAndPersistToken(101, request);

        assertEquals(Constants.AUTH_TYPE_BEARER, result.getAuthenticationType());
        assertEquals("new-bearer-token", result.getToken());
        verify(bearerTokenBlacklistService).addToBlacklist("old-jti", 1_700_000_500L);

        ArgumentCaptor<ServiceProvider> captor = ArgumentCaptor.forClass(ServiceProvider.class);
        verify(serviceProviderRepository).save(captor.capture());
        assertEquals("new-jti", captor.getValue().getBearerSecurityTokenJti());
        assertEquals(1_700_000_700L, captor.getValue().getBearerSecurityTokenExpiresAt());
        assertNull(captor.getValue().getApiKeySecurityToken());
        assertNull(captor.getValue().getBasicSecurityPassword());
    }

    @Test
    void shouldGenerateAndPersistApiKeyToken_withNoBlacklistInteraction() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_API_KEY);
        sp.setSystemId("sp-1");
        sp.setHeaderSecurityName(Constants.DEFAULT_API_KEY_HEADER);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateApiKeyToken(sp)).thenReturn(new GeneratedServiceProviderJwt("jwt-api-key-token", "jti-456", null));

        GeneratedServiceProviderSecurityTokenDTO result = service.generateAndPersistApiKeyToken(101);

        assertEquals(Constants.DEFAULT_API_KEY_HEADER, result.getHeaderName());
        assertEquals(Constants.AUTH_TYPE_API_KEY, result.getAuthenticationType());
        assertEquals("jwt-api-key-token", result.getToken());

        ArgumentCaptor<ServiceProvider> captor = ArgumentCaptor.forClass(ServiceProvider.class);
        verify(serviceProviderRepository).save(captor.capture());
        assertEquals("jwt-api-key-token", captor.getValue().getApiKeySecurityToken());
        assertNull(captor.getValue().getBearerSecurityTokenJti());
        assertNull(captor.getValue().getBearerSecurityTokenExpiresAt());
        verifyNoInteractions(bearerTokenBlacklistService);
    }

    @Test
    void shouldClearBearerExpiresAt_whenSwitchingToApiKey() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setSystemId("sp-1");
        sp.setBearerSecurityTokenJti("old-jti");
        sp.setBearerSecurityTokenExpiresAt(1_700_000_500L);

        GenerateServiceProviderSecurityTokenRequestDTO request = new GenerateServiceProviderSecurityTokenRequestDTO();
        request.setSecurityAuthenticationType(Constants.AUTH_TYPE_API_KEY);

        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        when(serviceProviderJwt.generateApiKeyToken(sp)).thenReturn(new GeneratedServiceProviderJwt("new-api-key", "api-jti", null));

        service.generateAndPersistToken(101, request);

        ArgumentCaptor<ServiceProvider> captor = ArgumentCaptor.forClass(ServiceProvider.class);
        verify(serviceProviderRepository).save(captor.capture());
        assertNull(captor.getValue().getBearerSecurityTokenJti());
        assertNull(captor.getValue().getBearerSecurityTokenExpiresAt());
        verifyNoInteractions(bearerTokenBlacklistService);
    }

    @Test
    void shouldFailWhenServiceProviderDoesNotExist() {
        when(serviceProviderRepository.findById(101)).thenReturn(null);
        assertThrows(SmscBackendException.class, () -> service.generateAndPersistBearerToken(101));
    }

    @Test
    void shouldFailWhenServiceProviderIsNotHttp() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol("SMPP");
        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        assertThrows(SmscBackendException.class, () -> service.generateAndPersistBearerToken(101));
    }

    @Test
    void shouldFailWhenBearerAuthIsNotConfigured() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_API_KEY);
        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        assertThrows(SmscBackendException.class, () -> service.generateAndPersistBearerToken(101));
    }

    @Test
    void shouldFailWhenBearerExpirationIsMissing() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(101);
        sp.setProtocol(Constants.PROTOCOL_HTTP);
        sp.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        sp.setBearerTokenExpirationSeconds(null);
        when(serviceProviderRepository.findById(101)).thenReturn(sp);
        assertThrows(SmscBackendException.class, () -> service.generateAndPersistBearerToken(101));
    }
}