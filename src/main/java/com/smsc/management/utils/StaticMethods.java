package com.smsc.management.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.kafka.KafkaTopicsConstants;
import com.smsc.management.app.user.model.entity.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StaticMethods {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private StaticMethods() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error while converting json to {} object", clazz.getName(), e);
            return null;
        }
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error while converting {} object to json", object.getClass().getName(), e);
            return null;
        }
    }

    public static UtilsRecords.BroadcastStatistic stringAsStatisticsEvent(String msgRaw) {
        try {
            return objectMapper.readValue(msgRaw, UtilsRecords.BroadcastStatistic.class);
        } catch (JsonProcessingException e) {
            log.warn("Error on converting string to MessageEvent {}", e.getMessage());
            return null;
        }
    }

    public static Object stringToObject(String value) {
        Object object;
        try {
            object = objectMapper.readValue(value, Object.class);
        } catch (JsonProcessingException e) {
            object = value;
        }
        return object;
    }

    public static boolean applyForUpdate(Object propertyValue) {
        boolean validList = true;
        if (propertyValue instanceof List<?> list) {
            validList = !list.isEmpty();
        }

        return Objects.nonNull(propertyValue) && !Objects.equals(propertyValue.toString(), "") && validList;
    }

    // Method to get the current user id from the security context
    public static Integer getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ((Users) principal).getId();
        } catch (Exception e) {
            log.debug("User active session was not found -> {}", e.getMessage());
        }
        return null;
    }

    public static FileInputStream getInputStreamFromFile(String filename, String dir) throws FileNotFoundException {
        String filePath = dir + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            log.error("File {} was not found in path {}", filename, filePath);
            throw new FileNotFoundException("File not found at path: " + filePath);
        }

        return new FileInputStream(file);
    }

    public static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.delete(path);
            log.info("File {} was deleted", filePath);
        } catch (Exception e) {
            log.error("Error while deleting file {} - > {}", filePath, e.getMessage());
        }
    }

    /**
     * Determines the appropriate Kafka topic based on Service Provider priority.
     * Routes messages to different Kafka topics for rate limiting based on priority level.
     * 
     * @param priority The priority level from Service Provider ("HIGH", "MEDIUM", "LOW")
     * @return The corresponding Kafka topic name
     */
    public static String getKafkaTopicByPriority(String priority) {
        // Handle null or empty priority - default to MEDIUM
        if (Objects.isNull(priority) || priority.isEmpty()) {
            log.warn("Service Provider priority is null or empty, defaulting to MEDIUM");
            return KafkaTopicsConstants.PRE_MESSAGE_MEDIUM_TOPIC;
        }
        
        return switch (priority.toUpperCase()) {
            case Constants.PRIORITY_HIGH -> {
                log.debug("Routing message to HIGH priority topic");
                yield KafkaTopicsConstants.PRE_MESSAGE_HIGH_TOPIC;
            }
            case Constants.PRIORITY_LOW -> {
                log.debug("Routing message to LOW priority topic");
                yield KafkaTopicsConstants.PRE_MESSAGE_LOW_TOPIC;
            }
            default -> {
                log.debug("Routing message to MEDIUM priority topic (priority={})", priority);
                yield KafkaTopicsConstants.PRE_MESSAGE_MEDIUM_TOPIC;
            }
        };
    }
}
