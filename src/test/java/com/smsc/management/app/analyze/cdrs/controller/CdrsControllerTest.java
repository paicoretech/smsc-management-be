package com.smsc.management.app.analyze.cdrs.controller;

import com.smsc.management.app.analyze.cdrs.dto.BroadcastCatalog;
import com.smsc.management.app.analyze.cdrs.dto.CdrsFilterDataDTO;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CdrsControllerTest extends BaseIntegrationTest {

    @Autowired
    private CdrsController cdrsController;

    @MockBean
    private BroadcastRepository broadcastRepository;

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should return CDR data successfully")
    void testGetCdrsDataSuccess() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2025-06-04 00:00:00");
        filters.put("end_datetime", "2025-06-15 23:59:59");
        filters.put("status", "SUCCESS");
        filters.put("limit", 10);
        filters.put("offset", 1);

        // When
        ResponseEntity<ApiResponse> response = cdrsController.data(filters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request successfully", response.getBody().comment());
        assertEquals(HttpStatus.OK.value(), response.getBody().status());

        assertNotNull(response.getBody().data());
        CdrsFilterDataDTO result = (CdrsFilterDataDTO) response.getBody().data();
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle service exception gracefully for CDR data")
    void testGetCdrsDataServiceException() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "202s-06-04 00:00:00");
        filters.put("end_datetime", "2025-06-15 23:59:59");
        filters.put("status", "SUCCESS");

        // When
        ResponseEntity<ApiResponse> response = cdrsController.data(filters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().comment().startsWith("Error to get data"));
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should return broadcast catalog successfully")
    void testGetBroadcastCatalogSuccess() {
        // Given
        List<BroadcastCatalog> mockCatalog = List.of(
                new BroadcastCatalog(1, "Test Broadcast 1", 1, "testuser1"),
                new BroadcastCatalog(2, "Test Broadcast 2", 2, "testuser2")
        );
        when(broadcastRepository.findAllBroadcastForFilter()).thenReturn(mockCatalog);

        // When
        ResponseEntity<ApiResponse> response = cdrsController.broadcastCatalog();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request successfully", response.getBody().comment());
        assertEquals(HttpStatus.OK.value(), response.getBody().status());

        assertNotNull(response.getBody().data());
        @SuppressWarnings("unchecked")
        List<BroadcastCatalog> result = (List<BroadcastCatalog>) response.getBody().data();
        assertFalse(result.isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle service exception gracefully for broadcast catalog")
    void testGetBroadcastCatalogServiceException() {
        // Given
        when(broadcastRepository.findAllBroadcastForFilter())
                .thenThrow(new RuntimeException("Repository error"));

        // When
        ResponseEntity<ApiResponse> response = cdrsController.broadcastCatalog();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error to get broadcast catalog (Repository error)", response.getBody().comment());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle empty filters for CDR data")
    void testGetCdrsDataWithEmptyFilters() {
        // Given
        Map<String, Object> emptyFilters = new HashMap<>();

        // When
        ResponseEntity<ApiResponse> response = cdrsController.data(emptyFilters);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request successfully", response.getBody().comment());

        assertNotNull(response.getBody().data());
        CdrsFilterDataDTO result = (CdrsFilterDataDTO) response.getBody().data();
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle pagination parameters correctly")
    void testGetCdrsDataWithPagination() {
        // Given
        Map<String, Object> filtersWithPagination = new HashMap<>();
        filtersWithPagination.put("start_datetime", "2025-06-04 00:00:00");
        filtersWithPagination.put("end_datetime", "2025-06-15 23:59:59");
        filtersWithPagination.put("offset", 1);
        filtersWithPagination.put("limit", 10);

        // When
        ResponseEntity<ApiResponse> response = cdrsController.data(filtersWithPagination);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request successfully", response.getBody().comment());

        assertNotNull(response.getBody().data());
        CdrsFilterDataDTO result = (CdrsFilterDataDTO) response.getBody().data();
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("Should handle search filter correctly")
    void testGetCdrsDataWithSearchFilter() {
        // Given
        Map<String, Object> filtersWithSearch = new HashMap<>();
        filtersWithSearch.put("start_datetime", "2025-06-04 00:00:00");
        filtersWithSearch.put("end_datetime", "2025-06-15 23:59:59");
        filtersWithSearch.put("search_filter", "msgid12345");

        // When
        ResponseEntity<ApiResponse> response = cdrsController.data(filtersWithSearch);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request successfully", response.getBody().comment());

        assertNotNull(response.getBody().data());
        CdrsFilterDataDTO result = (CdrsFilterDataDTO) response.getBody().data();
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }
}
