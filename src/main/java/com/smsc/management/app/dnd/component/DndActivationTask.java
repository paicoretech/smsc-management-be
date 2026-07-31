package com.smsc.management.app.dnd.component;

import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.entity.DndEntryMsisdn;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.service.DndScyllaService;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor

public class DndActivationTask {
    private final DndEntryMsisdnRepository dndEntryMsisdnRepository;
    private final DndEntryListRepository dndEntryListRepository;
    private final DndScyllaService dndScyllaService;
    private final AppProperties appProperties;

    public void startDndActivationSync(int parentId) {
        int page = 0;
        int size = appProperties.getProcessingBatchSize();
        long total = 0L;

        try {
            DndEntryList entry = dndEntryListRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("DND list not found: " + parentId));

            Page<DndEntryMsisdn> msisdns;
            do {
                msisdns = dndEntryMsisdnRepository.findByParentId(parentId, PageRequest.of(page, size));
                for (DndEntryMsisdn m : msisdns.getContent()) {
                    dndScyllaService.insertDndEntry(
                            parentId, entry.getDndValue(), entry.getDndType().name(), m.getMsisdn()
                    );
                }
                total += msisdns.getNumberOfElements();
                page++;
            } while (msisdns.hasNext());

            entry.setStatus(DndStatus.ACTIVE);
            dndEntryListRepository.save(entry);
            log.info("DND activation completed for parentId {}. Total {} msisdns.", parentId, total);

        } catch (Exception e) {
            log.error("Activation failed for parentId {}: {}", parentId, e.getMessage(), e);
            dndEntryListRepository.findById(parentId).ifPresent(entry -> {
                entry.setStatus(DndStatus.FAILED);
                dndEntryListRepository.save(entry);
            });
        }
    }
}
