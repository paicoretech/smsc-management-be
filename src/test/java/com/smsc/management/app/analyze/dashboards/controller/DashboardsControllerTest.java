package com.smsc.management.app.analyze.dashboards.controller;

import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardsControllerTest extends BaseIntegrationTest {
    @Autowired
    private DashboardsController dashboardsController;

    @MockBean
    private BroadcastRepository broadcastRepository;

    @WithMockUser(roles = "ADMINISTRATOR")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Get dashboards data with filters by date and status")
    void getDataWithUserFilterWhenErrorOccursThenHttpStatusResponseError(boolean isArrays) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("start_datetime", "2025-06-04 00:00:00");
        filters.put("end_datetime", "2025-06-04 23:59:59");
        if (isArrays) {
            filters.put("broadcast_filter_user", Arrays.asList(1, 3, 4));
            Mockito.when(broadcastRepository.findIdsByCreatedByUserIds(Arrays.asList(1, 3, 4))).thenThrow(new RuntimeException("just for testing"));
        } else {
            filters.put("broadcast_filter_user", 1);
            Mockito.when(broadcastRepository.findIdsByCreatedByUserId(1)).thenThrow(new RuntimeException("just for testing"));
        }

        ResponseEntity<ApiResponse> response = dashboardsController.data(filters);
        Assertions.assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}