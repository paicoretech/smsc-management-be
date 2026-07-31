package com.smsc.management.app.report.service;

import com.smsc.management.app.analyze.cdrs.component.CdrsData;
import com.smsc.management.app.analyze.reports.component.ReportData;
import com.smsc.management.app.report.exporter.ExcelExporter;
import com.smsc.management.app.report.model.entity.ReportFile;
import com.smsc.management.app.report.model.repository.ReportFileRepository;
import com.smsc.management.app.report.utils.FileExtension;
import com.smsc.management.app.report.utils.FileStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.app.report.utils.FileUtils;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.paicbd.smsc.utils.RedisManager;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportFileService {

    private final ReportFileRepository reportFileRepository;
    private final RedisManager redisManager;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final CdrsData cdrsData;
    private final ExcelExporter excelExporter;
    private final ReportData  reportData;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ApiResponse startDownloadReportForCdr(Map<String, Object> filters, FileType fileType) {
        try {

            FileExtension fileExtension = FileExtension.fromValue(filters.getOrDefault("exportAs", "xlsx").toString());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
            String fileName = String.format("%s-%s.%s", fileType.getValue().toLowerCase(), timestamp, fileExtension.getValue());
            ReportFile reportFile = createEmptyFile(fileName, fileType, fileExtension);
            executor.submit(() -> {
                try {
                    reportForCdrTask(reportFile, fileExtension, filters, fileType);
                } catch (Exception e) {
                    log.error("Error while executing report task: {}", e.getMessage(), e);
                    reportFile.setStatus(FileStatus.FAILED);
                    reportFileRepository.save(reportFile);
                }
            });
            return ResponseMapping.successMessage("Download process started", reportFile);
        } catch (Exception e) {
            log.error("Start download process request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Start download process request with error", e);
        }
    }

    public ApiResponse monitorDownloadProcess(int fileId) {
        try {
            ReportFile reportFile = reportFileRepository.findById(fileId);
            return ResponseMapping.successMessage("Monitor download process request", reportFile);
        } catch (Exception e) {
            log.error("Monitor download process request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Monitor download process request with error", e);
        }
    }

    public ResponseEntity<InputStreamResource> downloadFile(String token) {
        try {
            log.info("Downloading broadcast file with token: {}", token);
            String filename = redisManager.get(token);
            InputStreamResource resource = FileUtils.createStreamFile(appProperties.getReportBroadcastDir(), filename);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ApiResponse getReportFilesList (List<String> types) {
        try {
            var result = this.reportFileRepository.findAllByOrderByIdDesc(types);
            result.forEach(reportFile -> {
                if (FileStatus.COMPLETED.isEqual(reportFile.getStatus())) {
                    boolean exists = redisManager.exists(reportFile.getToken());
                    if (!exists) {
                        reportFile.setStatus(FileStatus.TOKEN_EXPIRED);
                    }
                }
            });
            return ResponseMapping.successMessage("Get report file request success", result);
        } catch (Exception e) {
            log.error("Get Report file list with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Report dile was end with error while getting data", e);
        }
    }

    public ReportFile createEmptyFile(String name, FileType type, FileExtension extension) {
        ReportFile reportFile = new ReportFile();
        reportFile.setFilename(name);
        reportFile.setPath(Paths.get(appProperties.getReportBroadcastDir(), name).toString());
        reportFile.setStatus(FileStatus.CREATING);
        reportFile.setExtension(extension);
        reportFile.setType(type.getName());
        reportFile.setToken(jwtService.generateToken());
        return reportFileRepository.save(reportFile);
    }

    public void reportForCdrTask(ReportFile reportFile, FileExtension extension, Map<String, Object> filters, FileType fileType) {
        log.info("Starting export task for reportFile: {}", reportFile.getFilename());
        if (FileExtension.XLSX.equals(extension)) {
            switch (fileType) {
                case CDRS, CDRS_DETAILED_REPORT -> excelExporter.exportCdr(filters, this.cdrsData, reportFile, fileType);
                default -> excelExporter.exportCdrSummary(filters, this.reportData, reportFile, fileType);
            }
        }
        log.info("Finished export task for reportFile: {}", reportFile.getFilename());
    }

}
