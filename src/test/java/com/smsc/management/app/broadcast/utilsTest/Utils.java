package com.smsc.management.app.broadcast.utilsTest;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.dto.BroadcastRecordsResponse;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Utils {
    public static void checkBroadcastAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus) {
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();

        switch (httpStatus) {
            case OK -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("success", response.getBody().message());
                assertNotNull(Objects.requireNonNull(apiResponse).data());
            }
            case NOT_FOUND -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case BAD_REQUEST -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case INTERNAL_SERVER_ERROR -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNull(response.getBody().data());
                assertEquals("error", response.getBody().message());
            }
            default -> throw new IllegalStateException("Unexpected value: " + response.getStatusCode());
        }
    }

    public static BroadcastRecordsResponse.BroadcastViewer getBroadcastViewer() {
        return new BroadcastRecordsResponse.BroadcastViewer(
                1,
                "test",
                BroadcastStatus.SCHEDULED,
                20,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "",
                1,
                "UserName");

    }

    public static BroadcastFile getBroadcastFile() {
        String broadcastFileStr = """
                {
                    "id": 1,
                    "filename": "broadcastId-1-logs-1727198723192.csv",
                    "sizeBytes": 0,
                    "totalRows": 0,
                    "status": "CREATING",
                    "type": "LOGS",
                    "columns": "",
                    "hasHeader": true
                }
                """;
        return Converter.stringToObject(broadcastFileStr, BroadcastFile.class);
    }

    public static Broadcast getBroadcast(boolean ok, int fileId) {
        Map<String, Object> columnMapData = new LinkedHashMap<>();
        columnMapData.put("sender", "739769082");
        columnMapData.put("ston", "1");
        columnMapData.put("snpi", "0");
        columnMapData.put("destination", "9739769082");
        columnMapData.put("dton", "0");
        columnMapData.put("dnpi", "0");
        columnMapData.put("message", "test");
        columnMapData.put("name", "John Doe");
        columnMapData.put("cost", "50");

        Broadcast broadcast = new Broadcast();
        broadcast.setId(1);
        broadcast.setName("this a test");
        broadcast.setDescription("this a test good");
        broadcast.setNetworkId(1);
        broadcast.setFileId(fileId);
        broadcast.setColumnMapping(Converter.valueAsString(getColumnMapping(ok)));
        broadcast.setFirstRecordMapping(Converter.valueAsString(columnMapData));
        broadcast.setMessageTemplate("Hi Mr. {{name}} the pants is cost {{cost}}");
        broadcast.setStatus(BroadcastStatus.SCHEDULED);
        broadcast.setStartDateTime(LocalDateTime.now());
        broadcast.setMaxExecutionDateTime(LocalDateTime.now().plusDays(2));

        return broadcast;
    }

    public static Broadcast getBroadcast(boolean ok) {
      return getBroadcast(ok, 4);
    }

    public static BroadcastRecordsResponse.BroadcastStatistics getBroadcastStatistics() {
        return new BroadcastRecordsResponse.BroadcastStatistics(
                100,
                50,
                10,
                30,
                10,
                5,
                3
        );
    }

    public static InputStream getFileStream(boolean includeHeader) {
        String file = "sourceAddress,sourceTon,sourceNpi,destinationAddress,destinationTon,destinationNpi,message,name,cost\n739769082,1,0,9739769082,0,0,test,John Doe, 50";
        if (!includeHeader) {
            file = "739769082,1,0,9739769082,0,0,test,John Doe, 50";
        }
        return new ByteArrayInputStream(file.getBytes());
    }

    public static String getResultResponse() {
        return "{\"broadcast_id\":1, \"message_id\": \"testId\", \"status\": 2, \"date\": \"2024-09-25 16:49:00\"}";
    }

    public static BroadcastDevices getBroadcastDevices(String messageId) {
        BroadcastDevices broadcastDevices = new BroadcastDevices();
        broadcastDevices.setBroadcastId(1);
        broadcastDevices.setMessageId(messageId);
        broadcastDevices.setMessage("Hi Mr. {{name}} the pants is cost {{cost}}");

        Map<String, Object> columnMapData = new LinkedHashMap<>();
        columnMapData.put("sender", "739769082");
        columnMapData.put("ston", "1");
        columnMapData.put("snpi", "0");
        columnMapData.put("destination", "9739769082");
        columnMapData.put("dton", "0");
        columnMapData.put("dnpi", "0");
        columnMapData.put("message", "test");
        columnMapData.put("name", "John Doe");
        columnMapData.put("cost", "50");

        broadcastDevices.setColumnMappingDataStr(Converter.valueAsString(columnMapData));

        return broadcastDevices;
    }

    public static BroadcastDevices getBroadcastDevices() {
        String messageId = System.currentTimeMillis() + "-" + System.nanoTime();
        return getBroadcastDevices(messageId);
    }

    public static BroadcastDTO getBroadcastDTO() {
        BroadcastDTO broadcastDTO = new BroadcastDTO();
        broadcastDTO.setId(1);
        broadcastDTO.setName("this a test");
        broadcastDTO.setDescription("this a test good");
        broadcastDTO.setNetworkId(1);
        broadcastDTO.setFileId(1);
        broadcastDTO.setColumnMapping(getColumnMapping(true));
        broadcastDTO.setMessageTemplate("Hi Mr. {{name}} the pants is cost {{cost}}");
        broadcastDTO.setStartDateTime(LocalDateTime.now());
        broadcastDTO.setMaxExecutionDateTime(LocalDateTime.now().plusDays(2));
        broadcastDTO.setSourceAddrTon(1);
        broadcastDTO.setSourceAddrNpi(2);
        broadcastDTO.setDestAddrTon(1);
        broadcastDTO.setDestAddrNpi(2);
        broadcastDTO.setIsImmediate(true);

        return broadcastDTO;
    }

    private static Map<String, String> getColumnMapping(boolean isOk) {
        Map<String, String> columnMapping = new LinkedHashMap<>();
        if (isOk) {
            columnMapping.put("sender", "sourceAddr");
        } else {
            columnMapping.put("sender", "sourceAddress");
        }

        columnMapping.put("ston", "sourceAddrTon");
        columnMapping.put("snpi", "sourceAddrNpi");
        columnMapping.put("destination", "destinationAddr");
        columnMapping.put("dton", "destAddrTon");
        columnMapping.put("dnpi", "destAddrNpi");
        columnMapping.put("message", "shortMessage");
        columnMapping.put("name", "");
        columnMapping.put("cost", "");

        return columnMapping;
    }
}
