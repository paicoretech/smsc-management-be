package com.smsc.management.app.broadcast.controller;

import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.processor.BroadcastFileProcessor;
import com.smsc.management.app.broadcast.service.BroadcastFileService;
import com.smsc.management.security.ControllerSecurity;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/broadcast-file")
public class BroadcastFileController {
    private final BroadcastFileService broadcastFileService;
    private final List<BroadcastFileProcessor> processors;
    private final ControllerSecurity security;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getFileDetail(@PathVariable int id) {
        try {
            security.checkOperatorRoles();
            BroadcastFile file = broadcastFileService.getFileById(id);
            return ResponseEntity.ok(ResponseMapping.successMessage("Broadcast file retrieved successfully", file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMapping.exceptionMessage("Error retrieving broadcast file", e));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("hasHeader") boolean hasHeader,
            @RequestParam(value = "delimiter", defaultValue = ",") String delimiter
    ) {
        try {
            security.checkOperatorRoles();
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseMapping.errorMessage("File is required"));
            }
            BroadcastFileProcessor processor = processors.stream()
                    .filter(p -> p.supports(file.getOriginalFilename()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported file format: " + file.getOriginalFilename()));

            String columns = processor.extractColumns(file.getInputStream(), hasHeader, delimiter);

            BroadcastFile createdFile = broadcastFileService.saveBroadcastFile(file, hasHeader, columns, delimiter);
            return ResponseEntity.ok(ResponseMapping.successMessage("File uploaded successfully", createdFile));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMapping.exceptionMessage(e.getMessage(), e));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable int id) {
        try {
            security.checkOperatorRoles();
            BroadcastFile file = broadcastFileService.getFileById(id);
            java.io.File physicalFile = new java.io.File(file.getFilename());

            broadcastFileService.cleanFileLogs(physicalFile);
            broadcastFileService.deleteBroadcastFile(id);
            return ResponseEntity.ok(ResponseMapping.successMessage("File deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMapping.exceptionMessage("Error deleting file", e));
        }
    }
}
