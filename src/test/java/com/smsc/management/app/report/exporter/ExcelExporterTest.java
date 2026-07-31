package com.smsc.management.app.report.exporter;

import com.smsc.management.app.analyze.cdrs.component.CdrsData;
import com.smsc.management.app.analyze.reports.component.ReportData;
import com.smsc.management.app.report.model.entity.ReportFile;
import com.smsc.management.app.report.model.repository.ReportFileRepository;
import com.smsc.management.app.report.utils.FileExtension;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.paicbd.smsc.utils.RedisManager;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.smsc.management.utils.Constants.BROADCAST_FILTER_USER;
import static com.smsc.management.utils.Constants.TOTAL_COUNT_COLUMN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExporterTest {

    @TempDir
    Path tempDir;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ReportFileRepository reportFileRepository;

    @Mock
    private RedisManager redisManager;

    @Mock
    private AppProperties appProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoutingRulesRepository routingRulesRepository;

    @Mock
    private CdrsData cdrsData;

    @Mock
    private ReportData reportData;

    @InjectMocks
    private ExcelExporter excelExporter;

    private ReportFile testReportFile;

    @BeforeEach
    void setUp() {
        testReportFile = getTestReportFile();
        lenient().when(appProperties.getLoadCsvBatchSize()).thenReturn(1000);
    }

    static Stream<Arguments> fileTypeProvider() {
        return Stream.of(
                Arguments.of(FileType.CDRS),
                Arguments.of(FileType.CDRS_DETAILED_REPORT),
                Arguments.of(FileType.SUMMARY_CDR_ACCOUNT),
                Arguments.of(FileType.SUMMARY_CDR_BROADCAST)
        );
    }

    @ParameterizedTest
    @MethodSource("fileTypeProvider")
    @DisplayName("Export CDR data successfully for different file types")
    void exportCdrSuccessfully(FileType fileType) {
        // Given
        Map<String, Object> filters = getTestFilters();
        String expectedTableName = "reports.data_cdr_report_file_id_" + testReportFile.getId();

        // Use temp directory for file path
        File tempFile = tempDir.resolve("test-report-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(cdrsData.createReportDataTable(eq(filters), eq(fileType), eq(expectedTableName))).thenReturn(true);

        // Mock the data headers method
        List<Map<String, Object>> mockHeaders = List.of(
                getCdrRecord("MSG1", "SUCCESS", "1234567890", "0987654321")
        );
        when(cdrsData.getSimpleDataForHeaders(eq(expectedTableName), eq(fileType))).thenReturn(mockHeaders);

        // Mock the query method for getting data
        when(cdrsData.getQueryForData(eq(expectedTableName), eq(fileType))).thenReturn("SELECT * FROM " + expectedTableName);

        // Mock the JDBC query execution
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            // Simulate processing one row
            ResultSet mockResultSet = createMockResultSet();
            handler.processRow(mockResultSet);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));

        doAnswer(invocation -> {
            // Simulate table deletion
            return null;
        }).when(cdrsData).deleteReportDataTable(eq(expectedTableName));

        // When
        excelExporter.exportCdr(filters, cdrsData, testReportFile, fileType);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));
        verify(cdrsData).deleteReportDataTable(eq(expectedTableName));

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR fails when table creation fails")
    void exportCdrFailsWhenTableCreationFails() {
        // Given
        Map<String, Object> filters = getTestFilters();
        String expectedTableName = "reports.data_cdr_report_file_id_" + testReportFile.getId();

        when(cdrsData.createReportDataTable(eq(filters), eq(FileType.CDRS), eq(expectedTableName))).thenReturn(false);

        // When
        excelExporter.exportCdr(filters, cdrsData, testReportFile, FileType.CDRS);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.FAILED, savedFile.getStatus());

        verify(cdrsData).deleteReportDataTable(eq(expectedTableName));
    }

    @Test
    @DisplayName("Export CDR handles file creation exception")
    void exportCdrHandlesFileCreationException() {
        // Given
        Map<String, Object> filters = getTestFilters();
        testReportFile.setPath("/invalid/path/file.xlsx"); // Invalid path to cause exception
        String expectedTableName = "reports.data_cdr_report_file_id_" + testReportFile.getId();

        when(cdrsData.createReportDataTable(eq(filters), eq(FileType.CDRS), eq(expectedTableName))).thenReturn(true);

        // When
        excelExporter.exportCdr(filters, cdrsData, testReportFile, FileType.CDRS);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.FAILED, savedFile.getStatus());
    }

    @Test
    @DisplayName("Export CDR summary successfully")
    void exportCdrSummarySuccessfully() {
        // Given
        Map<String, Object> filters = getTestFilters();
        List<Map<String, Object>> mockSummaryData = getMockSummaryData();

        File tempFile = tempDir.resolve("test-summary-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        Users mockUser = getMockUser();
        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.of(mockUser));

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR summary handles empty data")
    void exportCdrSummaryHandlesEmptyData() {
        // Given
        Map<String, Object> filters = getTestFilters();

        File tempFile = tempDir.resolve("test-empty-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(null);

        Users mockUser = getMockUser();
        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.of(mockUser));

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR summary handles file creation exception")
    void exportCdrSummaryHandlesFileCreationException() {
        // Given
        Map<String, Object> filters = getTestFilters();
        testReportFile.setPath("/invalid/path/summary.xlsx");

        List<Map<String, Object>> mockSummaryData = getMockSummaryData();
        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.FAILED, savedFile.getStatus());
    }

    @Test
    @DisplayName("Export CDR summary with user and network filters")
    void exportCdrSummaryWithFilters() {
        // Given
        Map<String, Object> filters = getTestFiltersWithUserAndNetwork();
        List<Map<String, Object>> mockSummaryData = getMockSummaryData();

        File tempFile = tempDir.resolve("test-filters-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        Users mockUser = getMockUser();
        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.of(mockUser));

        List<Users> mockUsers = Arrays.asList(mockUser);
        when(userRepository.findByIdIn(anyList())).thenReturn(mockUsers);

        List<NetworksToRoutingRulesDTO> mockNetworks = getMockNetworks();
        when(routingRulesRepository.findByNetworkIds(anyList())).thenReturn(mockNetworks);

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));
        verify(userRepository).findByIdIn(anyList());
        verify(routingRulesRepository).findByNetworkIds(anyList());

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR summary handles user not found")
    void exportCdrSummaryHandlesUserNotFound() {
        // Given
        Map<String, Object> filters = getTestFilters();
        List<Map<String, Object>> mockSummaryData = getMockSummaryData();

        File tempFile = tempDir.resolve("test-no-user-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.empty());

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR with no data headers")
    void exportCdrWithNoDataHeaders() {
        // Given
        Map<String, Object> filters = getTestFilters();
        String expectedTableName = "reports.data_cdr_report_file_id_" + testReportFile.getId();

        File tempFile = tempDir.resolve("test-no-headers-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(cdrsData.createReportDataTable(eq(filters), eq(FileType.CDRS), eq(expectedTableName))).thenReturn(true);
        when(cdrsData.getSimpleDataForHeaders(eq(expectedTableName), eq(FileType.CDRS))).thenReturn(Arrays.asList());

        // When
        excelExporter.exportCdr(filters, cdrsData, testReportFile, FileType.CDRS);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(cdrsData).deleteReportDataTable(eq(expectedTableName));

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR with large dataset creates multiple sheets")
    void exportCdrWithLargeDatasetCreatesMultipleSheets() {
        // Given
        Map<String, Object> filters = getTestFilters();
        String expectedTableName = "reports.data_cdr_report_file_id_" + testReportFile.getId();

        // Use temp directory for file path
        File tempFile = tempDir.resolve("test-large-dataset-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(cdrsData.createReportDataTable(eq(filters), eq(FileType.CDRS), eq(expectedTableName))).thenReturn(true);

        // Mock the data headers method
        List<Map<String, Object>> mockHeaders = Arrays.asList(
                getCdrRecord("MSG1", "SUCCESS", "1234567890", "0987654321")
        );
        when(cdrsData.getSimpleDataForHeaders(eq(expectedTableName), eq(FileType.CDRS))).thenReturn(mockHeaders);

        // Mock the query method for getting data
        when(cdrsData.getQueryForData(eq(expectedTableName), eq(FileType.CDRS))).thenReturn("SELECT * FROM " + expectedTableName);

        // Mock the JDBC query execution to simulate large dataset
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            ResultSet mockResultSet = createMockResultSet();

            // Simulate processing multiple rows (more than MAX_ROWS_PER_SHEET)
            for (int i = 0; i < 1000010; i++) { // This should create 2 sheets
                handler.processRow(mockResultSet);
            }
            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));

        doAnswer(invocation -> {
            // Simulate table deletion
            return null;
        }).when(cdrsData).deleteReportDataTable(eq(expectedTableName));

        // When
        excelExporter.exportCdr(filters, cdrsData, testReportFile, FileType.CDRS);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));
        verify(cdrsData).deleteReportDataTable(eq(expectedTableName));

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR summary with single user filter")
    void exportCdrSummaryWithSingleUserFilter() {
        // Given
        Map<String, Object> filters = getTestFilters();
        filters.put(BROADCAST_FILTER_USER, 1); // Single user instead of list

        List<Map<String, Object>> mockSummaryData = getMockSummaryData();

        File tempFile = tempDir.resolve("test-single-user-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        Users mockUser = getMockUser();
        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.of(mockUser));

        List<Users> mockUsers = Arrays.asList(mockUser);
        when(userRepository.findByIdIn(anyList())).thenReturn(mockUsers);

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));
        verify(userRepository).findByIdIn(anyList());

        assertTrue(tempFile.exists());
    }

    @Test
    @DisplayName("Export CDR summary with single network filter")
    void exportCdrSummaryWithSingleNetworkFilter() {
        // Given
        Map<String, Object> filters = getTestFilters();
        filters.put("origin_network", 1); // Single network instead of list

        List<Map<String, Object>> mockSummaryData = getMockSummaryData();

        File tempFile = tempDir.resolve("test-single-network-" + System.currentTimeMillis() + ".xlsx").toFile();
        testReportFile.setPath(tempFile.getAbsolutePath());

        when(reportData.createReportData(eq(filters), eq(FileType.SUMMARY_CDR_ACCOUNT)))
                .thenReturn(mockSummaryData);

        Users mockUser = getMockUser();
        when(userRepository.findById(eq(testReportFile.getCreatedById()))).thenReturn(Optional.of(mockUser));

        List<NetworksToRoutingRulesDTO> mockNetworks = getMockNetworks();
        when(routingRulesRepository.findByNetworkIds(anyList())).thenReturn(mockNetworks);

        // When
        excelExporter.exportCdrSummary(filters, reportData, testReportFile, FileType.SUMMARY_CDR_ACCOUNT);

        // Then
        ArgumentCaptor<ReportFile> reportFileCaptor = ArgumentCaptor.forClass(ReportFile.class);
        verify(reportFileRepository).save(reportFileCaptor.capture());

        ReportFile savedFile = reportFileCaptor.getValue();
        assertEquals(FileStatus.COMPLETED, savedFile.getStatus());

        verify(redisManager).setex(eq(testReportFile.getToken()), anyLong(), eq(testReportFile.getFilename()));
        verify(routingRulesRepository).findByNetworkIds(anyList());

        assertTrue(tempFile.exists());
    }

    // Helper methods
    private ReportFile getTestReportFile() {
        ReportFile reportFile = new ReportFile();
        reportFile.setId(1);
        reportFile.setFilename("test-report.xlsx");
        reportFile.setStatus(FileStatus.CREATING);
        reportFile.setType(FileType.CDRS.getName());
        reportFile.setExtension(FileExtension.XLSX);
        reportFile.setToken("test-token-123");
        reportFile.setPath("/tmp/test-report.xlsx");
        reportFile.setCreatedById(1);
        reportFile.setCreatedAt(LocalDateTime.now());
        return reportFile;
    }

    private Map<String, Object> getTestFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2024-01-15 00:00:00");
        filters.put("end_datetime", "2024-01-15 23:59:59");
        filters.put("status", "SUCCESS");
        return filters;
    }

    private Map<String, Object> getTestFiltersWithUserAndNetwork() {
        Map<String, Object> filters = getTestFilters();
        filters.put(BROADCAST_FILTER_USER, Arrays.asList(1, 2));
        filters.put("origin_network", Arrays.asList(1, 2));
        return filters;
    }

    private Map<String, Object> getCdrRecord(String messageId, String status, String srcAddr, String dstAddr) {
        Map<String, Object> record = new HashMap<>();
        record.put("message_id", messageId);
        record.put("status", status);
        record.put("addr_src_digits", srcAddr);
        record.put("addr_dst_digits", dstAddr);
        record.put("record_date", LocalDateTime.now());
        return record;
    }

    private List<Map<String, Object>> getMockSummaryData() {
        Map<String, Object> summary1 = new LinkedHashMap<>();
        summary1.put("account_name", "Account1");
        summary1.put("total_messages", 1000L);
        summary1.put("delivered_messages", 950L);
        summary1.put("failed_messages", 50L);
        summary1.put("delivery_rate", 95.0);
        summary1.put(TOTAL_COUNT_COLUMN, 2000L);

        Map<String, Object> summary2 = new LinkedHashMap<>();
        summary2.put("account_name", "Account2");
        summary2.put("total_messages", 500L);
        summary2.put("delivered_messages", 480L);
        summary2.put("failed_messages", 20L);
        summary2.put("delivery_rate", 96.0);
        summary2.put(TOTAL_COUNT_COLUMN, 2000L);

        return Arrays.asList(summary1, summary2);
    }

    private Users getMockUser() {
        Users user = new Users();
        user.setId(1);
        user.setName("Test User");
        user.setUserName("testuser");
        return user;
    }

    private List<NetworksToRoutingRulesDTO> getMockNetworks() {
        NetworksToRoutingRulesDTO network1 = new NetworksToRoutingRulesDTO();
        network1.setNetworkId(1);
        network1.setName("Network1");

        NetworksToRoutingRulesDTO network2 = new NetworksToRoutingRulesDTO();
        network2.setNetworkId(2);
        network2.setName("Network2");

        return Arrays.asList(network1, network2);
    }

    private ResultSet createMockResultSet() throws SQLException {
        ResultSet mockResultSet = org.mockito.Mockito.mock(ResultSet.class);
        when(mockResultSet.getObject("message_id")).thenReturn("MSG1");
        when(mockResultSet.getObject("status")).thenReturn("SUCCESS");
        when(mockResultSet.getObject("addr_src_digits")).thenReturn("1234567890");
        when(mockResultSet.getObject("addr_dst_digits")).thenReturn("0987654321");
        when(mockResultSet.getObject("record_date")).thenReturn(LocalDateTime.now());
        return mockResultSet;
    }
}
