package com.smsc.management.app.gateway.service;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.gateway.dto.ParseGatewaysDTO;
import com.smsc.management.app.gateway.mapper.GatewaysMapper;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.headers.model.repository.CallbackHeaderHttpRepository;
import com.smsc.management.app.interpreter.mapper.InterpreterMapper;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import com.smsc.management.app.interpreter.service.DefaultTemplateService;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.app.sequence.SequenceNetworksIdGenerator;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;

import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.DISABLED_ENABLED_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class GatewaysServiceTest {

    @Mock
    GatewaysRepository gatewaysRepo;

    @Mock
    GatewaysMapper gatewaysMapper;

    @Mock
    InterpreterMapper interpreterMapper;

    @Mock
    UtilsBase utilsBase;

    @Mock
    OperatorMnoRepository operatorRepo;

    @Mock
    SequenceNetworksIdGenerator seqGateway;

    @Mock
    InterpreterRepository interpreterRepository;

    @Mock
    CallbackHeaderHttpRepository callbackHeaderRepo;

    @InjectMocks
    GatewaysService gatewaysService;

    @Test
    @DisplayName("create when an exception occurs then a 5xx error is returned")
    void createWhenFailsThenReturn5xxError() {
        when(gatewaysRepo.existsByIpAndPortAndSystemTypeAndInterfaceVersionAndBindTypeAndEnabledNotAndSystemId(
                anyString(), anyInt(), anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(false);
        when(gatewaysMapper.toEntity(any())).thenReturn(new Gateways());
        when(gatewaysRepo.save(any())).thenThrow(new RuntimeException("RuntimeException"));
        GatewaysDTO mockGatewayDTO = new GatewaysDTO();
        mockGatewayDTO.setProtocol("SMPP");
        ApiResponse response = gatewaysService.create(mockGatewayDTO);
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("getGateways when an exception occurs then a 5xx error is returned")
    void getGatewaysWhenFailsThenReturn5xxError() {
        when(gatewaysRepo.findByEnabledNot(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = gatewaysService.getGateways();
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("getGatewayByExternalId when the gateway was not found then a 4xx error is returned")
    void getGatewayByExternalIdWhenNotFoundThenReturn4xxError() {
        when(gatewaysRepo.findByExternalId(anyString())).thenReturn(null);
        ApiResponse response = gatewaysService.getGatewayByExternalId(anyString());
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    @DisplayName("getGatewayByExternalId when fails then a 5xx error is returned")
    void getGatewayByExternalIdWhenFailsThenReturn5xxError() {
        when(gatewaysRepo.findByExternalId(anyString())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = gatewaysService.getGatewayByExternalId(anyString());
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("create when the gateway already exists then a 4xx error is returned")
    void createWhenGatewayExistsThenReturn4xxError() {
        createReferencedGatewaysTest("190.212.1.25", 2888, "SMPP");
        createReferencedGatewaysTest("10.10.5.25", 2822, "HTTP");
    }

    @Test
    @DisplayName("create when falling into a data integrity violation issue then a DataIntegrityViolationException is thrown")
    void createWhenDataViolationThenViolationExceptionIsThrown() {
        when(gatewaysRepo.existsByIpAndPortAndSystemTypeAndInterfaceVersionAndBindTypeAndEnabledNotAndSystemId(
                anyString(), anyInt(), anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(true);
        when(gatewaysMapper.toEntity(any())).thenReturn(new Gateways());
        when(gatewaysRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setProtocol("SMPP");
        assertThrows(DataIntegrityViolationException.class, () -> gatewaysService.create(gatewaysDTO));
    }

    @Test
    @DisplayName("update when updating to set like deleting gateway")
    void updateGatewayWhenEnabledIsDeletedThenDoItSuccessfully() {
        Gateways testGateway = new Gateways();
        testGateway.setNetworkId(1);
        testGateway.setEnabled(DISABLED_ENABLED_STATUS);
        testGateway.setProtocol("SMPP");
        testGateway.setSystemId("testSystemId");
        testGateway.setMnoId(1);

        Gateways responseGw = new Gateways();
        responseGw.setProtocol("SMPP");
        responseGw.setEnabled(DELETED_ENABLED_STATUS);
        responseGw.setSystemId("testSystemId");
        responseGw.setMnoId(1);
        responseGw.setNetworkId(1);
        responseGw.setProtocol("SMPP");
        responseGw.setIp("http://localhost:8080/callback");
        responseGw.setAuthenticationTypes("undefined");

        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setNetworkId(1);
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setEnabled(DELETED_ENABLED_STATUS);
        gatewaysDTO.setIp("127.0.0.1");
        gatewaysDTO.setPort(9000);
        gatewaysDTO.setSystemType("SMPP");
        gatewaysDTO.setProtocol("SMPP");
        gatewaysDTO.setInterfaceVersion("1.2.3.4");
        gatewaysDTO.setBindType("RECEIVER");
        gatewaysDTO.setMnoId(1);

        ParseGatewaysDTO parseGatewaysDTO = new ParseGatewaysDTO();
        parseGatewaysDTO.setNetworkId(1);
        parseGatewaysDTO.setIp("127.0.0.1");
        parseGatewaysDTO.setPort(9000);
        parseGatewaysDTO.setMnoId(1);

        OperatorMno mno = new OperatorMno();
        mno.setId(1);
        mno.setTlvMessageReceiptId(true);
        mno.setEnabled(true);

        when(gatewaysRepo.findById(anyInt())).thenReturn(testGateway);
        when(gatewaysRepo.save(any(Gateways.class))).thenReturn(responseGw);
        when(operatorRepo.findById(1)).thenReturn(mno);
        when(gatewaysMapper.toDTO(any(Gateways.class))).thenReturn(parseGatewaysDTO);

        ApiResponse response = gatewaysService.update(1, gatewaysDTO);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @DisplayName("update when updating an deleted gateway then a 4xx error is returned")
    void updateWhenGatewayIsDeletedThenReturn4xxError() {
        Gateways testGateway = new Gateways();
        testGateway.setEnabled(DELETED_ENABLED_STATUS);
        when(gatewaysRepo.findById(anyInt())).thenReturn(testGateway);
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setProtocol("HTTP");
        gatewaysDTO.setIp("127.0.0.1");
        gatewaysDTO.setAuthenticationTypes("undefined");

        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    @DisplayName("update when updating a non existing gateway then a 4xx error is returned")
    void updateWhenGatewayNotFoundThenReturn4xxError() {
        when(gatewaysRepo.findById(anyInt())).thenReturn(null);
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setProtocol("HTTP");
        gatewaysDTO.setIp("127.0.0.1");
        gatewaysDTO.setAuthenticationTypes("undefined");
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
        assertEquals(404, response.status());
        response = gatewaysService.update("External-ID-001", gatewaysDTO);
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    @DisplayName("update when updating an existing gateway with the properties of another one then a 4xx error is returned")
    void updateWhenExistingGatewayObjectThenReturn4xxError() {
        updateReferencesGatewaysTest("190.212.0.22", 2777, "SMPP");
        updateReferencesGatewaysTest("192.168.2.88", 2778, "HTTP");
    }

    @Test
    @DisplayName("update when falling into a data integrity violation issue then a DataIntegrityViolationException is thrown")
    void updateWhenDataViolationThenViolationExceptionIsThrown() {
        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());
        when(gatewaysRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setProtocol("HTTP");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setIp("127.0.0.1");
        gatewaysDTO.setAuthenticationTypes("undefined");
        assertThrows(DataIntegrityViolationException.class, () -> gatewaysService.update(-1, gatewaysDTO));
    }

    @Test
    @DisplayName("update when fails then a 5xx error is returned")
    void updateWhenFailsThenReturn5xxError() {
        when(gatewaysRepo.existsByGatewaySearchCriteria(
                any())).thenReturn(false);
        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());
        when(gatewaysRepo.save(any())).thenThrow(new RuntimeException());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setProtocol("SMPP");
        gatewaysDTO.setSystemId("SystemID");
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("update when operations performed to redis then return response")
    void updateWhenRedisActionThenReturnResponse() {
        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setProtocol("SMPP");
        gatewaysDTO.setIp("http://localhost:8080/callback");
        gatewaysDTO.setAuthenticationTypes("undefined");

        Gateways gateways = new Gateways();
        gateways.setNetworkId(10);
        gateways.setSystemId("SystemId3");
        gateways.setProtocol("SMPP");
        gateways.setIp("http://localhost:8080/callback");
        gateways.setAuthenticationTypes("undefined");
        when(gatewaysMapper.toDTO(gateways)).thenReturn(new ParseGatewaysDTO());
        when(gatewaysRepo.findById(anyInt())).thenReturn(gateways);
        when(gatewaysRepo.save(any())).thenReturn(new Gateways());
        when(operatorRepo.findById(anyInt())).thenReturn(new OperatorMno());
        OperatorMno operatorMno = new OperatorMno();
        operatorMno.setTlvMessageReceiptId(Boolean.TRUE);
        operatorMno.setMessageIdDecimalFormat(Boolean.TRUE);
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
    }

    @Test
    @DisplayName("update when the gateway is enabled the return response")
    void updateWhenGatewayIsEnabledThenReturnResponse() {
        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setEnabled(3);
        gatewaysDTO.setProtocol("SMPP");
        gatewaysDTO.setIp("http://localhost:8080/callback");
        gatewaysDTO.setAuthenticationTypes("undefined");

        Gateways gateways = new Gateways();
        gateways.setNetworkId(10);
        gateways.setSystemId("SystemId3");
        gateways.setProtocol("SMPP");
        gateways.setIp("http://localhost:8080/callback");
        gateways.setAuthenticationTypes("undefined");
        when(gatewaysMapper.toDTO(gateways)).thenReturn(new ParseGatewaysDTO());
        when(gatewaysRepo.findById(anyInt())).thenReturn(gateways);
        when(gatewaysRepo.save(any())).thenReturn(new Gateways());
        when(operatorRepo.findById(anyInt())).thenReturn(new OperatorMno());
        OperatorMno operatorMno = new OperatorMno();
        operatorMno.setTlvMessageReceiptId(Boolean.TRUE);
        operatorMno.setMessageIdDecimalFormat(Boolean.TRUE);
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
    }

    @Test
    @DisplayName("update when the gateway enabled value is zero then return OK response")
    void updateWhenGatewayEnabledZeroThenReturnOkMessage() {
        testGatewayEnabledNotPreviousTest(0, 1);
    }

    @Test
    @DisplayName("update when the gateway enabled value is one then return OK response")
    void updateWhenGatewayEnabledOneThenReturnOkMessage() {
        testGatewayEnabledNotPreviousTest(1, 0);
    }

    @Test
    @DisplayName("update when the gateway enabled value is two then return OK response")
    void updateWhenGatewayEnabledTwoThenReturnOkMessage() {
        testGatewayEnabledNotPreviousTest(2, 0);
    }

    @Test
    @DisplayName("onlyToLoadInitInRedisAndSocket when is a valid gateway then return response")
    void onlyToLoadInitInRedisAndSocketWhenStoreInRedisThenReturnResponse() {
        Gateways gateways = new Gateways();
        gateways.setSystemId("SystemId");
        gateways.setNetworkId(1);

        List<Interpreter> interpreterList = new ArrayList<>();

        Interpreter interpreterMessageInput = new Interpreter();
        interpreterMessageInput.setId(1);
        interpreterMessageInput.setDirection("input");
        interpreterMessageInput.setEventType("message");
        interpreterMessageInput.setBodyType("JSON");
        interpreterMessageInput.setTemplate("{}");
        interpreterMessageInput.setGatewayId(1);
        interpreterList.add(interpreterMessageInput);

        when(gatewaysRepo.findById(anyInt())).thenReturn(gateways);
        when(operatorRepo.findById(anyInt())).thenReturn(new OperatorMno());
        when(gatewaysMapper.toDTO(gateways)).thenReturn(new ParseGatewaysDTO());
        when(interpreterRepository.findByGatewayId(1)).thenReturn(interpreterList);
        assertDoesNotThrow(() -> gatewaysService.onlyToLoadInitInRedisAndSocket(gateways.getNetworkId()));
    }

    @Test
    @DisplayName("createGatewayFormatToRedis when fails then return null response")
    void createGatewayFormatToRedisWhenConvertingToRedisFailsThenReturnNull() {
        when(gatewaysRepo.findById(anyInt())).thenThrow(new RuntimeException());
        assertNull(gatewaysService.createGatewayFormatToRedis(anyInt()));
    }

    @Test
    @DisplayName("updateAllGatewayByMno when all gateways by MNO are valid then return true")
    void updateAllGatewayByMnoWhenUpdateIsSuccessThenReturnTrue() {
        when(gatewaysRepo.findByMnoIdAndEnabledNot(1, DELETED_ENABLED_STATUS)).thenReturn(List.of(new Gateways()));
        Gateways gateways = new Gateways();
        gateways.setSystemId("SystemId");
        gateways.setNetworkId(1);
        when(gatewaysRepo.findById(anyInt())).thenReturn(gateways);
        when(operatorRepo.findById(anyInt())).thenReturn(new OperatorMno());
        when(gatewaysMapper.toDTO(gateways)).thenReturn(new ParseGatewaysDTO());
        assertTrue(gatewaysService.updateAllGatewayByMno(1));
    }

    @Test
    @DisplayName("updateAllGatewayByMno when fails then return false")
    void updateAllGatewayByMnoWhenFailsThenReturnFalse() {
        when(gatewaysRepo.findByMnoIdAndEnabledNot(1, DELETED_ENABLED_STATUS)).thenThrow(new RuntimeException());
        assertFalse(gatewaysService.updateAllGatewayByMno(1));
    }

    @Test
    @DisplayName("create when the bind type is transmitter and DLR is enabled then a 5xx error message is returned")
    void createWhenTransmitterAndDlrEnabledThenReturn5xxError() {
        doThrow(new IllegalArgumentException("you must disable the DLR request for the bind type TRANSMITTER"))
                .when(utilsBase).isRequestDlrAndTransmitterBind(anyString(), anyInt(), anyString());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setBindType("TRANSMITTER");
        gatewaysDTO.setProtocol("SMPP");
        ApiResponse response = gatewaysService.create(gatewaysDTO);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("update when the bind type is transmitter and DLR is enabled then a 5xx error message is returned")
    void updateWhenTransmitterAndDlrEnabledThenReturn5xxError() {
        doThrow(new IllegalArgumentException("you must disable the DLR request for the bind type TRANSMITTER"))
                .when(utilsBase).isRequestDlrAndTransmitterBind(anyString(), anyInt(), anyString());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setBindType("TRANSMITTER");
        gatewaysDTO.setRequestDlr(1);
        gatewaysDTO.setProtocol("SMPP");
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("equals when objects are equals then return true")
    void equalsWhenObjectsAreEqualsThenReturnTrue() {
        GatewaysService.ExistGatewayValidatorParams params1 = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", 1, 2, 3);
        GatewaysService.ExistGatewayValidatorParams params2 = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", 1, 2, 3);
        assertEquals(params1, params2);
    }

    @Test
    @DisplayName("equals when objects are not equals then return false")
    void equalsWhenObjectsAreNotEqualsThenReturnFalse() {
        GatewaysService.ExistGatewayValidatorParams params1 = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", 1, 2, 3);
        GatewaysService.ExistGatewayValidatorParams params2 = new GatewaysService.ExistGatewayValidatorParams(2, "192.168.1.2", 8081, "system2", "v2", "bind2", "action2", "system1", "HTTP", 4, 5, 6);
        assertNotEquals(params1, params2);
    }

    @Test
    @DisplayName("hashCode when validation is success then return true")
    void hashCodeWhenComparingHashesThenReturnResponse() {
        GatewaysService.ExistGatewayValidatorParams params1 = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", 1, 2, 3);
        GatewaysService.ExistGatewayValidatorParams params2 = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", 1, 2, 3);
        assertEquals(params1.hashCode(), params2.hashCode());
    }

    @Test
    @DisplayName("toString when method is invoked then return the response")
    void toStringWhenMethodCalledThenReturnResponse() {
        GatewaysService.ExistGatewayValidatorParams params = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "System1", "SMPP", 1, 2, 3);
        String expected = Converter.valueAsString(params);
        assertEquals(expected, Converter.valueAsString(params));
    }

    @Test
    @DisplayName("existsGateway when gateway is not found then return false")
    void existsGatewayWhenNotFoundThenReturnFalse() {
        GatewaysService.ExistGatewayValidatorParams params = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "action1", "system1", "SMPP", (Integer) null);
        boolean exists = gatewaysService.existsGateway(params);
        assertFalse(exists);
        params = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", null, "system1", "SMPP", 1, 2, 3);
        exists = gatewaysService.existsGateway(params);
        assertFalse(exists);
        params = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "create", "system1", "SMPP");
        exists = gatewaysService.existsGateway(params);
        assertFalse(exists);
        params = new GatewaysService.ExistGatewayValidatorParams(1, "192.168.1.1", 8080, "system1", "v1", "bind1", "NonExistingAction", "system1", "SMPP", 1, 2, 3);
        exists = gatewaysService.existsGateway(params);
        assertFalse(exists);
    }

    void createReferencedGatewaysTest(String host, int port, String protocol) {
        if ("SMPP".equalsIgnoreCase(protocol)) {
            when(gatewaysRepo.existsByIpAndPortAndSystemTypeAndInterfaceVersionAndBindTypeAndEnabledNotAndSystemId(
                    anyString(), anyInt(), anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(true);
        } else {
            when(gatewaysRepo.existsByIpAndEnabledNotAndSystemId(anyString(), anyInt(), anyString())).thenReturn(true);
        }

        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setIp(host);
        gatewaysDTO.setProtocol(protocol);
        gatewaysDTO.setPort(port);
        gatewaysDTO.setSystemType("st");
        gatewaysDTO.setInterfaceVersion("iv");
        gatewaysDTO.setBindType("transmitter");
        gatewaysDTO.setIp("http://localhost:8080/callback");
        gatewaysDTO.setAuthenticationTypes("undefined");
        gatewaysDTO.setEnabled(1);

        ApiResponse response = gatewaysService.create(gatewaysDTO);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    void updateReferencesGatewaysTest(String host, int port, String protocol) {
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setProtocol(protocol);
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setNetworkId(10);
        gatewaysDTO.setIp(host);
        gatewaysDTO.setPort(port);
        gatewaysDTO.setSystemType("st");
        gatewaysDTO.setInterfaceVersion("iv");
        gatewaysDTO.setBindType("transmitter");
        gatewaysDTO.setEnabled(1);
        gatewaysDTO.setIp("http://localhost:8080/callback");
        gatewaysDTO.setAuthenticationTypes("undefined");
        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());

        if ("SMPP".equalsIgnoreCase(gatewaysDTO.getProtocol())) {
            when(gatewaysRepo.existsByGatewaySearchCriteria(
                    any())).thenReturn(true);
        } else {
            when(gatewaysRepo.existsByIpAndEnabledNotAndNetworkIdNotAndSystemId(
                    anyString(), anyInt(), anyInt(), anyString())).thenReturn(true);
        }

        Gateways result = new Gateways();
        result.setNetworkId(10);
        result.setSystemId("SystemId3");
        result.setMnoId(1);

        ApiResponse response = gatewaysService.update(12, gatewaysDTO);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    void testGatewayEnabledNotPreviousTest(int enabledForDto, int enabledForEntity) {
        DefaultTemplateService defaultTemplateService = mock(DefaultTemplateService.class);
        gatewaysService = new GatewaysService(gatewaysRepo, gatewaysMapper, interpreterMapper,
                utilsBase, operatorRepo, seqGateway, interpreterRepository, defaultTemplateService, callbackHeaderRepo);

        when(gatewaysRepo.findById(anyInt())).thenReturn(new Gateways());
        GatewaysDTO gatewaysDTO = new GatewaysDTO();
        gatewaysDTO.setEnabled(enabledForDto);
        gatewaysDTO.setPassword("passW00rD");
        gatewaysDTO.setSystemId("SystemID");
        gatewaysDTO.setProtocol("SMPP");

        Gateways gateways = new Gateways();
        gateways.setSystemId("SystemId");
        gateways.setEnabled(enabledForEntity);
        gateways.setProtocol("HTTP");
        gateways.setIp("http://localhost:8080/callback");
        gateways.setAuthenticationTypes("undefined");
        when(gatewaysMapper.toDTO(gateways)).thenReturn(new ParseGatewaysDTO());
        when(gatewaysRepo.findById(anyInt())).thenReturn(gateways);
        when(gatewaysRepo.save(any())).thenReturn(gateways);
        when(operatorRepo.findById(anyInt())).thenReturn(new OperatorMno());
        OperatorMno operatorMno = new OperatorMno();
        operatorMno.setTlvMessageReceiptId(Boolean.TRUE);
        operatorMno.setMessageIdDecimalFormat(Boolean.TRUE);
        ApiResponse response = gatewaysService.update(-1, gatewaysDTO);
        assertNotNull(response);
        assertEquals(200, response.status());
    }
}
