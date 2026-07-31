package com.smsc.management.app.dnd.component;

import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.processor.DndFileProcessor;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.StaticMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DndFileTask {

    private final List<DndFileProcessor> dndFileProcessors;
    private final DndEntryListRepository dndEntryListRepository;
    private final AppProperties appProperties;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void startProcessing(String uniqueFileName, DndEntryList dndEntryList) {
        executor.submit(() -> {
            log.info("Starting DND file processing for dndNameId: {}, file: {}", dndEntryList.getId(), uniqueFileName);
            try {
                InputStream inputStream = StaticMethods.getInputStreamFromFile(uniqueFileName, appProperties.getUploadFileDir());
                DndFileProcessor processor = dndFileProcessors.stream()
                        .filter(p -> p.supports(uniqueFileName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unsupported file format: " + uniqueFileName));

                processor.process(inputStream, dndEntryList);

            } catch (Exception e) {
                log.error("Error while processing DND file: {}", e.getMessage(), e);
                dndEntryList.setStatus(DndStatus.FAILED);
                dndEntryListRepository.save(dndEntryList);
            } finally {
                String filePath = appProperties.getUploadFileDir() + uniqueFileName;
                StaticMethods.deleteFile(filePath);
            }
        });
    }
}
