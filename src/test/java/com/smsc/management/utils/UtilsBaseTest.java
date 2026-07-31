package com.smsc.management.utils;

import com.smsc.management.app.errorcode.dto.ParseErrorCodeMappingDTO;
import com.smsc.management.app.routing.dto.RedisRoutingRulesDTO;
import com.smsc.management.app.routing.dto.RedisRoutingRulesDestinationDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO;
import com.smsc.management.app.routing.mapper.RoutingRulesMapper;
import com.smsc.management.app.routing.model.entity.RoutingRules;
import com.smsc.management.app.routing.model.repository.RoutingRulesDestinationRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.app.settings.model.entity.CommonVariables;
import com.smsc.management.app.settings.model.repository.CommonVariablesRepository;
import com.smsc.management.exception.InvalidStructureException;
import com.smsc.management.exception.SmscBackendException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.paicbd.smsc.utils.RedisManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.smsc.management.utils.Constants.DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT;
import static com.smsc.management.utils.Constants.DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static com.smsc.management.utils.Constants.KEY_MAX_PASSWORD_LENGTH;
import static com.smsc.management.utils.Constants.KEY_MAX_SYSTEM_ID_LENGTH;
import static com.smsc.management.utils.Constants.SMSC_ACCOUNT_SETTINGS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilsBaseTest {
    @Mock
    RedisManager redisManager;
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    RoutingRulesRepository routingRulesRepo;
    @Mock
    RoutingRulesDestinationRepository routingRulesDestRepo;
    @Mock
    SequenceNetworksIdRepository sequenceNetworkRepo;
    @Mock
    RoutingRulesMapper routingRulesMapper;
    @Mock
    CommonVariablesRepository commonVariablesRepository;

    @InjectMocks
    UtilsBase service;

    @Test
    void testGetErrorCodeMappings() {
        List<Map<String, Object>> mapData = new ArrayList<>();

        Map<String, Object> entry1 = new HashMap<>();
        entry1.put("errorCodeMappingId", 1);
        entry1.put("operatorMnoId", 101);
        entry1.put("operatorMno", "OperatorA");
        entry1.put("errorCodeId", 201);
        entry1.put("errorCode", "ErrorA");
        entry1.put("deliveryErrorCodeId", 301);
        entry1.put("deliveryErrorCode", "DeliveryErrorA");
        entry1.put("deliveryStatus", "StatusA");

        Map<String, Object> entry2 = new HashMap<>();
        entry2.put("errorCodeMappingId", 2);
        entry2.put("operatorMnoId", 102);
        entry2.put("operatorMno", "OperatorB");
        entry2.put("errorCodeId", 202);
        entry2.put("errorCode", "ErrorB");
        entry2.put("deliveryErrorCodeId", 302);
        entry2.put("deliveryErrorCode", "DeliveryErrorB");
        entry2.put("deliveryStatus", "StatusB");

        mapData.add(entry1);
        mapData.add(entry2);

        List<ParseErrorCodeMappingDTO> result = service.getErrorCodeMappings(mapData);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testValidateDestinationRulesWithValidRules() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 100, 1, "Name1", 10, 0, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 101, 2, "Name2", 11, 0, "Type2"));
        destinationRules.add(new RoutingRulesDestinationDTO(3, 102, 3, "Name3", 12, 0, "Type3")); // different priority and networkId, valid

        assertDoesNotThrow(() -> service.validateDestinationRules(destinationRules));
    }

    @Test
    void testValidateDestinationRulesWithDuplicatePriorities() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 100, 1, "Name1", 10, 0, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 100, 2, "Name2", 11, 0, "Type2")); // duplicate priority

        InvalidStructureException exception = assertThrows(InvalidStructureException.class, () ->
                service.validateDestinationRules(destinationRules));

        assertEquals("Priority 100 duplicate", exception.getMessage());
    }

    @Test
    void testValidateDestinationRulesWithDuplicateNetworkIds() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 100, 1, "Name1", 10, 0, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 101, 1, "Name2", 11, 0, "Type2")); // duplicate networkId

        InvalidStructureException exception = assertThrows(InvalidStructureException.class, () ->
                service.validateDestinationRules(destinationRules));

        assertEquals("The network_id 1 is duplicated", exception.getMessage());
    }

    @Test
    void testValidateDestinationRulesWithActionEqualsOne() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 100, 1, "Name1", 10, 0, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 100, 1, "Name2", 11, 1, "Type2")); // action == 1, should not validate duplicates

        assertDoesNotThrow(() -> service.validateDestinationRules(destinationRules));
    }

    @Test
    void testValidateDestinationRulesWithNullFields() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 0, 1, "Name1", 10, 0, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 0, 1, "Name2", 11, 0, "Type2")); // Invalid due to null priority

        InvalidStructureException exception = assertThrows(InvalidStructureException.class, () ->
                service.validateDestinationRules(destinationRules));

        assertEquals("Priority 0 duplicate", exception.getMessage());
    }

    @Test
    void testValidateDestinationRulesWithAllActionsEqualsOne() {
        List<RoutingRulesDestinationDTO> destinationRules = new ArrayList<>();

        destinationRules.add(new RoutingRulesDestinationDTO(1, 100, 1, "Name1", 10, 1, "Type1"));
        destinationRules.add(new RoutingRulesDestinationDTO(2, 100, 1, "Name2", 11, 1, "Type2")); // all actions are 1, no duplicates check

        assertDoesNotThrow(() -> service.validateDestinationRules(destinationRules));
    }

    @Test
    void testValidateMaxLengthSpAndGw() {
        CommonVariables data = new CommonVariables();
        data.setDataType("json");
        data.setKey(SMSC_ACCOUNT_SETTINGS);
        data.setValue("{\"" + KEY_MAX_PASSWORD_LENGTH +"\": 9, \"" + KEY_MAX_SYSTEM_ID_LENGTH + "\": 15}");
        Mockito.when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(data);
        assertDoesNotThrow(() -> service.validateMaxLengthSpAndGw("passwd123", "networkId", "HTTP"));
    }

    @Test
    void testValidateMaxLengthSpAndGw_handleThrowExceptionForPasswd() {
        CommonVariables data = new CommonVariables();
        data.setDataType("json");
        data.setKey(SMSC_ACCOUNT_SETTINGS);
        data.setValue("{\"" + KEY_MAX_PASSWORD_LENGTH +"\": 5, \"" + KEY_MAX_SYSTEM_ID_LENGTH + "\": 15}");
        Mockito.when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(data);
        assertThrows(IllegalArgumentException.class, () -> service.validateMaxLengthSpAndGw("passwd121111113", "networkId", "SMPP"));
    }

    @Test
    void testValidateMaxLengthSpAndGw_handleThrowExceptionForSystemId() {
        CommonVariables data = new CommonVariables();
        data.setDataType("json");
        data.setKey(SMSC_ACCOUNT_SETTINGS);
        data.setValue("{\"" + KEY_MAX_PASSWORD_LENGTH +"\": 9, \"" + KEY_MAX_SYSTEM_ID_LENGTH + "\": 5}");
        Mockito.when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(data);
        assertThrows(IllegalArgumentException.class, () -> service.validateMaxLengthSpAndGw("passwd123", "networkId", "HTTP"));
    }

    @Test
    void testIsRequestDlrAndTransmitterBind() {
        assertThrows(IllegalArgumentException.class, () -> service.isRequestDlrAndTransmitterBind("TRANSMITTER", 1, "SMPP"));
        assertDoesNotThrow(() -> service.isRequestDlrAndTransmitterBind("RECEIVER", 1, "HTTP"));
    }

    @Test
    void validateNetworksParameter() {
        RoutingRulesDTO routingRulesDTO = new RoutingRulesDTO();
        routingRulesDTO.setDropTempFailure(true);
        routingRulesDTO.setNetworkIdTempFailure(null);
        assertThrows(SmscBackendException.class, () -> service.validateNetworksParameter(routingRulesDTO));

        routingRulesDTO.setDropTempFailure(false);
        routingRulesDTO.setNetworkIdTempFailure(1);
        assertDoesNotThrow(() -> service.validateNetworksParameter(routingRulesDTO));

        routingRulesDTO.setDropTempFailure(true);
        routingRulesDTO.setNetworkIdTempFailure(1);
        assertDoesNotThrow(() -> service.validateNetworksParameter(routingRulesDTO));
    }

    @Test
    void storeInRedisFail() {
        when(redisManager.hset(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> service.storeInRedis("key", "field", "value"));
    }

    @Test
    void removeInRedisFail() {
        when(redisManager.hdel(anyString(), anyString())).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> service.removeInRedis("key", "field"));
    }

    @Test
    void getAllInRedisOk() {
        Map<String, String> map = new HashMap<>();
        map.put("field", "value");
        when(redisManager.hgetAll(anyString())).thenReturn(map);
        assertNotNull(service.getAllInRedis("key"));
        assertEquals(1, service.getAllInRedis("key").size());
    }

    @Test
    void getAllInRedisFail() {
        when(redisManager.hgetAll(anyString())).thenThrow(new RuntimeException());
        assertNotNull(service.getAllInRedis("key"));
        assertEquals(0, service.getAllInRedis("key").size());
    }

    @Test
    void sendNotificationSocketFail() {
        doThrow(new RuntimeException()).when(simpMessagingTemplate).convertAndSend(anyString(), anyString());
        assertDoesNotThrow(() -> service.sendNotificationSocket("topic", "message"));
    }

    @Test
    void getInRedisFail() {
        when(redisManager.hget(anyString(), anyString())).thenThrow(new RuntimeException());
        assertNull(service.getInRedis("hashName", "key"));
    }

    @Test
    void findDeleteEndpointByProtocolToSPOk() {
        String protocol = "SMPP";
        assertEquals(DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT, service.findDeleteEndpointByProtocolToSP(protocol));

        protocol = "HTTP";
        assertEquals(DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT, service.findDeleteEndpointByProtocolToSP(protocol));
    }

    @Test
    void findDeleteGwEndpointByProtocolToGw() {
        String protocol = "SMPP";
        assertEquals(Constants.DELETE_GATEWAY_ENDPOINT, service.findDeleteEndpointByProtocolToGw(protocol));

        protocol = "HTTP";
        assertEquals(Constants.DELETE_HTTP_GATEWAY_ENDPOINT, service.findDeleteEndpointByProtocolToGw(protocol));
    }

    @Test
    void findEndpointToGatewayOk() {
        String protocol = "SMPP";
        assertEquals(Constants.STOP_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 0));
        assertEquals(Constants.CONNECT_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 1));
        assertEquals(Constants.DELETE_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 2));

        protocol = "HTTP";
        assertEquals(Constants.STOP_HTTP_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 0));
        assertEquals(Constants.CONNECT_HTTP_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 1));
        assertEquals(Constants.DELETE_HTTP_GATEWAY_ENDPOINT, service.findEndpointToGateway(protocol, 2));
    }

    @Test
    void findEndpointToGateway_ToThrowException() {
        String protocol = "SMPP";
        assertThrows(SmscBackendException.class, () -> service.findEndpointToGateway(protocol, 3));
    }


    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testHasFilterRules(boolean sourceAddrBlank, boolean sourceAddrTonBlank, boolean sourceAddrNpiBlank,
                            boolean destAddrBlank, boolean destAddrTonBlank, boolean destAddrNpiBlank,
                            boolean imsiDigitsMaskBlank, boolean networkNodeNumberBlank, boolean callingPartyAddressBlank,
                            boolean sriResponse, boolean expectedResult) {
        RedisRoutingRulesDTO dto = new RedisRoutingRulesDTO();
        dto.setRegexSourceAddr(sourceAddrBlank ? " " : "valid");
        dto.setRegexSourceAddrTon(sourceAddrTonBlank ? " " : "valid");
        dto.setRegexSourceAddrNpi(sourceAddrNpiBlank ? " " : "valid");
        dto.setRegexDestinationAddr(destAddrBlank ? " " : "valid");
        dto.setRegexDestAddrTon(destAddrTonBlank ? " " : "valid");
        dto.setRegexDestAddrNpi(destAddrNpiBlank ? null : "valid");
        dto.setRegexImsiDigitsMask(imsiDigitsMaskBlank ? " " : "valid");
        dto.setRegexNetworkNodeNumber(networkNodeNumberBlank ? " " : "valid");
        dto.setRegexCallingPartyAddress(callingPartyAddressBlank ? " " : "valid");
        dto.setSriResponse(sriResponse);

        assertEquals(expectedResult, service.hasFilterRules(dto));
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
                Arguments.of(true, false, false, false, false, false, false, false, false, true, true),
                Arguments.of(false, true, false, false, false, false, false, false, false, false, true),
                Arguments.of(false, false, true, false, false, false, false, false, false, false, true),
                Arguments.of(false, false, false, true, false, false, false, false, false, false, true),
                Arguments.of(false, false, false, false, true, false, false, false, false, false, true),
                Arguments.of(false, false, false, false, false, true, false, false, false, false, true),
                Arguments.of(false, false, false, false, false, false, true, false, false, false, true),
                Arguments.of(false, false, false, false, false, false, false, true, false, false, true),
                Arguments.of(false, false, false, false, false, false, false, false, true, false, true),
                Arguments.of(false, false, false, false, false, false, false, false, false, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideActionRulesTestCases")
    void testHasActionRules(boolean sourceAddrBlank, int sourceAddrTon, int sourceAddrNpi,
                            boolean destAddrBlank, int destAddrTon, int destAddrNpi,
                            boolean addSourcePrefixBlank, boolean addDestPrefixBlank,
                            boolean removeDestPrefixBlank, boolean gtSccpAddrMtBlank,
                            boolean checkSriResponse, boolean expectedResult) {
        RedisRoutingRulesDTO dto = new RedisRoutingRulesDTO();
        dto.setNewSourceAddr(sourceAddrBlank ? " " : "valid");
        dto.setNewSourceAddrTon(sourceAddrTon);
        dto.setNewSourceAddrNpi(sourceAddrNpi);
        dto.setNewDestinationAddr(destAddrBlank ? null : "valid");
        dto.setNewDestAddrTon(destAddrTon);
        dto.setNewDestAddrNpi(destAddrNpi);
        dto.setAddSourceAddrPrefix(addSourcePrefixBlank ? " " : "valid");
        dto.setAddDestAddrPrefix(addDestPrefixBlank ? " " : "valid");
        dto.setRemoveDestAddrPrefix(removeDestPrefixBlank ? " " : "valid");
        dto.setNewGtSccpAddr(gtSccpAddrMtBlank ? " " : "valid");
        dto.setCheckSriResponse(checkSriResponse);

        assertEquals(expectedResult, service.hasActionRules(dto));
    }

    private static Stream<Arguments> provideActionRulesTestCases() {
        return Stream.of(
                Arguments.of(true, 1, 1, false, 1, 1, false, false, false, false, true, true),
                Arguments.of(false, 1, 1, true, 1, 1, false, false, false, false, false, true),
                Arguments.of(false, 1, 1, false, 1, 1, true, false, false, false, false, true),
                Arguments.of(false, 1, 1, false, 1, 1, false, true, false, false, false, true),
                Arguments.of(false, 1, 1, false, 1, 1, false, false, true, false, false, true),
                Arguments.of(false, 1, 1, false, 1, 1, false, false, false, true, false, true),
                Arguments.of(false, 1, 1, false, 1, 1, false, false, false, false, true, true)
        );
    }

    @Test
    void testHasActionRules_ReturnsFalse_WhenAllFieldsAreDefault() {
        RedisRoutingRulesDTO dto = new RedisRoutingRulesDTO();
        dto.setNewSourceAddr("");
        dto.setNewSourceAddrTon(-1);
        dto.setNewSourceAddrNpi(-1);
        dto.setNewDestinationAddr("");
        dto.setNewDestAddrTon(-1);
        dto.setNewDestAddrNpi(-1);
        dto.setAddSourceAddrPrefix("");
        dto.setAddDestAddrPrefix("");
        dto.setRemoveDestAddrPrefix("");
        dto.setNewGtSccpAddr("");
        dto.setCheckSriResponse(false);

        assertFalse(service.hasActionRules(dto));

        dto.setCheckSriResponse(true);
        assertTrue(service.hasActionRules(dto));
    }


    @Test
    void getRoutingRules() {
        int networkId = 1;
        String protocol = "SMPP";
        List<RoutingRules> routingRulesEntity = new ArrayList<>();
        RoutingRules routingRule = new RoutingRules();
        routingRule.setId(1);
        routingRule.setOriginNetworkId(1);
        routingRule.setNetworkIdToMapSri(1);
        routingRule.setNetworkIdToPermanentFailure(1);
        routingRule.setNetworkIdTempFailure(1);
        routingRulesEntity.add(routingRule);
        SequenceNetworksId seqNet = new SequenceNetworksId();
        seqNet.setNetworkType("networkType");
        when(routingRulesRepo.findByOriginNetworkId(networkId)).thenReturn(routingRulesEntity);
        when(sequenceNetworkRepo.findById(networkId)).thenReturn(seqNet);
        List<RedisRoutingRulesDestinationDTO> routingRulesDestinations = new ArrayList<>();
        when(routingRulesDestRepo.findByRoutingRulesIdDTO(routingRule.getId())).thenReturn(routingRulesDestinations);
        when(routingRulesMapper.toRedisDTO(routingRule)).thenReturn(new RedisRoutingRulesDTO());
        assertNotNull(service.getRoutingRules(networkId, protocol));
    }

    @Test
    void getRoutingRules_handleException() {
        int networkId = 1;
        String protocol = "SMPP";
        when(routingRulesRepo.findByOriginNetworkId(networkId)).thenThrow(new RuntimeException());
        assertNotNull(service.getRoutingRules(networkId, protocol));
    }

    @Test
    void getRoutingRules_getNetworkIdToMapSriNull_getNetworkIdToPermanentFailureNull_getNetworkIdTempFailureNull() {
        int networkId = 1;
        String protocol = "SMPP";
        List<RoutingRules> routingRulesEntity = new ArrayList<>();
        RoutingRules routingRule = new RoutingRules();
        routingRule.setId(1);
        routingRule.setOriginNetworkId(1);
        routingRule.setNetworkIdToMapSri(null);
        routingRule.setNetworkIdToPermanentFailure(null);
        routingRule.setNetworkIdTempFailure(null);
        routingRulesEntity.add(routingRule);
        SequenceNetworksId seqNet = new SequenceNetworksId();
        seqNet.setNetworkType("networkType");
        when(routingRulesRepo.findByOriginNetworkId(networkId)).thenReturn(routingRulesEntity);
        when(sequenceNetworkRepo.findById(networkId)).thenReturn(seqNet);
        List<RedisRoutingRulesDestinationDTO> routingRulesDestinations = new ArrayList<>();
        when(routingRulesDestRepo.findByRoutingRulesIdDTO(routingRule.getId())).thenReturn(routingRulesDestinations);
        when(routingRulesMapper.toRedisDTO(routingRule)).thenReturn(new RedisRoutingRulesDTO());
        assertNotNull(service.getRoutingRules(networkId, protocol));
    }

    @Test
    void shouldNotThrowExceptionWhenBindTypeIsNotTransmitter() {
        assertDoesNotThrow(() -> service.isRequestDlrAndTransmitterBind("RECEIVER", 1, "SMPP"));
    }

    @Test
    void shouldNotThrowExceptionWhenIsRequestDrlIsFalse() {
        assertDoesNotThrow(() -> service.isRequestDlrAndTransmitterBind("TRANSMITTER", 0, "SMPP"));
    }

    @Test
    void shouldNotThrowExceptionWhenProtocolIsNotSMPP() {
        assertDoesNotThrow(() -> service.isRequestDlrAndTransmitterBind("TRANSMITTER", 1, "HTTP"));
    }

    @Test
    void shouldThrowExceptionWhenConditionsAreMet() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> service.isRequestDlrAndTransmitterBind("TRANSMITTER", 1, "SMPP"));
        assertEquals("you must disable the DLR request for the bind type TRANSMITTER", exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenBindTypeIsDifferentCase() {
        assertDoesNotThrow(() -> service.isRequestDlrAndTransmitterBind("transmitter", 0, "SMPP"));
    }

    @Test
    void shouldThrowExceptionWhenCommonVariablesNotFound() {
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(null);
        SmscBackendException exception = assertThrows(SmscBackendException.class,
                () -> service.validateMaxLengthSpAndGw("password", "systemId", "SMPP"));
        assertEquals("max password length setting was not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordTooLongForSmpp() {
        CommonVariables commonVariablesThrown = new CommonVariables();
        commonVariablesThrown.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariablesThrown);
        assertThrows(IllegalArgumentException.class,
                () -> service.validateMaxLengthSpAndGw("toolongpassword", "systemId", "SMPP"));


        CommonVariables commonVariablesOk = new CommonVariables();
        commonVariablesOk.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariablesOk);

        assertDoesNotThrow(() -> service.validateMaxLengthSpAndGw("validpwd", "systemId", "SMPP"));

        CommonVariables commonVariablesLong = new CommonVariables();
        commonVariablesLong.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariablesLong);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateMaxLengthSpAndGw("passwd", "toolongsystemid", "SMPP"));
    }

    @Test
    void shouldNotThrowExceptionWhenSystemIdWithinLimits() {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariables);

        assertDoesNotThrow(() -> service.validateMaxLengthSpAndGw("passwd", "validId", "SMPP"));
    }

    @Test
    void shouldNotThrowExceptionWhenProtocolIsNotSmpp() {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariables);

        assertDoesNotThrow(() -> service.validateMaxLengthSpAndGw("toolongpassword", "validId", "HTTP"));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsBlankForSmpp() {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariables);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.validateMaxLengthSpAndGw("", "validId", "SMPP"));

        assertEquals("Password must be between 1 and 8 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSystemIdIsBlank() {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setValue("{\"max_password_length\": 8, \"max_system_id_length\": 12}");
        when(commonVariablesRepository.findByKey(SMSC_ACCOUNT_SETTINGS)).thenReturn(commonVariables);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.validateMaxLengthSpAndGw("validpwd", "", "SMPP"));

        assertEquals("System id must be between 1 and 12 characters", exception.getMessage());
    }

    @Test
    void convertStringUTCToLocalTimeZoneTest() {
        String convertedDate = service.convertStringUTCToLocalTimeZone("2025-07-11T07:17:21.080", "yyyy-MM-dd'T'HH:mm:ss.SSS");
        assertNotNull(convertedDate);
        assertFalse(convertedDate.isBlank());

        convertedDate = service.convertStringUTCToLocalTimeZone("2025-07-11 07:17:21", "yyyy-MM-dd HH:mm:ss");
        assertNotNull(convertedDate);
        assertFalse(convertedDate.isBlank());
    }
}