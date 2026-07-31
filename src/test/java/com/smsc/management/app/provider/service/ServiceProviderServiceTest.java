package com.smsc.management.app.provider.service;

import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.credit.custom.HandlerServiceProvider;
import com.smsc.management.app.provider.dto.ParseServiceProviderDTO;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import com.smsc.management.app.provider.mapper.ServiceProviderMapper;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.headers.model.repository.CallbackHeaderHttpRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.app.server.model.repository.SmppServerRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import com.smsc.management.app.provider.dto.GenerateServiceProviderSecurityTokenRequestDTO;
import com.smsc.management.app.provider.dto.GeneratedServiceProviderSecurityTokenDTO;
import com.smsc.management.app.provider.security.ServiceProviderHttpSecurityService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith({MockitoExtension.class})
class ServiceProviderServiceTest {

    @Mock
    ServiceProviderRepository serviceProviderRepository;

    @Mock
    CallbackHeaderHttpRepository callbackHeaderRepo;

    @Mock
    ServiceProviderMapper serviceProviderMapper;

    @Mock
    UtilsBase utilsBase;

    @Mock
    SequenceNetworksIdGenerator seqServiceProvider;

    @Mock
    HandlerCreditByServiceProvider handlerBalance;

    @Mock
    HandlerServiceProvider handlerSp;

    @Mock
    AppProperties appProperties;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ServiceProviderHttpSecurityService serviceProviderHttpSecurityService;

    @InjectMocks
    ServiceProviderService serviceProviderService;

    @Mock
    SmppServerRepository smppServerRepository;

    @Test
    @DisplayName("getServiceProvider when an exception occurs then a 5xx error is returned")
    void getSpWhenSearchingSpByEnabledThenReturn5xxError() {
        when(serviceProviderRepository.findByEnabledNot(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = serviceProviderService.getServiceProvider();
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("getServiceProviderByExternalId when an exception occurs then a 5xx error is returned")
    void getSpByExternalIdWhenSearchThenReturn5xxError() {
        when(serviceProviderRepository.findByExternalId(anyString())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = serviceProviderService.getServiceProviderByExternalId(anyString());
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("create when service provider already exists then a 4xx error is returned")
    void createWhenSpExistsThenReturn4xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        when(serviceProviderRepository.findBySystemIdAndEnabledNot(anyString(), anyInt())).thenReturn(List.of(new ServiceProvider()));
        ApiResponse response = serviceProviderService.create(serviceProviderDTO);
        assertEquals(400, response.status());
    }

    @Test
    @DisplayName("create when falling into a data integrity violation issue then a DataIntegrityViolationException is thrown")
    void createWhenDataViolationThenViolationExceptionIsThrown() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        serviceProviderDTO.setSmppServerId(null);
        when(serviceProviderRepository.findBySystemIdAndEnabledNot(anyString(), anyInt())).thenReturn(null);
        when(serviceProviderMapper.toEntity(any())).thenReturn(new ServiceProvider());
        when(serviceProviderRepository.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        assertThrows(DataIntegrityViolationException.class, () -> serviceProviderService.create(serviceProviderDTO));
    }

    @Test
    @DisplayName("create when an exception occurs then a 5xx error is returned")
    void createWhenFailsThenReturn5xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        when(serviceProviderRepository.findBySystemIdAndEnabledNot(anyString(), anyInt())).thenThrow(new RuntimeException());
        ApiResponse response = serviceProviderService.create(serviceProviderDTO);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("update when updating a non existing service provider then a 4xx error is returned")
    void givenDtoWhenUpdateNotExistingSpThenReturn4xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    @DisplayName("update when updating an existing sp with the properties of another one then a 4xx error is returned")
    void updateWhenSpWithExistingObjectThenReturn4xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        when(serviceProviderRepository.findById(anyInt())).thenReturn(new ServiceProvider());
        when(serviceProviderRepository.findBySystemIdAndEnabledNotAndNetworkIdNot(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ServiceProvider()));
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    @DisplayName("update when trying to update a deleted service provider then a 4xx error is returned")
    void updateWhenActionOverDeletedSpThenReturn4xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setEnabled(Constants.DELETED_ENABLED_STATUS);
        when(serviceProviderRepository.findById(anyInt())).thenReturn(serviceProvider);
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    @DisplayName("update when falling into a data violation issue then a DataIntegrityViolationException is thrown")
    void updateWhenDataViolationThenViolationExceptionIsThrown() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        when(serviceProviderRepository.findById(anyInt())).thenReturn(new ServiceProvider());
        when(serviceProviderRepository.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        assertThrows(DataIntegrityViolationException.class, () -> serviceProviderService.update(-1, serviceProviderDTO));
    }

    @Test
    @DisplayName("update when the process fails then a 5xx error is returned")
    void updateWhenFailsThenReturn5xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        when(serviceProviderRepository.findById(anyInt())).thenThrow(new RuntimeException());
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("update when changing the service provider state to started then an OK message is returned")
    void updateWhenSpIsStoppedAndNoProtocolThenSpIsStarted() {
        testServiceProviderByEnabledStatus(Boolean.TRUE, null);
    }

    @Test
    @DisplayName("update when changing the service provider state to stopped then an OK message is returned")
    void updateWhenSpIsStartedAndNoProtocolThenSpIsStopped() {
        testServiceProviderByEnabledStatus(Boolean.FALSE, null);
    }

    @Test
    @DisplayName("update when changing any HTTP service provider state to stopped then an OK message is returned")
    void updateWhenSpIsStartedThenSpIsStopped() {
        testServiceProviderByEnabledStatus(Boolean.FALSE, "http");
    }

    @Test
    @DisplayName("update an existing service provider when setting another system id then an OK message is returned")
    void updateWhenSpHasDifferentSystemIdThenReturnOk() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("System1");
        serviceProvider.setNetworkId(10);
        when(serviceProviderRepository.findById(anyInt())).thenReturn(serviceProvider);
        ServiceProvider anotherServiceProvider = new ServiceProvider();
        anotherServiceProvider.setSystemId("System2");
        anotherServiceProvider.setNetworkId(10);
        when(serviceProviderRepository.save(serviceProvider)).thenReturn(anotherServiceProvider);
        when(callbackHeaderRepo.findByNetworkId(anyInt())).thenReturn(List.of(new CallbackHeaderHttp()));
        when(serviceProviderMapper.toServiceProviderDTO(serviceProvider)).thenReturn(new RedisServiceProviderDTO());
        when(serviceProviderMapper.toDTO(anotherServiceProvider)).thenReturn(new ParseServiceProviderDTO());
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @DisplayName("existsSystemIdAndEnabled when setting up a non existing system id and any type then return false")
    void existsSystemIdAndEnabledWhenSearchAnyTypeThenReturnDoesNotExists() {
        boolean result = serviceProviderService.existsSystemIdAndEnabled("SystemId", 0, 1, "anotherType");
        assertFalse(result);
    }

    @Test
    @DisplayName("existsSystemIdAndEnabled when setting up a non existing system id and update as the type then return false")
    void existsSystemIdAndEnabledWhenSearchAnySystemIdThenReturnDoesNotExists() {
        boolean result = serviceProviderService.existsSystemIdAndEnabled("SystemId", 0, 1, "update");
        assertFalse(result);
    }

    @Test
    @DisplayName("socketAndRedisAction when setting the state to 0 then return an OK message")
    void socketAndRedisActionWhenNotEnableSpThenReturnOk() {
        socketAndRedisActionCaseTest(0, Boolean.TRUE);
    }

    @Test
    @DisplayName("socketAndRedisAction when setting the state to 2 then return an OK message")
    void socketAndRedisActionWhenEnableSpThenReturnOk() {
        socketAndRedisActionCaseTest(2, Boolean.FALSE);
    }

    @Test
    @DisplayName("socketAndRedisAction when setting an invalid state then return false")
    void socketAndRedisActionWhenWrongStateThenThrowsIllegalArgumentException() {
        socketAndRedisActionCaseTest(3, Boolean.FALSE);
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is null and the protocol is HTTP then an exception is thrown")
    void validateCallbackUrlWhenNullCallbackAndHttpThenThrowsRuntimeException() {
        assertThrows(Exception.class, () -> serviceProviderService.validateCallbackUrl(null, "http"));
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is empty and the protocol is HTTP then an exception is thrown")
    void validateCallbackUrlWhenEmptyCallbackAndHttpThenThrowsRuntimeException() {
        assertThrows(Exception.class, () -> serviceProviderService.validateCallbackUrl("", "http"));
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is valid and the protocol is HTTP then return an OK message")
    void validateCallbackUrlWhenCallbackAndHttpThenReturnOk() {
        assertDoesNotThrow(() -> serviceProviderService.validateCallbackUrl("http://callback.com/", "http"));
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is valid and the protocol is SMPP then return an OK message")
    void validateCallbackUrlWhenCallbackAndSmppThenReturnOk() {
        assertDoesNotThrow(() -> serviceProviderService.validateCallbackUrl("http://callback.com/", "smpp"));
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is null and the protocol is SMPP then return an OK message")
    void validateCallbackUrlWhenNullCallbackAndSmppThenReturnOk() {
        assertDoesNotThrow(() -> serviceProviderService.validateCallbackUrl(null, "smpp"));
    }

    @Test
    @DisplayName("validateCallbackUrl when the callback URL is empty and the protocol is SMPP then return an OK message")
    void validateCallbackUrlWhenEmptyCallbackAndSmppThenReturnOk() {
        assertDoesNotThrow(() -> serviceProviderService.validateCallbackUrl("", "smpp"));
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a wrong authorization type with a null value then an exception is thrown")
    void validateTokenAuthWhenWrongTypeAndNullValueThenThrowsRuntimeException() {
        validateTokenAuthorizationUserAndPasswordTest("password", null);
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a wrong authorization type with an empty value then an exception is thrown")
    void validateTokenAuthWhenWrongTypeAndEmptyValueThenThrowsRuntimeException() {
        validateTokenAuthorizationUserAndPasswordTest("password", "");
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a wrong authorization with any value then an exception is thrown")
    void validateTokenAuthWhenWrongTypeAndValueThenThrowsRuntimeException() {
        validateTokenAuthorizationUserAndPasswordTest("password", "paSSw0Rd");
    }

    @Test
    @DisplayName("validateTokenAuthorization when setting the username without password then return the response")
    void validateTokenAuthWhenUserNameAndNullValueThenReturnResponse() {
        validateTokenAuthorizationUserAndPasswordTest("userName", null);
    }

    @Test
    @DisplayName("validateTokenAuthorization when setting the username with empty password then return the response")
    void validateTokenAuthWhenUserNameAndEmptyValueThenReturnResponse() {
        validateTokenAuthorizationUserAndPasswordTest("userName", "");
    }

    @Test
    @DisplayName("validateTokenAuthorization when setting the username and the password then return the response")
    void validateTokenAuthWhenUserNameAndValueThenReturnResponse() {
        validateTokenAuthorizationUserAndPasswordTest("userName", "TestUser");
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a bearer header without value then return the response")
    void validateTokenAuthWhenBearerAndNullValueThenReturnResponse() {
        validateTokenAuthorizationNoBasicAuthTest("bearer", null);
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a bearer header with empty value then return the response")
    void validateTokenAuthWhenBearerAndEmptyValueThenReturnResponse() {
        validateTokenAuthorizationNoBasicAuthTest("bearer", "");
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a bearer header with any value then return the response")
    void validateTokenAuthWhenBearerAndValueThenReturnResponse() {
        validateTokenAuthorizationNoBasicAuthTest("bearer", "any-bearer");
    }

    @Test
    @DisplayName("validateTokenAuthorization when using a bearer with any header and any value then return the response")
    void validateTokenAuthWhenAnyKeyAndAnyValueThenReturnResponse() {
        validateTokenAuthorizationNoBasicAuthTest("undefined", "undefined");
    }

    @Test
    @DisplayName("isUniqueSystemId when an exception is thrown in findBySystemIdAndEnabledNotAndNetworkIdNot then return false")
    void isUniqueSystemIdWhenEmptySystemIdThenReturnResponse() {
        when(serviceProviderRepository.findBySystemIdAndEnabledNotAndNetworkIdNot(anyString(), anyInt(), anyInt())).thenThrow(new RuntimeException());
        boolean response = serviceProviderService.isUniqueSystemId("", 0);
        assertFalse(response);
    }

    @Test
    @DisplayName("create when trying to save a non complete service DTO object then a 5xx error message is returned")
    void createWhenPartialDtoThenReturn5xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        serviceProviderDTO.setBindType("TRANSMITTER");
        serviceProviderDTO.setProtocol("SMPP");
        ApiResponse response = serviceProviderService.create(serviceProviderDTO);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("update when trying to save a non complete service DTO object then a 5xx error message is returned")
    void updateWhenPartialDtoThenReturn5xxError() {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        serviceProviderDTO.setBindType("TRANSMITTER");
        serviceProviderDTO.setProtocol("SMPP");
        when(serviceProviderRepository.findById(anyInt())).thenReturn(new ServiceProvider());
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("setCustomParameters accepts a valid JSON object and stores it")
    void setCustomParametersWhenValidJsonThenStored() {
        ServiceProviderDTO dto = new ServiceProviderDTO();
        String validJson = "{\"system_id\":\"NMB\",\"pwd\":\"test123\"}";

        dto.setCustomParameters(validJson);

        assertEquals(validJson, dto.getCustomParameters());
    }

    @Test
    @DisplayName("setCustomParameters throws when value is not a valid JSON object")
    void setCustomParametersWhenInvalidJsonThenThrowsIllegalArgumentException() {
        ServiceProviderDTO dto = new ServiceProviderDTO();

        assertThrows(IllegalArgumentException.class,
                () -> dto.setCustomParameters("not-valid-json"));
    }

    @Test
    @DisplayName("setCustomParameters accepts null without attempting validation")
    void setCustomParametersWhenNullThenAccepted() {
        ServiceProviderDTO dto = new ServiceProviderDTO();

        dto.setCustomParameters(null);

        assertNull(dto.getCustomParameters());
    }

    @Test
    @DisplayName("setCustomParameters accepts a blank string without attempting validation")
    void setCustomParametersWhenBlankThenAcceptedWithoutValidation() {
        ServiceProviderDTO dto = new ServiceProviderDTO();
        String blank = "   ";

        dto.setCustomParameters(blank);

        assertEquals(blank, dto.getCustomParameters());
    }

    ServiceProviderDTO getBasicServiceProviderDto() {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setSystemId("System1");
        serviceProviderDTO.setPasswd("pAssW0rd");
        serviceProviderDTO.setProtocol("SMPP");
        serviceProviderDTO.setBindType("TRANSCEIVER");
        serviceProviderDTO.setCallbackUrl("http://127.0.0.1/URL");
        serviceProviderDTO.setSmppServerId(1);
        return serviceProviderDTO;
    }

    void testServiceProviderByEnabledStatus(boolean isStarted, String protocol) {
        ServiceProviderDTO serviceProviderDTO = getBasicServiceProviderDto();
        serviceProviderDTO.setEnabled(isStarted ? 1 : 0);
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("System1");
        serviceProvider.setEnabled(isStarted ? 0 : 1);
        serviceProvider.setNetworkId(10);
        serviceProvider.setProtocol(protocol);
        when(serviceProviderRepository.findById(anyInt())).thenReturn(serviceProvider);
        when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
        when(callbackHeaderRepo.findByNetworkId(anyInt())).thenReturn(List.of(new CallbackHeaderHttp()));
        when(serviceProviderMapper.toServiceProviderDTO(serviceProvider)).thenReturn(new RedisServiceProviderDTO());
        when(serviceProviderMapper.toDTO(serviceProvider)).thenReturn(new ParseServiceProviderDTO());
        ApiResponse response = serviceProviderService.update(-1, serviceProviderDTO);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    void socketAndRedisActionCaseTest(int enabledState, boolean usesApiKey) {
        when(serviceProviderRepository.findById(anyInt())).thenReturn(new ServiceProvider());
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setEnabled(enabledState);
        if (usesApiKey) {
            redisServiceProviderDTO.setAuthenticationTypes("Api-key");
        }
        when(serviceProviderMapper.toServiceProviderDTO(any())).thenReturn(redisServiceProviderDTO);
        if (enabledState <= 2) {
            boolean socketAndRedisActionResult = serviceProviderService.socketAndRedisAction(-1);
            assertTrue(socketAndRedisActionResult);
        } else {
            assertThrows(IllegalArgumentException.class, () -> serviceProviderService.socketAndRedisAction(-1));
        }
    }

    void validateTokenAuthorizationUserAndPasswordTest(String key, String value) {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setProtocol("http");
        serviceProviderDTO.setAuthenticationTypes("basic");
        if (key.equalsIgnoreCase("password")) {
            serviceProviderDTO.setPasswd(value);
            assertThrows(Exception.class, () -> serviceProviderService.validateTokenAuthorization(serviceProviderDTO));
        } else {
            serviceProviderDTO.setPasswd("PAssW0Rd");
            serviceProviderDTO.setUserName(value);
            if (value != null && !value.isBlank()) {
                assertDoesNotThrow(() -> serviceProviderService.validateTokenAuthorization(serviceProviderDTO));
            } else {
                assertThrows(Exception.class, () -> serviceProviderService.validateTokenAuthorization(serviceProviderDTO));
            }
        }
    }

    void validateTokenAuthorizationNoBasicAuthTest(String authorizationType, String token) {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setProtocol("http");
        serviceProviderDTO.setAuthenticationTypes(authorizationType);
        serviceProviderDTO.setToken(token);
        if (token != null && !token.isBlank()) {
            assertDoesNotThrow(() -> serviceProviderService.validateTokenAuthorization(serviceProviderDTO));
        } else {
            assertThrows(Exception.class, () -> serviceProviderService.validateTokenAuthorization(serviceProviderDTO));
        }
    }

    @Test
    @DisplayName("generateHttpSecurityToken when generation succeeds then return OK")
    void generateHttpSecurityTokenWhenGenerationSucceedsThenReturnOk() {
        GenerateServiceProviderSecurityTokenRequestDTO request = new GenerateServiceProviderSecurityTokenRequestDTO();
        request.setSecurityAuthenticationType(Constants.AUTH_TYPE_BEARER);
        request.setBearerTokenExpirationSeconds(600L);

        GeneratedServiceProviderSecurityTokenDTO generatedToken =
                GeneratedServiceProviderSecurityTokenDTO.builder()
                        .headerName(Constants.DEFAULT_AUTHORIZATION_HEADER)
                        .authenticationType(Constants.AUTH_TYPE_BEARER)
                        .token("generated-bearer-token")
                        .build();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setNetworkId(101);
        serviceProvider.setProtocol(Constants.PROTOCOL_HTTP);
        serviceProvider.setEnabled(Constants.ACTIVE_ENABLED_STATUS);

        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setNetworkId(101);
        redisServiceProviderDTO.setProtocol(Constants.PROTOCOL_HTTP);
        redisServiceProviderDTO.setEnabled(Constants.ACTIVE_ENABLED_STATUS);

        when(serviceProviderHttpSecurityService.generateAndPersistToken(101, request)).thenReturn(generatedToken);
        when(serviceProviderRepository.findById(101)).thenReturn(serviceProvider);
        when(callbackHeaderRepo.findByNetworkId(101)).thenReturn(List.of());
        when(serviceProviderMapper.toServiceProviderDTO(serviceProvider)).thenReturn(redisServiceProviderDTO);
        when(utilsBase.findUpdateEndpointByProtocolToSP(Constants.PROTOCOL_HTTP)).thenReturn("/service-provider/update");

        ApiResponse response = serviceProviderService.generateHttpSecurityToken(101, request);

        assertNotNull(response);
        assertEquals(200, response.status());
    }
}
