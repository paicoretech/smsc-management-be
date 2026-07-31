package com.smsc.management.app.analyze.cdrs.component;

import com.smsc.management.app.analyze.utils.Utils;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.smsc.management.app.analyze.utils.HelperQuery.COUNT_QUERY_BY_REPORT_TABLE;
import static com.smsc.management.app.analyze.utils.HelperQuery.PAGINATION_QUERY_REPORT_CDR;
import static com.smsc.management.app.analyze.utils.HelperQuery.PAGINATION_QUERY_REPORT_DETAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CdrsDataTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private Utils utils;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private CdrsData cdrsData;

    static Stream<Arguments> fileTypeProvider() {
        return Stream.of(
                Arguments.of(FileType.CDRS, "test_table_cdrs"),
                Arguments.of(FileType.CDRS_DETAILED_REPORT, "test_table_detailed")
        );
    }

    @ParameterizedTest
    @MethodSource("fileTypeProvider")
    void getSimpleDataForHeaders_ShouldReturnCorrectData(FileType fileType, String tableName) {
        // Arrange
        List<Map<String, Object>> expectedData = List.of(
                Map.of("column1", "value1", "column2", "value2"),
                Map.of("column1", "value3", "column2", "value4")
        );

        when(jdbcTemplate.queryForList(anyString(), any(HashMap.class)))
                .thenReturn(expectedData);

        // Act
        List<Map<String, Object>> result = cdrsData.getSimpleDataForHeaders(tableName, fileType);

        // Assert
        assertEquals(expectedData, result);
        assertFalse(result.isEmpty());

        // Verify correct query format based on file type
        String expectedQuery;
        if (fileType.equals(FileType.CDRS_DETAILED_REPORT)) {
            expectedQuery = String.format(PAGINATION_QUERY_REPORT_DETAILED, tableName) + " LIMIT 1";
        } else {
            expectedQuery = String.format(PAGINATION_QUERY_REPORT_CDR, tableName) + " LIMIT 1";
        }

        verify(jdbcTemplate).queryForList(eq(expectedQuery), any(HashMap.class));
    }

    @ParameterizedTest
    @MethodSource("fileTypeProvider")
    void getQueryForData_ShouldReturnCorrectQuery(FileType fileType, String tableName) {
        // Act
        String result = cdrsData.getQueryForData(tableName, fileType);

        // Assert
        String expectedQuery;
        if (fileType.equals(FileType.CDRS_DETAILED_REPORT)) {
            expectedQuery = String.format(PAGINATION_QUERY_REPORT_DETAILED, tableName);
        } else {
            expectedQuery = String.format(PAGINATION_QUERY_REPORT_CDR, tableName);
        }

        assertEquals(expectedQuery, result);
    }

    @Test
    void getTotalElements_ShouldReturnTotalCount() {
        // Arrange
        String tableName = "test_report_table";
        Long expectedCount = 150L;

        when(jdbcTemplate.queryForObject(anyString(), any(LinkedHashMap.class), eq(Long.class)))
                .thenReturn(expectedCount);

        // Act
        long result = cdrsData.getTotalElements(tableName);

        // Assert
        assertEquals(expectedCount, result);

        String expectedQuery = String.format(COUNT_QUERY_BY_REPORT_TABLE, tableName);
        verify(jdbcTemplate).queryForObject(eq(expectedQuery), any(LinkedHashMap.class), eq(Long.class));
    }

    @Test
    void getTotalElements_WhenCountResponseIsNull_ShouldReturnZero() {
        // Arrange
        String tableName = "test_report_table";

        when(jdbcTemplate.queryForObject(anyString(), any(LinkedHashMap.class), eq(Long.class)))
                .thenReturn(null);

        // Act
        long result = cdrsData.getTotalElements(tableName);

        // Assert
        assertEquals(0L, result);

        String expectedQuery = String.format(COUNT_QUERY_BY_REPORT_TABLE, tableName);
        verify(jdbcTemplate).queryForObject(eq(expectedQuery), any(LinkedHashMap.class), eq(Long.class));
    }

    @Test
    void getSimpleDataForHeaders_WithCdrsFileType_ShouldUseCorrectQuery() {
        // Arrange
        String tableName = "cdrs_test_table";
        FileType fileType = FileType.CDRS;
        List<Map<String, Object>> mockData = List.of(Map.of("id", 1, "message", "test"));

        when(jdbcTemplate.queryForList(anyString(), any(HashMap.class)))
                .thenReturn(mockData);

        // Act
        List<Map<String, Object>> result = cdrsData.getSimpleDataForHeaders(tableName, fileType);

        // Assert
        assertEquals(mockData, result);

        String expectedQuery = String.format(PAGINATION_QUERY_REPORT_CDR, tableName) + " LIMIT 1";
        verify(jdbcTemplate).queryForList(eq(expectedQuery), any(HashMap.class));
    }

    @Test
    void getSimpleDataForHeaders_WithDetailedReportFileType_ShouldUseCorrectQuery() {
        // Arrange
        String tableName = "detailed_test_table";
        FileType fileType = FileType.CDRS_DETAILED_REPORT;
        List<Map<String, Object>> mockData = List.of(Map.of("id", 1, "details", "detailed_test"));

        when(jdbcTemplate.queryForList(anyString(), any(HashMap.class)))
                .thenReturn(mockData);

        // Act
        List<Map<String, Object>> result = cdrsData.getSimpleDataForHeaders(tableName, fileType);

        // Assert
        assertEquals(mockData, result);

        String expectedQuery = String.format(PAGINATION_QUERY_REPORT_DETAILED, tableName) + " LIMIT 1";
        verify(jdbcTemplate).queryForList(eq(expectedQuery), any(HashMap.class));
    }
}
