package com.smsc.management.app.analyze.dashboards.service;

import com.smsc.management.app.analyze.dashboards.component.DashboardsData;
import com.smsc.management.app.analyze.dashboards.dto.DashboardsDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardsService {
    private final DashboardsData dashboardsData;

    public ApiResponse getDashboards(Map<String, Object> filters) {
        try {
            DashboardsDTO result = dashboardsData.filterDataForDashboard(filters);
            return ResponseMapping.successMessage("Request successfully", result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error getting dashboards", e);
        }
    }
}
