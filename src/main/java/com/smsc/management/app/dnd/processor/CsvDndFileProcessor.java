package com.smsc.management.app.dnd.processor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import com.smsc.management.app.dnd.model.entity.DndEntryMsisdn;
import com.smsc.management.app.dnd.model.repository.DndEntryListRepository;
import com.smsc.management.app.dnd.model.repository.DndEntryMsisdnRepository;
import com.smsc.management.app.dnd.service.DndScyllaService;
import com.smsc.management.app.dnd.utils.DndStatus;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvDndFileProcessor implements DndFileProcessor {
    private final DndEntryListRepository dndEntryListRepository;
    private final DndEntryMsisdnRepository dndEntryMsisdnRepository;
    private final AppProperties appProperties;
    private final DndScyllaService dndScyllaService;

    enum FileProcessStatus { EMPTY, ALL_INVALID, SOME_INVALID, ALL_VALID }

    @Override
    public boolean supports(String fileType) {
        return fileType.toLowerCase().endsWith(".csv") || fileType.toLowerCase().endsWith(".txt");
    }

    @Override
    public void process(InputStream inputStream, DndEntryList dndEntryList) {
        int invalidCount = 0;
        int validCount = 0;
        int totalCount = 0;
        int duplicateCount = 0;

        log.info("Processing DND upload: listId={}, name='{}'", dndEntryList.getId(), dndEntryList.getName());
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).build()) {
            // skip header
            reader.readNext();
            final Pattern msisdnPattern = Pattern.compile(appProperties.getMsisdnRegex());

            String[] nextLine;
            List<DndEntryMsisdn> batch = new ArrayList<>();
            Set<String> msisdnSet = new HashSet<>();
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length == 0 || nextLine[0].isBlank()) continue;
                String msisdn = nextLine[0].trim();
                if (msisdn.isEmpty()) continue;
                totalCount++;

                if (!msisdnPattern.matcher(msisdn).matches()) {
                    invalidCount++;
                    continue;
                }
                if (!msisdnSet.add(msisdn)) {
                    duplicateCount++;
                    continue;
                }

                validCount++;
                DndEntryMsisdn entry = new DndEntryMsisdn();
                entry.setMsisdn(msisdn);
                entry.setParentId(dndEntryList.getId());
                batch.add(entry);

                dndScyllaService.insertDndEntry(
                        dndEntryList.getId(),
                        dndEntryList.getDndValue(),
                        dndEntryList.getDndType().name(),
                        msisdn
                );
                if (batch.size() >= appProperties.getLoadCsvBatchSize()) {
                    saveBatch(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                saveBatch(batch);
            }
            validateResultProcessor(dndEntryList, totalCount, validCount, invalidCount, duplicateCount);
            dndEntryListRepository.save(dndEntryList);
            log.info("Finished processing DND upload: listId={}, name='{}'", dndEntryList.getId(), dndEntryList.getName());
        } catch (Exception e) {
            log.error("Error processing DND upload: listId={}", dndEntryList.getId(), e);
            dndEntryList.setStatus(DndStatus.FAILED);
            dndEntryList.setComment("File processing failed: " + e.getMessage());
            dndEntryListRepository.save(dndEntryList);
        }
    }
    private void saveBatch(List<DndEntryMsisdn> entries) {
        try {
            dndEntryMsisdnRepository.saveAllAndFlush(entries);
        } catch (Exception e) {
            log.error("Error saving batch of DndEntry records: {}", e.getMessage(), e);
        }
    }

    private FileProcessStatus getFileCase(int totalCount, int validCount, int invalidCount, int duplicateCount) {
        if (totalCount == 0) return FileProcessStatus.EMPTY;
        if (validCount == 0) return FileProcessStatus.ALL_INVALID;
        if (invalidCount > 0 || duplicateCount > 0) return FileProcessStatus.SOME_INVALID;
        return FileProcessStatus.ALL_VALID;
    }

    private void validateResultProcessor(DndEntryList dndEntryList, int totalCount, int validCount, int invalidCount, int duplicateCount) {
        FileProcessStatus resultCase = getFileCase(totalCount, validCount, invalidCount, duplicateCount);
        switch (resultCase) {
            case EMPTY -> {
                String message = "Empty file: no MSISDN rows found.";
                dndEntryList.setStatus(DndStatus.FAILED);
                dndEntryList.setComment(message);
                log.warn("DND '{}' (id={}): empty file.", dndEntryList.getName(), dndEntryList.getId());
            }
            case ALL_INVALID -> {
                String message = invalidCount + " of " + totalCount + " MSISDNs discarded due to invalid format.";
                dndEntryList.setStatus(DndStatus.FAILED);
                dndEntryList.setComment(message);
                log.warn("DND '{}' (id={}): all {} rows discarded (invalid).", dndEntryList.getName(), dndEntryList.getId(), invalidCount);
            }
            case SOME_INVALID -> {
                String message = String.format(
                        "Some MSISDNs were discarded: %d duplicates, %d invalid format. %d out of %d were inserted successfully.",
                        duplicateCount,
                        invalidCount,
                        validCount,
                        totalCount
                );
                dndEntryList.setStatus(DndStatus.ACTIVE);
                dndEntryList.setComment(message);
                log.warn("DND '{}' (id={}): {}", dndEntryList.getName(), dndEntryList.getId(), message);
            }
            case ALL_VALID -> {
                dndEntryList.setStatus(DndStatus.ACTIVE);
                dndEntryList.setComment(null);
                log.info("DND '{}' (id={}): all {} rows inserted.", dndEntryList.getName(), dndEntryList.getId(), validCount);
            }
            default -> throw new IllegalStateException("Unexpected case: " + resultCase);
        }
    }
}