package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastFileRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.report.utils.FileType;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.AppProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastFileService {
    private final BroadcastFileRepository broadcastFileRepository;
    private final BroadcastRepository broadcastRepository;
    private final BroadcastDevicesRepository broadcastDevicesRepository;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    public BroadcastFile saveBroadcastFile(MultipartFile file, boolean detectHeader, String columns, String delimiter) throws IOException {
        validateFile(file);

        String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = appProperties.getUploadBroadcastDir() + uniqueFilename;

        file.transferTo(new File(filePath));

        BroadcastFile broadcastFile = new BroadcastFile();
        broadcastFile.setFilename(uniqueFilename);
        broadcastFile.setSizeBytes(file.getSize());
        broadcastFile.setStatus(BroadcastStatus.CREATING);
        broadcastFile.setType(FileType.BROADCAST.getName());
        broadcastFile.setHasHeader(detectHeader);
        broadcastFile.setColumns(columns);
        broadcastFile.setDelimiter(delimiter);

        return broadcastFileRepository.save(broadcastFile);
    }

    public BroadcastFile createEmptyLogFile(String filename) {
        BroadcastFile broadcastFile = new BroadcastFile();
        broadcastFile.setFilename(filename);
        broadcastFile.setSizeBytes(0);
        broadcastFile.setStatus(BroadcastStatus.CREATING);
        broadcastFile.setType(FileType.LOGS.getName());
        broadcastFile.setToken(jwtService.generateToken());

        return broadcastFileRepository.save(broadcastFile);
    }

    public void validateFile(MultipartFile file) {
        if (Objects.isNull(file)) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() == 0) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    public BroadcastFile getFileById(int fileId) {
        BroadcastFile broadcastFile = broadcastFileRepository.findById(fileId);

        if (Objects.isNull(broadcastFile)) {
            log.error("File id {} was not found", fileId);
            throw new SmscBackendException("Configuration was not found with File id " + fileId);
        }

        return broadcastFile;
    }

    @Transactional
    public void changeStatus(int fileId, int broadcastId, BroadcastStatus status, int totalMessage, String message) {
        try {
            BroadcastFile broadcastFile = broadcastFileRepository.findById(fileId);
            if (Objects.isNull(broadcastFile)) {
                throw new SmscBackendException("File id " + fileId + " was not found to change status");
            }
            broadcastFile.setStatus(status);
            broadcastFile.setTotalRows(totalMessage);
            broadcastFileRepository.save(broadcastFile);

            // clean data in broadcast devices table
            if (BroadcastStatus.FAILED.isEqual(status)) {
                deleteLastLoad(broadcastId);
                broadcastRepository.changeStatus(broadcastId, status, LocalDateTime.now(ZoneOffset.UTC), message);
                return;
            }

            broadcastRepository.changeStatus(broadcastId, BroadcastStatus.PENDING, LocalDateTime.now(ZoneOffset.UTC), message);
        } catch (Exception e) {
            log.error("Error change status for loading file {}", e.getMessage());
        }
    }

    @Transactional
    public void saveEntitiesInBatch(List<BroadcastDevices> broadcastDevicesBatch) {
        broadcastDevicesRepository.saveAll(broadcastDevicesBatch);
    }

    public void deleteLastLoad(int broadcastId) {
        try {
            broadcastDevicesRepository.deleteAllByBroadcastId(broadcastId);
        } catch (Exception e) {
            log.error("Error deleting last load ", e);
            throw e;
        }
    }

    public InputStreamResource createStreamFileLogs(String dir, String filename) throws FileNotFoundException {
        File file = new File(dir, filename);
        if (!file.exists()) {
            log.error("File {} was not found in the server", filename);
            throw new FileNotFoundException("File was not found");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        return new InputStreamResource(fileInputStream);
    }

    public void cleanFileLogs(File file) {
        try {
            if (!Files.deleteIfExists(file.toPath())) {
                throw new SmscBackendException("File " + file.getName() + " was not deleted.");
            }
        } catch (Exception e) {
            log.error("Error to delete file logs", e);
        }
    }

    @Transactional
    public void deleteBroadcastFile(int fileId) {
        broadcastFileRepository.deleteById(fileId);
    }
}
