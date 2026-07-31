package com.smsc.management.app.interpreter.service;

import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.gateway.service.GatewaysService;
import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class InterpreterServiceTest {
    @Mock
    private GatewaysRepository gatewaysRepository;

    @Mock
    private InterpreterRepository interpreterRepository;

    @Mock
    private GatewaysService gatewaysService;

    @InjectMocks
    private InterpreterService interpreterService;

    @Test
    @DisplayName("Get interpreters when an unexpected error occurs")
    void getInterpreterSettingsWhenAnUnexpectedErrorThenHTTPStatusCode500() {
        when(interpreterRepository.getAllInterpreters()).thenThrow(new RuntimeException("test exception"));
        ApiResponse apiResponse = interpreterService.getInterpreterSettings();
        assertNotNull(apiResponse);
        assertEquals("error", apiResponse.message());
        assertEquals(500, apiResponse.status());
    }

    @Test
    @DisplayName("Update interpreter when an unexpected error occurs")
    void updateInterpreterWhenAnUnexpectedErrorThenHTTPStatusCode500() {
        InterpreterDTO interpreter = new InterpreterDTO();
        interpreter.setId(1);
        interpreter.setDirection("input");
        interpreter.setEventType("message");
        interpreter.setBodyType("JSON");
        interpreter.setTemplate("{\"message\":\"{text:STRING}\"}");
        interpreter.setUseProxy(true);
        interpreter.setPath("/message/mt");
        interpreter.setGatewayId(1);

        when(interpreterRepository.findById(1)).thenThrow(new RuntimeException("updating error"));
        ApiResponse apiResponse = interpreterService.updateInterpreter(1, interpreter);
        assertNotNull(apiResponse);
        assertEquals("error", apiResponse.message());
        assertEquals(500, apiResponse.status());
    }

    @Test
    @DisplayName("Update interpreter when path is duplicated then throw exception error")
    void updateInterpreterWhenPathIsDuplicatedThenHTTPStatusCode500() {
        InterpreterDTO interpreter = new InterpreterDTO();
        interpreter.setId(1);
        interpreter.setBodyType("JSON");
        interpreter.setTemplate("{\"message\":\"{text:STRING}\"}");
        interpreter.setUseProxy(true);
        interpreter.setPath("/message");
        interpreter.setDefaultTemplate(false);

        Interpreter currentInterpreter = new Interpreter();
        currentInterpreter.setId(1);
        currentInterpreter.setDirection("input");
        currentInterpreter.setEventType("message");
        currentInterpreter.setBodyType("XML");
        currentInterpreter.setTemplate("<xml><message>{text:STRING}</message></xml>");
        currentInterpreter.setUseProxy(false);
        currentInterpreter.setDefaultTemplate(false);
        currentInterpreter.setGatewayId(1);
        currentInterpreter.setPath("/message");

        when(interpreterRepository.findById(1)).thenReturn(currentInterpreter);
        when(gatewaysRepository.existsByEnabledNotAndNetworkIdAndProtocol(DELETED_ENABLED_STATUS, 1, "HTTP")).thenReturn(Boolean.TRUE);
        when(interpreterRepository.existsByPathAndGatewayIdNotAndDefaultTemplateIsFalse("/message", 1)).thenReturn(Boolean.TRUE);
        ApiResponse apiResponse = interpreterService.updateInterpreter(1, interpreter);
        assertNotNull(apiResponse);
        assertEquals("error", apiResponse.message());
        assertEquals(500, apiResponse.status());
    }

    @Test
    @DisplayName("Update interpreter when gateway id not exists then throw exception error")
    void updateInterpreterWhenGatewayIdNotExistsThenHTTPStatusCode500() {
        InterpreterDTO interpreter = new InterpreterDTO();
        interpreter.setId(1);
        interpreter.setDirection("input");
        interpreter.setEventType("message");
        interpreter.setBodyType("JSON");
        interpreter.setTemplate("{\"message\":\"{text:STRING}\"}");
        interpreter.setUseProxy(true);
        interpreter.setPath("/message");
        interpreter.setGatewayId(1);
        interpreter.setDefaultTemplate(false);

        Interpreter currentInterpreter = new Interpreter();
        currentInterpreter.setId(1);
        currentInterpreter.setDirection("output");
        currentInterpreter.setEventType("message");
        currentInterpreter.setBodyType("XML");
        currentInterpreter.setTemplate("<xml><message>{text:STRING}</message></xml>");
        currentInterpreter.setUseProxy(false);
        currentInterpreter.setGatewayId(1);

        when(interpreterRepository.findById(1)).thenReturn(currentInterpreter);
        when(gatewaysRepository.existsByEnabledNotAndNetworkIdAndProtocol(DELETED_ENABLED_STATUS, 1, "HTTP")).thenReturn(Boolean.FALSE);
        ApiResponse apiResponse = interpreterService.updateInterpreter(1, interpreter);
        assertNotNull(apiResponse);
        assertEquals("error", apiResponse.message());
        assertEquals(400, apiResponse.status());
    }

    @Test
    @DisplayName("get Http Gateways when an unexpected error occurs")
    void getHttpGatewaysWhenAnUnexpectedErrorThenHTTPStatusCode500() {
        when(gatewaysRepository.findHttpGatewaysList()).thenThrow(new RuntimeException("updating error"));
        ApiResponse apiResponse = interpreterService.getHttpGateways();
        assertNotNull(apiResponse);
        assertEquals("error", apiResponse.message());
        assertEquals(500, apiResponse.status());
    }
}