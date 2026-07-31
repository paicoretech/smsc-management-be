package com.smsc.management.app.broadcast.controller;

import com.smsc.management.app.broadcast.service.BroadcastLogsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BroadcastLogControllerTest {
    @Mock
    private BroadcastLogsService broadcastLogsServiceMock;

    @InjectMocks
    private BroadcastController broadcastController;

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void downloadProcessTest() {
        Mockito.when(broadcastLogsServiceMock.downloadFile(anyString())).thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        ResponseEntity<InputStreamResource> response = broadcastController.downloadProcess("testTokenForDownloadFile");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
