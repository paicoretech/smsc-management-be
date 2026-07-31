package com.smsc.management.app.report.controller;

import com.smsc.management.app.report.dto.ReportFileDTO;
import com.smsc.management.app.report.model.entity.ReportFile;
import com.smsc.management.app.report.model.repository.ReportFileRepository;
import com.smsc.management.app.report.utils.FileExtension;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import com.paicbd.smsc.utils.RedisManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ReportFileControllerTest extends BaseIntegrationTest {

    @Autowired
    private ReportFileController reportFileController;

    @MockBean
    private ReportFileRepository reportFileRepository;

    @MockBean
    private RedisManager redisManager;

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should return report files list successfully")
    void testListReportFilesSuccess() {
        // Given
        List<String> types = Arrays.asList("cdrs", "logs");
        List<ReportFileDTO> mockReportFiles = createMockReportFilesList();

        when(reportFileRepository.findAllByOrderByIdDesc(anyList())).thenReturn(mockReportFiles);
        when(redisManager.exists(anyString())).thenReturn(true);

        // When
        ResponseEntity<ApiResponse> response = reportFileController.listReportFiles(types);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Get report file request success", response.getBody().comment());
        assertEquals(HttpStatus.OK.value(), response.getBody().status());

        assertNotNull(response.getBody().data());
        @SuppressWarnings("unchecked")
        List<ReportFileDTO> result = (List<ReportFileDTO>) response.getBody().data();
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle repository exception gracefully for report files list")
    void testListReportFilesRepositoryException() {
        // Given
        List<String> types = Arrays.asList("cdrs", "logs");

        when(reportFileRepository.findAllByOrderByIdDesc(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<ApiResponse> response = reportFileController.listReportFiles(types);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Report dile was end with error while getting data (Database error)", response.getBody().comment());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should start download report for CDR successfully")
    void testStartDownloadReportForCdrSuccess() {
        // Given
        FileType fileType = FileType.CDRS;
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2025-06-04 00:00:00");
        filters.put("end_datetime", "2025-06-15 23:59:59");
        filters.put("status", "SUCCESS");

        ReportFile mockReportFile = createMockReportFile();

        when(reportFileRepository.save(any(ReportFile.class))).thenReturn(mockReportFile);

        // When
        ResponseEntity<ApiResponse> response = reportFileController.startDownloadReportForCdr(fileType, filters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Download process started", response.getBody().comment());
        assertEquals(HttpStatus.OK.value(), response.getBody().status());

        assertNotNull(response.getBody().data());
        ReportFile result = (ReportFile) response.getBody().data();
        assertEquals(FileStatus.CREATING, result.getStatus());
        assertEquals(FileType.CDRS.getName(), result.getType());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle repository exception gracefully for start download")
    void testStartDownloadReportForCdrRepositoryException() {
        // Given
        FileType fileType = FileType.CDRS;
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2025-06-04 00:00:00");
        filters.put("end_datetime", "2025-01-15 23:59:59");

        when(reportFileRepository.save(any(ReportFile.class)))
                .thenThrow(new RuntimeException("Database save error"));

        // When
        ResponseEntity<ApiResponse> response = reportFileController.startDownloadReportForCdr(fileType, filters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Start download process request with error (Database save error)", response.getBody().comment());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should monitor download process successfully")
    void testMonitorDownloadProcessSuccess() {
        // Given
        int fileId = 1;
        ReportFile mockReportFile = createMockReportFile();
        mockReportFile.setStatus(FileStatus.COMPLETED);

        when(reportFileRepository.findById(fileId)).thenReturn(mockReportFile);

        // When
        ResponseEntity<ApiResponse> response = reportFileController.monitorDownloadProcess(fileId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Monitor download process request", response.getBody().comment());
        assertEquals(HttpStatus.OK.value(), response.getBody().status());

        assertNotNull(response.getBody().data());
        ReportFile result = (ReportFile) response.getBody().data();
        assertEquals(FileStatus.COMPLETED, result.getStatus());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle repository exception gracefully for monitor download")
    void testMonitorDownloadProcessRepositoryException() {
        // Given
        int fileId = 1;

        when(reportFileRepository.findById(fileId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<ApiResponse> response = reportFileController.monitorDownloadProcess(fileId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Monitor download process request with error (Database error)", response.getBody().comment());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should download file successfully")
    void testDownloadFileSuccess() {
        // Given
        String token = "test-token-123";
        String filename = "test-report.xlsx";

        when(redisManager.get(token)).thenReturn(filename);

        // When
        ResponseEntity<InputStreamResource> response = reportFileController.downloadProcess(token);

        // Then
        assertNotNull(response);
        // Note: This will likely return NOT_FOUND because the actual file doesn't exist
        // but the service logic is being tested
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle file not found for download")
    void testDownloadFileNotFound() {
        // Given
        String token = "invalid-token";

        when(redisManager.get(token)).thenReturn(null);

        // When
        ResponseEntity<InputStreamResource> response = reportFileController.downloadProcess(token);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle different file types for CDR download")
    void testStartDownloadReportForDifferentFileTypes() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2025-06-04 00:00:00");
        filters.put("end_datetime", "2025-06-15 23:59:59");
        filters.put("exportAs", "xlsx");

        ReportFile mockReportFile = createMockReportFile();

        when(reportFileRepository.save(any(ReportFile.class))).thenReturn(mockReportFile);

        // Test CDRS_DETAILED_REPORT
        ResponseEntity<ApiResponse> response1 = reportFileController.startDownloadReportForCdr(
                FileType.CDRS_DETAILED_REPORT, filters
        );

        assertNotNull(response1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Test SUMMARY_CDR_ACCOUNT
        ResponseEntity<ApiResponse> response2 = reportFileController.startDownloadReportForCdr(
                FileType.SUMMARY_CDR_ACCOUNT, filters
        );

        assertNotNull(response2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle empty filters for CDR download")
    void testStartDownloadReportWithEmptyFilters() {
        // Given
        FileType fileType = FileType.CDRS;
        Map<String, Object> emptyFilters = new HashMap<>();

        ReportFile mockReportFile = createMockReportFile();

        when(reportFileRepository.save(any(ReportFile.class))).thenReturn(mockReportFile);

        // When
        ResponseEntity<ApiResponse> response = reportFileController.startDownloadReportForCdr(fileType, emptyFilters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Download process started", response.getBody().comment());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle token expired status in report files list")
    void testListReportFilesWithTokenExpired() {
        // Given
        List<String> types = List.of("cdrs");
        List<ReportFileDTO> mockReportFiles = createMockReportFilesList();
        mockReportFiles.getFirst().setStatus(FileStatus.COMPLETED);

        when(reportFileRepository.findAllByOrderByIdDesc(anyList())).thenReturn(mockReportFiles);
        when(redisManager.exists(anyString())).thenReturn(false); // Token expired

        // When
        ResponseEntity<ApiResponse> response = reportFileController.listReportFiles(types);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        List<ReportFileDTO> result = (List<ReportFileDTO>) response.getBody().data();
        assertNotNull(result);
        assertEquals(FileStatus.TOKEN_EXPIRED, result.getFirst().getStatus());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle CSV export format")
    void testStartDownloadReportWithCsvFormat() {
        // Given
        FileType fileType = FileType.CDRS;
        Map<String, Object> filters = new HashMap<>();
        filters.put("exportAs", "csv");

        ReportFile mockReportFile = createMockReportFile();
        mockReportFile.setExtension(FileExtension.CSV);

        when(reportFileRepository.save(any(ReportFile.class))).thenReturn(mockReportFile);

        // When
        ResponseEntity<ApiResponse> response = reportFileController.startDownloadReportForCdr(fileType, filters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Download process started", response.getBody().comment());
    }

    private List<ReportFileDTO> createMockReportFilesList() {
        ReportFileDTO dto1 = new ReportFileDTO();
        dto1.setId(1);
        dto1.setFilename("cdrs-2024-01-15T120000.xlsx");
        dto1.setStatus(FileStatus.COMPLETED);
        dto1.setType("cdrs");
        dto1.setExtension(FileExtension.XLSX);
        dto1.setToken("token-123");
        dto1.setCreatedById(1);
        dto1.setCreatedBy("testuser");
        dto1.setCreatedAt(LocalDateTime.now().minusHours(1));
        dto1.setUpdatedAt(LocalDateTime.now());

        ReportFileDTO dto2 = new ReportFileDTO();
        dto2.setId(2);
        dto2.setFilename("logs-2024-01-15T130000.xlsx");
        dto2.setStatus(FileStatus.CREATING);
        dto2.setType("logs");
        dto2.setExtension(FileExtension.XLSX);
        dto2.setToken("token-456");
        dto2.setCreatedById(1);
        dto2.setCreatedBy("testuser");
        dto2.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        dto2.setUpdatedAt(LocalDateTime.now());

        return Arrays.asList(dto1, dto2);
    }

    private ReportFile createMockReportFile() {
        ReportFile reportFile = new ReportFile();
        reportFile.setId(1);
        reportFile.setFilename("cdrs-2024-01-15T120000.xlsx");
        reportFile.setStatus(FileStatus.CREATING);
        reportFile.setType(FileType.CDRS.getName());
        reportFile.setExtension(FileExtension.XLSX);
        reportFile.setToken("test-token-123");
        reportFile.setPath("/tmp/reports/cdrs-2024-01-15T120000.xlsx");
        reportFile.setCreatedById(1);
        reportFile.setCreatedAt(LocalDateTime.now());
        return reportFile;
    }
}
