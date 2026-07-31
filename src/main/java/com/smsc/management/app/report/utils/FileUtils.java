package com.smsc.management.app.report.utils;

import com.smsc.management.exception.SmscBackendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;

@Slf4j
public class FileUtils {

    private FileUtils() {throw new IllegalStateException("Utility class");}

    public static InputStreamResource createStreamFile(String dir, String filename) throws FileNotFoundException {
        File file = new File(dir, filename);
        if (!file.exists()) {
            log.error("File {} was not found in the server", filename);
            throw new FileNotFoundException("File was not found");
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        return new InputStreamResource(fileInputStream);
    }

    public static void cleanFile(File file) {
        try {
            if (!Files.deleteIfExists(file.toPath())) {
                throw new SmscBackendException("File " + file.getName() + " was not deleted.");
            }
        } catch (Exception e) {
            log.error("Error to delete file logs", e);
        }
    }
}
