package com.smsc.management.app.broadcast.service;

import com.smsc.management.app.broadcast.component.BroadcastFileTask;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.ResponseMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.paicbd.smsc.utils.RedisManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
public class BroadcastLogsService {
    private final BroadcastFileTask broadcastFileTask;
    private final BroadcastFileService broadcastFileService;
    private final AppProperties appProperties;
    private final BroadcastRepository broadcastRepository;
    private final RedisManager redisManager;
    private final ScheduledExecutorService executor;

    public BroadcastLogsService(BroadcastFileTask broadcastFileTask, BroadcastFileService broadcastFileService, AppProperties appProperties, BroadcastRepository broadcastRepository, RedisManager redisManager) {
        this.broadcastFileTask = broadcastFileTask;
        this.broadcastFileService = broadcastFileService;
        this.appProperties = appProperties;
        this.broadcastRepository = broadcastRepository;
        this.redisManager = redisManager;
        this.executor = Executors.newScheduledThreadPool(this.appProperties.getBroadcastThreadPoolSize());
    }

    public ResponseEntity<InputStreamResource> downloadFile(String token) {
        String filename = "";
        try {
            log.info("Downloading broadcast file with token: {}", token);
            filename = redisManager.get(token);
            InputStreamResource resource = broadcastFileService.createStreamFileLogs(appProperties.getReportBroadcastDir(), filename);

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
        } finally {
            broadcastFileService.cleanFileLogs(new File(appProperties.getReportBroadcastDir(), filename));
        }
    }

    public ApiResponse startDownloadProcess(int broadcastId) {
        try {
            if(!broadcastRepository.existsById(broadcastId)) {
                log.error("broadcast id {} was not found", broadcastId);
                throw new SmscBackendException("broadcast id " + broadcastId + " was not found");
            }
            BroadcastFile broadcastFile = startProcessLog(appProperties.getReportBroadcastDir(), broadcastId);
            return ResponseMapping.successMessage("Download logs process started", broadcastFile);
        } catch (Exception e) {
            log.error("Start download logs process request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Start download logs process request with error", e);
        }
    }

    public ApiResponse monitorDownloadProcess(int fileId) {
        try {
            BroadcastFile broadcastFile = broadcastFileService.getFileById(fileId);
            if (BroadcastStatus.CREATED.isEqual(broadcastFile.getStatus())) {
                log.info("Broadcast file with id {} is ready to download", fileId);
                redisManager.setex(broadcastFile.getToken(), 60, broadcastFile.getFilename());
            }
            return ResponseMapping.successMessage("Monitor download process request", broadcastFile);
        } catch (Exception e) {
            log.error("Monitor download process request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Monitor download process request with error", e);
        }
    }

    public BroadcastFile startProcessLog(String path, int broadcastId) {
        var fileName = String.format("broadcastId-%s-logs-%s.csv", broadcastId, System.currentTimeMillis());

        BroadcastFile broadcastFile = broadcastFileService.createEmptyLogFile(fileName);
        File file = new File(path, broadcastFile.getFilename());
        executor.submit(() -> broadcastFileTask.generateCSVFile(file, broadcastId, broadcastFile));
        return broadcastFile;
    }
}
