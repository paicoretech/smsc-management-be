package com.smsc.management.app.report.controller;

import com.smsc.management.app.report.service.ReportFileService;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reportFile")
public class ReportFileController {

    private final ReportFileService reportFileService;
    private final ControllerSecurity controllerSecurity;

    @GetMapping
    public ResponseEntity<ApiResponse> listReportFiles (@RequestParam List<String> types) {
        controllerSecurity.checkSupportRoles();
        ApiResponse result = reportFileService.getReportFilesList(types);
        return ResponseEntity.status(result.status()).body(result);
    }

    @PostMapping("/cdrs/{fileType}")
    public ResponseEntity<ApiResponse> startDownloadReportForCdr(
            @PathVariable FileType fileType,
            @RequestBody @Valid Map<String, Object> filters) {
        controllerSecurity.checkSupportRoles();
        ApiResponse result = reportFileService.startDownloadReportForCdr(filters, fileType);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse> monitorDownloadProcess(@PathVariable int fileId) {
        controllerSecurity.checkSupportRoles();
        ApiResponse result = reportFileService.monitorDownloadProcess(fileId);
        return ResponseEntity.status(result.status()).body(result);
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<InputStreamResource> downloadProcess(@PathVariable String token) {
        return reportFileService.downloadFile(token);
    }
}
