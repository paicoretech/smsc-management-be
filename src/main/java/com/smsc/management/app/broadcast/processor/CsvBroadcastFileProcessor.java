package com.smsc.management.app.broadcast.processor;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.service.BroadcastFileService;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CsvBroadcastFileProcessor extends FileProcessor implements BroadcastFileProcessor {

    public CsvBroadcastFileProcessor(BroadcastFileService broadcastFileService, NamedParameterJdbcTemplate jdbcTemplate) {
        super(broadcastFileService, jdbcTemplate);
    }

    @Override
    public void process(InputStream inputStream, int fileId, Broadcast broadcast, int batchSize, boolean hasHeaders, String delimiter) {
        log.info("Processing CSV file for broadcastId: {}", broadcast.getId());
        int totalMessage = 0;
        char separator = delimiter.charAt(0);
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                .build()) {
            String[] nextLine;
            List<BroadcastDevices> broadcastDevicesBatch = new ArrayList<>();
            broadcastFileService.deleteLastLoad(broadcast.getId());

            while ((nextLine = csvReader.readNext()) != null) {
                // skipping empty line or header
                if (isLineEmpty(nextLine) || hasHeaders) {
                    hasHeaders = false;
                    continue;
                }

                BroadcastDevices broadcastDevices = this.getBroadcastDevices(nextLine, broadcast);
                broadcastDevicesBatch.add(broadcastDevices);

                if (++totalMessage % batchSize == 0) {
                    insertBroadcastDevicesNative(broadcastDevicesBatch);
                }
            }

            if (!broadcastDevicesBatch.isEmpty()) {
                insertBroadcastDevicesNative(broadcastDevicesBatch);
            }

            if (totalMessage < 1) {
                broadcastFileService.changeStatus(fileId, broadcast.getId(), BroadcastStatus.FAILED, totalMessage, "File is empty");
                return;
            }

            broadcastFileService.changeStatus(fileId, broadcast.getId(), BroadcastStatus.CREATED, totalMessage, "successfully created");
            log.info("Successfully created file for broadcastId: {}", broadcast.getId());

        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
            broadcastFileService.changeStatus(fileId, broadcast.getId(), BroadcastStatus.FAILED, totalMessage, "Error loading target file, please try again.");
        }
    }

    @Override
    public boolean supports(String fileType) {
        return fileType.toLowerCase().endsWith(".csv") || fileType.toLowerCase().endsWith(".txt");
    }

    @Override
    public String extractColumns(InputStream inputStream, boolean detectHeader, String delimiter) {
        char separator = delimiter.charAt(0);
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                .build()) {
            String[] headerOrData = reader.readNext(); // First line
            if (headerOrData == null || isLineEmpty(headerOrData)) {
                throw new FileProcessingException("CSV file is empty");
            }

            String[] keys;
            String[] values;

            if (detectHeader) {
                keys = trimArray(headerOrData);

                // Read next non-empty line as values
                String[] nextLine;
                do {
                    nextLine = reader.readNext();
                } while (nextLine != null && isLineEmpty(nextLine));

                if (nextLine == null || nextLine.length == 0) {
                    throw new FileProcessingException("CSV file is empty");
                }

                values = trimArray(nextLine);
            } else {
                values = trimArray(headerOrData);
                keys = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    keys[i] = "Column" + String.format("%02d", i + 1);
                }
            }

            Map<String, String> result = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(keys.length, values.length); i++) {
                result.put(keys[i], values[i]);
            }

            return Converter.valueAsString(result);

        } catch (Exception e) {
            log.error("Error extracting columns from CSV: {}", e.getMessage(), e);
            throw new FileProcessingException(e.getMessage());
        }
    }

    private String[] trimArray(String[] values) {
        String[] trimmed = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            trimmed[i] = values[i].trim();
        }
        return trimmed;
    }

    /**
     * Helper method to check if a line is empty.
     * A line is considered empty if it contains a single element, and that element is blank (could be empty row)
     * (either empty or consists only of whitespace characters).
     *
     * @param csvRow The array of strings representing a single row read from the CSV file.
     * @return {@code true} if the line is empty (contains only one blank element),
     * {@code false} otherwise.
     */
    private boolean isLineEmpty(String[] csvRow) {
        return csvRow.length == 1 && csvRow[0].isBlank();
    }


}
