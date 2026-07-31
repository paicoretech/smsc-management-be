package com.smsc.management.websocket;

import com.smsc.management.app.settings.service.StatusHandlerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {
    @Mock
    Sinks.Many<String> sink;
    @Mock
    StatusHandlerService statusHandlerService;

    @InjectMocks
    WebSocketController webSocketController;

    @Test
    void handleSessionConfirm() {
        assertDoesNotThrow(() -> webSocketController.handleSessionConfirm("sessionConfirm"));
    }

    @Test
    void handleResponseSmppServer() {
        assertDoesNotThrow(() -> webSocketController.handleResponseSmppServer("responseSmppServer"));
    }

    @Test
    void handleResponseSmppClient() {
        assertDoesNotThrow(() -> webSocketController.handleResponseSmppClient("responseSmppClient"));
    }

    @Test
    void handleResponseHttpServer() {
        assertDoesNotThrow(() -> webSocketController.handleResponseHttpServer("responseSmppServer"));
    }

    @Test
    void handleResponseHttpClient() {
        assertDoesNotThrow(() -> webSocketController.handleResponseHttpClient("responseSmppClient"));
    }

    @Test
    void handleStatus() {
        String spMsg = "sp,71,param,value";
        assertDoesNotThrow(() -> webSocketController.handleStatus(spMsg));

        String gwMsg = "gw,17,param,value";
        assertDoesNotThrow(() -> webSocketController.handleStatus(gwMsg));

        String invalidMsg = "invalid,17,param,value";
        assertDoesNotThrow(() -> webSocketController.handleStatus(invalidMsg));

        String emptyMsg = "";
        assertDoesNotThrow(() -> webSocketController.handleStatus(emptyMsg));

        String nullMsg = null;
        assertDoesNotThrow(() -> webSocketController.handleStatus(nullMsg));

        String invalidLengthMsg = "sp,71,param";
        assertDoesNotThrow(() -> webSocketController.handleStatus(invalidLengthMsg));
    }

    @Test
    void handleConfirmInstanceConnection() {
        assertDoesNotThrow(() -> webSocketController.handleConfirmInstanceConnection("confirmInstanceConnection"));
    }

    @Test
    void handleConfirmInstanceDisconnection() {
        assertDoesNotThrow(() -> webSocketController.handleConfirmInstanceDisconnection("confirmInstanceDisconnection"));
    }

    @Test
    void emitMessage() {
        assertDoesNotThrow(() -> webSocketController.emitMessage("emitMessage"));
    }
}