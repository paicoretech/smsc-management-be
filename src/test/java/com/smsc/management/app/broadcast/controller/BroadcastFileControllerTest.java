package com.smsc.management.app.broadcast.controller;

import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BroadcastFileControllerTest extends BaseIntegrationTest {

    @Autowired
    BroadcastFileController broadcastFileController;

    static Stream<Arguments> provideHeaderAndExpectedColumn() {
        return Stream.of(
                Arguments.of(
                        "broadcast_test_100000.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        true,
                        "{\"destination\":\"+10698899044\",\"totalAmount\":\"73319\",\"message\":\"using is balance our balance your is thanks\",\"date\":\"2024-08-21\"}"
                ),
                Arguments.of(
                        "broadcast_test_100000.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        false,
                        "{\"Column01\":\"destination\",\"Column02\":\"totalAmount\",\"Column03\":\"message\",\"Column04\":\"date\"}"
                ),
                Arguments.of(
                        "broadcast_test_100000.csv",
                        "text/csv",
                        true,
                        "{\"destination\":\"+10440806190\",\"totalAmount\":\"50973\",\"message\":\"our our service our is\",\"date\":\"2023-06-06\"}"
                ),
                Arguments.of(
                        "broadcast_test_100000.csv",
                        "text/csv",
                        false,
                        "{\"Column01\":\"destination\",\"Column02\":\"totalAmount\",\"Column03\":\"message\",\"Column04\":\"date\"}"
                )
        );
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @ParameterizedTest
    @MethodSource("provideHeaderAndExpectedColumn")
    void uploadExcelFileWithSuccessResult(String filename, String contentType, boolean hasHeader, String expectedColumn) throws IOException {
        new File("/tmp/broadcast/uploads/").mkdirs();
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getFile());
        FileInputStream input = new FileInputStream(file);
        var mockExcelFile = new MockMultipartFile(
                "file",
                file.getName(),
                contentType,
                input
        );
        ResponseEntity<ApiResponse> response = broadcastFileController.uploadFile(mockExcelFile, hasHeader, ",");

        // Assert: check the upload response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().message());
        var broadcastFileResponse = (BroadcastFile) response.getBody().data();
        assertNotNull(broadcastFileResponse);
        assertNotNull(broadcastFileResponse.getFilename());
        assertEquals(BroadcastStatus.CREATING, broadcastFileResponse.getStatus());
        assertEquals(expectedColumn, broadcastFileResponse.getColumns());

        // Act: fetch the uploaded file using the getFileDetail endpoint
        int generatedId = broadcastFileResponse.getId();
        ResponseEntity<ApiResponse> detailResponse = broadcastFileController.getFileDetail(generatedId);

        // Assert: check that the retrieved file matches the uploaded one
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertEquals("success", detailResponse.getBody().message());

        var detailFile = (BroadcastFile) detailResponse.getBody().data();
        assertNotNull(detailFile);
        assertEquals(generatedId, detailFile.getId());
        assertEquals(broadcastFileResponse.getFilename(), detailFile.getFilename());

        // Act & Assert: try fetching a non-existing file and expect a 500 error
        response = broadcastFileController.getFileDetail(-999);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().message());
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void uploadFileShouldReturnBadRequestWhenFileIsEmpty() {
        var emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        ResponseEntity<ApiResponse> response = broadcastFileController.uploadFile(emptyFile, true, ",");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody().message());
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void uploadFileShouldReturn500WhenUnsupportedFormat() {
        byte[] content = "test content".getBytes();
        var mockFile = new MockMultipartFile("file", "unsupported.xyz", "application/octet-stream", content);
        ResponseEntity<ApiResponse> response = broadcastFileController.uploadFile(mockFile, true, ",");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().message());
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void deleteFile() throws IOException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("broadcast_test_100000.csv")).getFile());
        FileInputStream input = new FileInputStream(file);
        var mockFile = new MockMultipartFile("file", file.getName(), "text/csv", input);
        ResponseEntity<ApiResponse> uploadResponse = broadcastFileController.uploadFile(mockFile, true, ",");

        var broadcastFile = (BroadcastFile) uploadResponse.getBody().data();
        int id = broadcastFile.getId();

        ResponseEntity<ApiResponse> response = broadcastFileController.deleteFile(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().message());

        response = broadcastFileController.deleteFile(-999);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().message());
    }
}
