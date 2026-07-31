package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.paicbd.smsc.utils.RedisManager;

import java.io.IOException;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BroadcastLogsServiceTest {
    @Mock
    private BroadcastFileService broadcastFileService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private RedisManager redisManager;

    @InjectMocks
    private BroadcastLogsService broadcastLogsServiceMock;

    @Test
    void downloadFile() throws IOException {
        BroadcastFile broadcastFile = Utils.getBroadcastFile();
        broadcastFile.setFilename("fakeLogsTest.csv");
        InputStreamResource inputStreamResource = new InputStreamResource(Utils.getFileStream(true));

        Mockito.when(redisManager.get(anyString())).thenReturn("fakeLogsTest.csv");
        Mockito.when(appProperties.getReportBroadcastDir()).thenReturn("/path/to/files");
        Mockito.when(broadcastFileService.createStreamFileLogs(anyString(),anyString())).thenReturn(inputStreamResource);

        ResponseEntity<InputStreamResource> responseEntity = broadcastLogsServiceMock.downloadFile("testTokenForDownloadFile");
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM, responseEntity.getHeaders().getContentType());
        Assertions.assertEquals("attachment; filename=" + broadcastFile.getFilename(), responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        Assertions.assertNotNull(responseEntity.getBody());

        Mockito.doThrow(new RuntimeException("test exception")).when(redisManager).get(anyString());
        responseEntity = broadcastLogsServiceMock.downloadFile("testTokenForDownloadFile");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
}