package com.smsc.management.app.dnd.service;

import com.paicbd.smsc.scylla.ScyllaManager;
import com.paicbd.smsc.utils.DndType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DndScyllaService {
    private final ScyllaManager scyllaManager;

    public void insertDndEntry(int parentId, String dndValue, String dndType, String msisdn) {
        try {
            String valueToStore = DndType.GLOBAL.name().equals(dndType)
                    ? DndType.GLOBAL.name()
                    : dndValue;
            scyllaManager.insertIntoDndEntries(parentId, valueToStore, dndType, msisdn);
        } catch (Exception e) {
            log.error("Failed to insert into Scylla for msisdn {}: {}", msisdn, e.getMessage(), e);
        }
    }

    public void deleteDndEntriesByParentId(int parentId) {
        try {
            scyllaManager.deleteDndEntriesByParentId(parentId);
        } catch (Exception e) {
            log.error("Failed to delete DND entries from Scylla for parentId {}: {}", parentId, e.getMessage(), e);
        }
    }

    public void deleteSingleDndEntry(String dndType, String msisdn) {
        try {
            scyllaManager.deleteDndEntry(dndType, msisdn);
        } catch (Exception e) {
            log.error("Failed to delete single DND entry in Scylla (dndType={}, dndValue={}, msisdn={}): {}",
                    dndType, msisdn, e.getMessage(), e);
            throw e;
        }
    }


}
