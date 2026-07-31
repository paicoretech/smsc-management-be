package com.smsc.management.app.dnd.service;

import com.paicbd.smsc.scylla.ScyllaManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;

class DndScyllaServiceTest {

    private ScyllaManager scyllaManager;
    private DndScyllaService dndScyllaService;

    private static final int PARENT_ID = 1;
    private static final String DND_VALUE = "GLOBAL";
    private static final String DND_TYPE = "GLOBAL";
    private static final String MSISDN = "50512345678";

    @BeforeEach
    void setUp() {
        scyllaManager = mock(ScyllaManager.class);
        dndScyllaService = new DndScyllaService(scyllaManager);
    }

    @Test
    @DisplayName("Should call ScyllaManager to insert DND entry successfully")
    void insertDndEntryWhenValidParametersProvidedThenCallScyllaInsert() {
        dndScyllaService.insertDndEntry(PARENT_ID, DND_VALUE, DND_TYPE, MSISDN);

        verify(scyllaManager, times(1)).insertIntoDndEntries(PARENT_ID, DND_VALUE, DND_TYPE, MSISDN);
    }

    @Test
    @DisplayName("Should handle exception and log error when Scylla insertion fails")
    void insertDndEntryWhenScyllaThrowsExceptionThenHandleErrorGracefully() {
        doThrow(new RuntimeException("Scylla DB down"))
                .when(scyllaManager).insertIntoDndEntries(PARENT_ID, DND_VALUE, DND_TYPE, MSISDN);

        dndScyllaService.insertDndEntry(PARENT_ID, DND_VALUE, DND_TYPE, MSISDN);

        verify(scyllaManager, times(1)).insertIntoDndEntries(PARENT_ID, DND_VALUE, DND_TYPE, MSISDN);
    }

    @Test
    @DisplayName("Should call ScyllaManager to delete DND entries by parentId")
    void deleteDndEntriesByParentIdShouldCallScyllaManager() {
        dndScyllaService.deleteDndEntriesByParentId(PARENT_ID);

        verify(scyllaManager, times(1)).deleteDndEntriesByParentId(PARENT_ID);
    }

    @Test
    @DisplayName("Should handle exception and log error when Scylla deletion fails")
    void deleteDndEntriesByParentIdWhenScyllaThrowsExceptionThenHandleErrorGracefully() {
        doThrow(new RuntimeException("Delete failed"))
                .when(scyllaManager).deleteDndEntriesByParentId(PARENT_ID);

        dndScyllaService.deleteDndEntriesByParentId(PARENT_ID);

        verify(scyllaManager, times(1)).deleteDndEntriesByParentId(PARENT_ID);
    }
}
