package com.smsc.management.app.catalog.controller;

import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetCatalogControllerTest extends BaseIntegrationTest {
    static final Map<String, Integer> expectedSizes = new HashMap<>();

    static {
        expectedSizes.put("bindstatuses", 7);
        expectedSizes.put("balancetype", 2);
        expectedSizes.put("interfazversion", 4);
        expectedSizes.put("bindtypes", 4);
        expectedSizes.put("ss7TrafficMode", 3);
        expectedSizes.put("ss7OriginType", 3);
        expectedSizes.put("slsRange", 3);
        expectedSizes.put("npicatalog", 10);
        expectedSizes.put("toncatalog", 7);
        expectedSizes.put("npicatalogrules", 11);
        expectedSizes.put("toncatalogrules", 8);
        expectedSizes.put("deliverystatus", 8);
        expectedSizes.put("ss7Functionality", 4);
        expectedSizes.put("ss7RuleType", 4);
        expectedSizes.put("encodingType", 4);
        expectedSizes.put("gtIndicators", 4);
        expectedSizes.put("numberingPlan", 12);
        expectedSizes.put("natureOfAddress", 129);
        expectedSizes.put("ss7LoadSharingAlgorithm", 6);
    }

    @Autowired
    private GetCatalogController getCatalogController;

    @ParameterizedTest
    @ValueSource(strings = {
            "bindstatuses", "balancetype", "interfazversion", "npicatalog", "toncatalog",
            "npicatalogrules", "toncatalogrules", "deliverystatus", "bindtypes", "ss7Functionality",
            "ss7TrafficMode", "ss7OriginType", "ss7RuleType", "ss7LoadSharingAlgorithm", "slsRange",
            "encodingType", "gtIndicators", "numberingPlan", "natureOfAddress"
    })
    void testGetCatalog(String catalogType) {
        ResponseEntity<ApiResponse> response = getCatalogController.bindStatuses(catalogType);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        if (expectedSizes.containsKey(catalogType)) {
            assertInstanceOf(List.class, response.getBody().data());
            List<?> result = (List<?>) response.getBody().data();
            assertEquals(expectedSizes.get(catalogType), result.size());
        }
    }

    @Test
    void testGetCatalogUnknown() {
        ResponseEntity<ApiResponse> response = getCatalogController.bindStatuses("unknown");
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}