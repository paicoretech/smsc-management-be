package com.smsc.management.app.analyze.cdrs.service;

import com.smsc.management.app.analyze.cdrs.component.CdrsData;
import com.smsc.management.app.analyze.cdrs.dto.BroadcastCatalog;
import com.smsc.management.app.analyze.cdrs.dto.CdrsFilterDataDTO;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdrsService {
    private final CdrsData cdrsData;
    private final BroadcastRepository broadcastRepository;

    public ApiResponse getDataLogs(Map<String, Object> filters) {
        try {
            CdrsFilterDataDTO result = cdrsData.filterData(filters);
            return ResponseMapping.successMessage("Request successfully", result);
        } catch (Exception e) {
            log.error("Error to get data {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error to get data", e);
        }
    }

    public ApiResponse getBroadcastCatalog() {
        try {
            List<BroadcastCatalog> broadcastCatalogList = broadcastRepository.findAllBroadcastForFilter();
            return ResponseMapping.successMessage("Request successfully", broadcastCatalogList);
        } catch (Exception e) {
            log.error("Error to get broadcast catalog {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error to get broadcast catalog", e);
        }
    }
}
