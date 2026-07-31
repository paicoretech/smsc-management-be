package com.smsc.management.app.report.exporter;

import com.smsc.management.app.analyze.cdrs.component.CdrsData;
import com.smsc.management.app.analyze.reports.component.ReportData;
import com.smsc.management.app.report.model.entity.ReportFile;
import com.smsc.management.app.report.model.repository.ReportFileRepository;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.app.report.utils.FileUtils;
import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.utils.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import com.paicbd.smsc.utils.RedisManager;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.smsc.management.app.analyze.utils.HelperQuery.FORMAT_TABLE_NAME;
import static com.smsc.management.utils.Constants.BROADCAST_FILTER_USER;
import static com.smsc.management.utils.Constants.TOTAL_COUNT_COLUMN;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelExporter implements Exporter {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ReportFileRepository reportFileRepository;
    private final RedisManager redisManager;
    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final RoutingRulesRepository rulesRepository;

    @Override
    public void exportCdr(Map<String, Object> filters, CdrsData cdrsData, ReportFile reportFile, FileType fileType) {
        FileStatus status;
        String tableTempName = String.format(FORMAT_TABLE_NAME, reportFile.getId());
        if (cdrsData.createReportDataTable(filters, fileType, tableTempName)) {
            log.info("Data for export report type {} created successfully", fileType.getValue());
            log.info("Table with name {} created successfully", tableTempName);
            status = createAndExportExcelFile(cdrsData, reportFile, fileType, tableTempName);
        } else {
            log.error("failed to create temp tabla for export report");
            status = FileStatus.FAILED;
        }
        this.completeFileProcess(status, reportFile);
        cdrsData.deleteReportDataTable(tableTempName);
    }

    private FileStatus createAndExportExcelFile(CdrsData cdrsData, ReportFile reportFile, FileType fileType, String tableName) {
        Map<String, Object> offsetAndLimit = new HashMap<>();
        log.info("Exporting CDRs to file: {}", reportFile.getPath());

        final int MAX_ROWS_PER_SHEET = 1_000_000;
        FileStatus status = FileStatus.COMPLETED;

        try (   FileOutputStream out = new FileOutputStream(reportFile.getPath());
                SXSSFWorkbook workbook = new SXSSFWorkbook(appProperties.getLoadCsvBatchSize())) {
            int sheetIndex = 1;
            Sheet sheet = workbook.createSheet(fileType.getValue() + "_Page_" + sheetIndex);
            int rowIndex = 0;

            CellStyle headerStyle = this.createCellWithStyle(workbook, true);
            CellStyle valueStyle = this.createCellWithStyle(workbook, false);
            List<String> headers;
            List<Map<String, Object>> dataForHeaders = cdrsData.getSimpleDataForHeaders(tableName, fileType);
            if (!dataForHeaders.isEmpty()) {
                headers = new ArrayList<>(dataForHeaders.getFirst().keySet());
                writeHeader(sheet, headers, rowIndex++, headerStyle);
            } else {
                log.info("No data found to export.");
                workbook.writeAvoidingTempFiles(out);
                return FileStatus.COMPLETED;
            }

            // create cuerpo
            AtomicReference<Sheet> currentSheet = new AtomicReference<>(sheet);
            AtomicLong processedCount = new AtomicLong(0);
            AtomicInteger currentSheetIndex = new AtomicInteger(sheetIndex);
            AtomicInteger currentRowIndex = new AtomicInteger(rowIndex);

            jdbcTemplate.query(cdrsData.getQueryForData(tableName, fileType), rs -> {
                processedCount.incrementAndGet();
                if (currentRowIndex.get() >= MAX_ROWS_PER_SHEET) {
                    currentSheetIndex.incrementAndGet();
                    Sheet newSheet = workbook.createSheet(fileType.getValue() + "_Page_" + currentSheetIndex.get());
                    currentSheet.set(newSheet);
                    currentRowIndex.set(0);
                    writeHeader(newSheet, headers, currentRowIndex.getAndIncrement(), headerStyle);
                }

                Row row = currentSheet.get().createRow(currentRowIndex.getAndIncrement());
                int colIndex = 0;

                for (String key : headers) {
                    Object value = rs.getObject(key);
                    Cell cell = row.createCell(colIndex++);
                    setCellValue(cell, valueStyle, value, key);
                }
            });

            long totalMessages = processedCount.get();
            Sheet sheetReportSummary = workbook.createSheet("Report Summary");
            this.createReportSummarySheet(
                    totalMessages, reportFile, offsetAndLimit, workbook, sheetReportSummary, fileType
            );

            workbook.writeAvoidingTempFiles(out);

        } catch (Exception e) {
            status = FileStatus.FAILED;
            FileUtils.cleanFile(new File(reportFile.getPath()));
            log.error("Error while exporting CDRs to file: {}", reportFile.getPath(), e);
        }

        return status;
    }

    private void writeHeader(Sheet sheet, List<String> headers, int rowIndex, CellStyle style) {
        Row headerRow = sheet.createRow(rowIndex);
        int colIndex = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(formatHeader(header));
            cell.setCellStyle(style);
        }
    }

    @Override
    public void exportCdrSummary(Map<String, Object> filters, ReportData reportData, ReportFile reportFile, FileType fileType) {
        var result = reportData.createReportData(filters, fileType);
        FileStatus status = FileStatus.COMPLETED;

        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                FileOutputStream out = new FileOutputStream(reportFile.getPath())
        ) {
            Sheet sheet = workbook.createSheet(fileType.getValue());
            long totalMessages = 0L;

            if (Objects.nonNull(result) && !result.isEmpty()) {
                CellStyle headerStyle = this.createCellWithStyle(workbook, true);
                CellStyle valueStyle = this.createCellWithStyle(workbook, false);
                int rowIndex = 0;
                int headerCol = 0;

                // header
                Map<String, Object> firstRow = result.getFirst();
                totalMessages = (long) firstRow.getOrDefault(TOTAL_COUNT_COLUMN, 0);
                firstRow.remove(TOTAL_COUNT_COLUMN);
                Row headerRow = sheet.createRow(rowIndex++);
                for (String header : firstRow.keySet()) {
                    Cell cell = headerRow.createCell(headerCol);
                    cell.setCellValue(formatHeader(header));
                    cell.setCellStyle(headerStyle);
                    headerCol++;
                }

                // body
                for (Map<String, Object> rowData : result) {
                    rowData = new LinkedHashMap<>(rowData);
                    rowData.remove(TOTAL_COUNT_COLUMN);
                    Row row = sheet.createRow(rowIndex++);
                    headerCol = 0;

                    for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                        Cell cell = row.createCell(headerCol++);
                        Object value = entry.getValue();
                        this.setCellValue(cell, valueStyle, value, entry.getKey());
                    }
                }
            }

            Sheet sheetReportSummary = workbook.createSheet("Report Summary");
            this.createReportSummarySheet(totalMessages, reportFile, filters, workbook, sheetReportSummary, fileType);

            workbook.writeAvoidingTempFiles(out);
        } catch (Exception e) {
            status =  FileStatus.FAILED;
            FileUtils.cleanFile(new File(reportFile.getPath()));
            log.error("Error while exporting summary report to file: {}", reportFile.getPath(), e);
        }

        this.completeFileProcess(status, reportFile);
    }

    private void setCellValue(Cell cell, CellStyle style, Object value, String key) {
        try {
            switch (value) {
                case null -> cell.setCellValue("");
                case Number number -> this.setCellValueConverted(cell, number, style);
                case Boolean b -> cell.setCellValue(b);
                default -> cell.setCellValue(value.toString());
            }
            cell.setCellStyle(style);
        } catch (Exception ex) {
            log.warn("Error on export cell [{}]: {} -> ", key, value, ex);
            cell.setCellValue("[ERROR]");
        }
    }

    private void setCellValueConverted(Cell cell, Number number, Object value) {
        try {
            cell.setCellValue(number.doubleValue());
        } catch (Exception e) {
            cell.setCellValue(value.toString());
        }
    }

    private void completeFileProcess(FileStatus status, ReportFile reportFile) {
        reportFile.setStatus(status);
        reportFileRepository.save(reportFile);
        if (FileStatus.COMPLETED.isEqual(reportFile.getStatus())) {
            log.info("File with id {} is ready to download", reportFile.getId());
            redisManager.setex(reportFile.getToken(), 86400, reportFile.getFilename());
        }
    }

    private String formatHeader(String snakeCase) {
        if (snakeCase == null || snakeCase.isBlank()) return "";
        return Arrays.stream(snakeCase.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private CellStyle createCellWithStyle(SXSSFWorkbook workbook, boolean forHeader) {
        CellStyle style = workbook.createCellStyle();
        Font fontHeader = workbook.createFont();
        short fontSize = 11;
        fontHeader.setFontHeightInPoints(fontSize);
        fontHeader.setBold(forHeader);
        style.setFont(fontHeader);

        return style;
    }

    private void createReportSummarySheet(long totalMessage, ReportFile reportFile, Map<String, Object> filters, SXSSFWorkbook workbook, Sheet sheet, FileType fileType) {
        CellStyle headerStyle = this.createCellWithStyle(workbook, true);
        CellStyle valueStyle = this.createCellWithStyle(workbook, false);

        Users user = userRepository.findById(reportFile.getCreatedById()).orElse(null);
        String userName = "";
        if (Objects.nonNull(user)) {
            userName = user.getName();
        }

        String accounts = this.verifyAccountFilters(filters);
        String usersFilter = this.verifyUsersFilters(filters);

        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 0, "Report File Name", reportFile.getFilename());
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 1, "User Name", userName);
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 2, "Aggregation Type", formatHeader(fileType.getValue()));
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 3, "Accounts", accounts);
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 4, "Users", usersFilter);
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 5, "Total Messages", totalMessage);
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 6, "Requested", reportFile.getCreatedAt());
        this.setValueForSummarySheet(sheet, headerStyle, valueStyle, 7, "Generated", LocalDateTime.now(ZoneId.systemDefault()));
    }

    private void setValueForSummarySheet(Sheet sheet, CellStyle headerStyle, CellStyle valueStyle, int index, String header, Object value) {
        Row row = sheet.createRow(index);
        Cell cell = row.createCell(0);
        this.setCellValue(cell, headerStyle, header, "");
        Cell cellValue = row.createCell(1);
        this.setCellValue(cellValue, valueStyle, value, "");
    }

    private String verifyAccountFilters(Map<String, Object> filters) {
        String accountStringFilter = "All Accounts";
        if (filters.containsKey("origin_network")) {
            Object accounts = filters.get("origin_network");
            List<Integer> networkIds = new ArrayList<>();
            if (accounts instanceof Collection) {
                networkIds.addAll((List<Integer>) accounts);
            } else {
                networkIds.add((Integer) accounts);
            }

            List<NetworksToRoutingRulesDTO> accountsFilter = rulesRepository.findByNetworkIds(networkIds);
            accountStringFilter = accountsFilter.stream()
                    .map(NetworksToRoutingRulesDTO::getName)
                    .collect(Collectors.joining(","));
        }

        return accountStringFilter;
    }

    private String verifyUsersFilters(Map<String, Object> filters) {
        String userStringFilter = "All Users";
        if (filters.containsKey(BROADCAST_FILTER_USER)) {
            Object usersId = filters.get(BROADCAST_FILTER_USER);
            List<Integer> users = new ArrayList<>();
            if (usersId instanceof Collection) {
                users.addAll((List<Integer>) usersId);
            } else {
                users.add((Integer) usersId);
            }

            List<Users> usersFilter = userRepository.findByIdIn(users);
            userStringFilter = usersFilter.stream()
                    .map(Users::getUsername)
                    .collect(Collectors.joining(","));
        }

        return userStringFilter;
    }
}
