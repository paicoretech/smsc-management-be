package com.smsc.management.app.dnd.service;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.component.DndData;
import com.smsc.management.app.dnd.dto.DisableDndRequest;
import com.smsc.management.app.dnd.dto.DndEntryMsisdnDTO;
import com.smsc.management.app.dnd.dto.DndEntryMsisdnFilterDataDTO;
import com.smsc.management.app.dnd.dto.DndRequestDTO;
import com.smsc.management.app.dnd.mapper.DndMapper;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doThrow;

class DndServiceTest extends BaseIntegrationTest {

    @Autowired
    private DndService dndService;

    @Autowired
    private DndEntryListRepository dndEntryListRepository;

    @SpyBean
    private DndMapper dndMapper;

    @SpyBean
    private DndEntryMsisdnRepository dndEntryMsisdnRepository;

    @MockBean
    private DndData dndData;

    @Test
    @DisplayName("getAll when data exists then return success, then simulate failure and return error")
    void testGetAllShouldReturnSuccessAndThenFail() {
        ApiResponse responseSuccess = dndService.getAll();
        assertEquals(200, responseSuccess.status());
        assertEquals("success", responseSuccess.message());

        doThrow(new RuntimeException("Simulated error")).when(dndMapper).toDtoList(Mockito.any());

        ApiResponse responseFail = dndService.getAll();
        assertEquals(500, responseFail.status());
        assertEquals("error", responseFail.message());
        assertTrue(responseFail.comment().contains("An error occurred while retrieving DND names."));
    }

    @Test
    @DisplayName("saveDndFile when valid sender type and file then return success")
    void testSaveDndFileShouldSucceedWithSenderType() {
        DndRequestDTO dto = new DndRequestDTO();
        dto.setName("Service-Level-Test");
        dto.setDndType(DndType.SENDER);
        dto.setDndValue("CLARO_SMS");

        String content = "msisdn\n5050000001\n5050000002";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());

        ApiResponse response = dndService.saveDndFile(dto, file);

        assertEquals(200, response.status());
        assertEquals("success", response.message());
    }

    @Test
    @DisplayName("saveDndEntry when repository throws exception then return error")
    void testSaveDndEntryShouldFailWhenExceptionIsThrown() {
        DndEntryList dndEntryList = new DndEntryList();
        dndEntryList.setDndType(DndType.SENDER);
        dndEntryList.setDndValue("CLARO_SMS");
        dndEntryList.setName("Test");
        dndEntryList.setStatus(DndStatus.ACTIVE);
        dndEntryList = dndEntryListRepository.save(dndEntryList);

        DndEntryMsisdnDTO dto = new DndEntryMsisdnDTO();
        dto.setMsisdns(List.of("5050000001"));
        dto.setParentId(dndEntryList.getId());

        doThrow(new RuntimeException("Simulated error"))
                .when(dndEntryMsisdnRepository)
                .save(Mockito.any());

        ApiResponse response = dndService.saveDndEntry(dto);

        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("An error occurred while saving the DND entry."));
    }

    @Test
    @DisplayName("saveDndEntry when MSISDN list is empty then return error")
    void testSaveDndEntryShouldFailWithEmptyMsisdnList() {
        DndEntryList dndEntryList = new DndEntryList();
        dndEntryList.setDndType(DndType.SENDER);
        dndEntryList.setDndValue("CLARO_SMS");
        dndEntryList.setName("Test-Empty-" + System.currentTimeMillis());
        dndEntryList = dndEntryListRepository.save(dndEntryList);

        DndEntryMsisdnDTO dto = new DndEntryMsisdnDTO();
        dto.setMsisdns(List.of());
        dto.setParentId(dndEntryList.getId());

        ApiResponse response = dndService.saveDndEntry(dto);

        assertEquals(400, response.status());
        assertEquals("error", response.message());
        assertEquals("MSISDN list cannot be empty.", response.comment());
    }

    @Test
    @DisplayName("disableDndEntryList when parentId does not exist then return error")
    void disableDndEntryListWhenParentIdDoesNotExistThenReturnError() {
        DisableDndRequest request = new DisableDndRequest();
        request.setParentId(999999);
        ApiResponse response = dndService.changeStatus(request.getParentId(), false);

        assertEquals(400, response.status());
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("Parent DND name with ID " + request.getParentId() + " does not exist."));
    }

    @Test
    @DisplayName("getDndEntries when valid parent_id then return success with paginated data")
    void testGetDndEntriesShouldReturnSuccessWithValidParentId() {
        // Create a test DND list
        DndEntryList dndEntryList = new DndEntryList();
        dndEntryList.setDndType(DndType.SENDER);
        dndEntryList.setDndValue("CLARO_SMS");
        dndEntryList.setName("Test DND List Success");
        dndEntryList = dndEntryListRepository.save(dndEntryList);

        // Prepare mock response from DndData
        DndEntryMsisdnFilterDataDTO mockResult = new DndEntryMsisdnFilterDataDTO();
        mockResult.setPage(1);
        mockResult.setPageSize(10);
        mockResult.setTotalElements(5);

        // Prepare filters
        Map<String, Object> filters = new HashMap<>();
        filters.put("parent_id", dndEntryList.getId());
        filters.put("offset", 1);
        filters.put("limit", 10);

        // Mock the DndData response
        Mockito.when(dndData.filterDndEntries(any())).thenReturn(mockResult);

        ApiResponse response = dndService.getDndEntries(filters);

        assertEquals(200, response.status());
        assertEquals("success", response.message());
        assertEquals("DND entries retrieved successfully", response.comment());
        assertNotNull(response.data());
        assertInstanceOf(DndEntryMsisdnFilterDataDTO.class, response.data());
    }

    @Test
    @DisplayName("getDndEntries when parent_id is null then return error")
    void testGetDndEntriesShouldFailWhenParentIdIsNull() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("offset", 1);
        filters.put("limit", 10);

        ApiResponse response = dndService.getDndEntries(filters);

        assertEquals(400, response.status());
        assertEquals("error", response.message());
        assertEquals("parent_id parameter is required", response.comment());
    }

    @Test
    @DisplayName("getDndEntries when parent_id is invalid format then return error")
    void testGetDndEntriesShouldFailWhenParentIdIsInvalidFormat() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("parent_id", "invalid_id");
        filters.put("offset", 1);
        filters.put("limit", 10);

        ApiResponse response = dndService.getDndEntries(filters);

        assertEquals(500, response.status());
    }

    @Test
    @DisplayName("getDndEntries when parent_id does not exist then return not found")
    void testGetDndEntriesShouldFailWhenParentIdDoesNotExist() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("parent_id", 99999); // Non-existent ID
        filters.put("offset", 1);
        filters.put("limit", 10);

        ApiResponse response = dndService.getDndEntries(filters);

        assertEquals(404, response.status());
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("DND list with id 99999 was not found"));
    }

    @Test
    @DisplayName("getDndEntries when exception occurs then return error")
    void testGetDndEntriesShouldFailWhenExceptionOccurs() {
        // Create a test DND list
        DndEntryList dndEntryList = new DndEntryList();
        dndEntryList.setDndType(DndType.SENDER);
        dndEntryList.setDndValue("CLARO_SMS");
        dndEntryList.setName("Test DND List Exception");
        dndEntryList = dndEntryListRepository.save(dndEntryList);

        Map<String, Object> filters = new HashMap<>();
        filters.put("parent_id", dndEntryList.getId());
        filters.put("offset", 1);
        filters.put("limit", 10);

        // Mock exception in DndData component
        doThrow(new RuntimeException("Database connection error"))
                .when(dndData)
                .filterDndEntries(any());

        ApiResponse response = dndService.getDndEntries(filters);

        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertTrue(response.comment().contains("Error retrieving DND entries"));
    }
}
