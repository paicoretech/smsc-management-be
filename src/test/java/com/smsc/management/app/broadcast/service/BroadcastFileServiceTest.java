package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastFileRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class BroadcastFileServiceTest {
    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private BroadcastFileRepository broadcastFileRepository;

    @Mock
    private BroadcastDevicesRepository broadcastDevicesRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private BroadcastFileService broadcastFileService;

    private Path tempDirectory;
    private File tempFile;

    @AfterEach
    void cleanup() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile.toPath());
        }
        if (tempDirectory != null) {
            Files.deleteIfExists(tempDirectory);
        }
    }
    
    @Test
    @DisplayName("save broadcast file with valid data should succeed")
    void saveBroadcastFileTestWithValidDataThenDoItSuccessfully() throws IOException {
        String csvContent = "destinationAddr,message\n+1234567890,Hello World";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "MockBroadcastFile.csv",
                "text/csv",
                csvContent.getBytes()
        );

        AppProperties mockProperties = Mockito.mock(AppProperties.class);
        Path tempDir = Files.createTempDirectory("uploadTest");
        Mockito.when(mockProperties.getUploadBroadcastDir()).thenReturn(tempDir.toAbsolutePath() + "/");

        broadcastFileService = new BroadcastFileService(
                broadcastFileRepository, broadcastRepository,
                broadcastDevicesRepository, jwtService, mockProperties
        );

        Mockito.when(broadcastFileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        BroadcastFile result = broadcastFileService.saveBroadcastFile(multipartFile, true, "[\"destinationAddr\"]", ",");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(BroadcastStatus.CREATING, result.getStatus());
        Assertions.assertTrue(result.getColumns().contains("destinationAddr"));
    }

    @Test
    void changeStatusTest() {
        BroadcastFile broadcastFile = Utils.getBroadcastFile();

        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(broadcastFile);
        Mockito.when(broadcastFileRepository.save(any())).thenReturn(null);
        Assertions.assertDoesNotThrow(() -> broadcastFileService.changeStatus(1, 1, BroadcastStatus.FAILED, 10, "testing"));
        Assertions.assertDoesNotThrow(() -> broadcastFileService.changeStatus(1, 1, BroadcastStatus.CREATED, 10, "ok"));

        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(null);
        Assertions.assertDoesNotThrow(() -> broadcastFileService.changeStatus(1, 1, BroadcastStatus.FAILED, 10, "ok"));

        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(broadcastFile);
        Assertions.assertDoesNotThrow(() -> broadcastFileService.changeStatus(1, 1, BroadcastStatus.FAILED, 10, "ok"));
    }

    @Test
    void deleteLastLoadTest() {
        Mockito.doThrow(new RuntimeException("Error to save")).when(broadcastDevicesRepository).deleteAllByBroadcastId(anyInt());
        Assertions.assertThrows(Exception.class, () -> broadcastFileService.deleteLastLoad(1));
    }

    @Test
    void createStreamFileLogsTest() throws IOException {
        tempDirectory = Files.createTempDirectory("testDir");
        tempFile = Files.createTempFile(tempDirectory, "testLogs", ".csv").toFile();
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), "this is a test");
        InputStreamResource resource = broadcastFileService.createStreamFileLogs(tempDirectory.toString(), tempFile.getName());
        Assertions.assertNotNull(resource);
    }

    @Test
    void cleanFileLogsTest() throws IOException {
        tempDirectory = Files.createTempDirectory("testDir");
        tempFile = Files.createTempFile(tempDirectory, "testLogs", ".csv").toFile();
        Files.writeString(tempFile.toPath(), "this is a test");
        Assertions.assertDoesNotThrow(() -> broadcastFileService.cleanFileLogs(tempFile));
        Assertions.assertFalse(tempFile.exists(), "Temporary file should be deleted after cleaning");

        tempFile = new File("/opt/test/test.csv");
        File mockFile = Mockito.mock(File.class);
        Mockito.when(mockFile.toPath()).thenReturn(tempFile.toPath());
        Assertions.assertDoesNotThrow(() -> broadcastFileService.cleanFileLogs(mockFile));
    }
}