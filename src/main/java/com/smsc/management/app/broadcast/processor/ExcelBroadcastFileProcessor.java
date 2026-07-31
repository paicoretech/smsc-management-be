package com.smsc.management.app.broadcast.processor;

import com.github.pjfanning.xlsx.StreamingReader;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.service.BroadcastFileService;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExcelBroadcastFileProcessor extends FileProcessor implements BroadcastFileProcessor {
    private final DataFormatter dataFormatter = new DataFormatter();

    public ExcelBroadcastFileProcessor(BroadcastFileService broadcastFileService, NamedParameterJdbcTemplate jdbcTemplate) {
        super(broadcastFileService, jdbcTemplate);
    }

    @Override
    public void process(InputStream inputStream, int fileId, Broadcast broadcast, int batchSize, boolean hasHeaders, String delimiter) {
        log.info("Processing Excel file for broadcastId: {}", broadcast.getId());
        int totalMessage = 0;

        try (Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(batchSize)
                     .bufferSize(8192)
                     .open(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<BroadcastDevices> broadcastDevicesBatch = new ArrayList<>();
            broadcastFileService.deleteLastLoad(broadcast.getId());

            Iterator<Row> rowIterator = sheet.iterator();
            if (hasHeaders && rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isLineEmpty(row)) continue;

                BroadcastDevices broadcastDevices = getBroadcastDevices(rowToStringArray(row), broadcast);
                broadcastDevicesBatch.add(broadcastDevices);
                totalMessage++;

                if (totalMessage % batchSize == 0) {
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

            broadcastFileService.changeStatus(fileId, broadcast.getId(), BroadcastStatus.CREATED, totalMessage, "Successfully created");

        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            broadcastFileService.changeStatus(fileId, broadcast.getId(), BroadcastStatus.FAILED, totalMessage, "Error loading Excel file, please try again.");
        }
    }

    @Override
    public boolean supports(String fileType) {
        return fileType.toLowerCase().endsWith(".xlsx");
    }


    @Override
    public String extractColumns(InputStream inputStream, boolean detectHeader, String delimiter) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        try (Workbook workbook = StreamingReader.builder()
                .rowCacheSize(10)
                .bufferSize(8192)
                .open(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            List<String> headers = new ArrayList<>();
            if (!rowIterator.hasNext()) throw new FileProcessingException("Excel file is empty");
            if (detectHeader) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
            }

            if (rowIterator.hasNext()) {
                Row dataRow = rowIterator.next();
                int columnCount = dataRow.getLastCellNum();
                for (int i = 0; i < columnCount; i++) {
                    String key = detectHeader ? headers.get(i) : "Column" + String.format("%02d", i + 1);
                    String value = getCellValueAsString(dataRow.getCell(i));
                    resultMap.put(key, value);
                }
            }

        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            throw new FileProcessingException(e.getMessage());
        }

        return Converter.valueAsString(resultMap);
    }


    private String[] rowToStringArray(Row row) {
        int lastCellNum = row.getLastCellNum();
        String[] result = new String[lastCellNum];

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);
            result[i] = (cell != null) ? getCellValueAsString(cell) : "";
        }

        return result;
    }



    private boolean isLineEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> dataFormatter.formatCellValue(cell);
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
