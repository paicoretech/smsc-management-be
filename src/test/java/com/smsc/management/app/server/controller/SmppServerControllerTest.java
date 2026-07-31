package com.smsc.management.app.server.controller;

import com.smsc.management.app.server.dto.SmppServerDTO;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.smsc.management.utils.Constants.DEFAULT_SMPP_SERVER_NAME;
import static com.smsc.management.utils.Constants.DEFAULT_STATUS;
import static com.smsc.management.utils.Constants.UPDATE_SMPP_SERVER_LISTENER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

class SmppServerControllerTest extends BaseIntegrationTest {
    @Autowired
    SmppServerController smppServerController;

    @MockBean
    UtilsBase utilsBase;

    @Test
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @DisplayName("getSmppServers with correct value then do it successfully")
    void getSmppServersWhenAllIsGoodThenDoItSuccessfully() {
        ResponseEntity<ApiResponse> response = smppServerController.getSmppServers();
        ApiResponse apiResponse = response.getBody();

        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        var smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        assertEquals(2, smppServerResponseList.size());
        var smppServer = (SmppServerDTO) smppServerResponseList.getFirst();
        assertEquals("Default smpp server", smppServer.getName());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("newSmppServer with correct format then do it successfully")
    void newSmppServerWhenFormatIsCorrectThenDoItSuccessfully() {
        int countSmppServerBeforeCreate;
        int countSmppServerAfterCreate;

        // getting smpp server list before delete
        ResponseEntity<ApiResponse> response = smppServerController.getSmppServers();
        ApiResponse apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        var smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        countSmppServerBeforeCreate = smppServerResponseList.size();

        // creating smpp server
        SmppServerDTO smppServerConfig = getSmppServerDTO("new test smpp server", "192.168.1.1", 2777);
        response = smppServerController.newSmppServer(smppServerConfig);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = response.getBody();
        assertNotNull(apiResponse);
        var smppServerResponse = (SmppServerDTO) Objects.requireNonNull(apiResponse.data());
        assertEquals("new test smpp server", smppServerResponse.getName());

        // getting smpp server list after
        response = smppServerController.getSmppServers();
        apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        countSmppServerAfterCreate = smppServerResponseList.size();

        // There's one more on the list
        assertEquals(1, (countSmppServerAfterCreate - countSmppServerBeforeCreate));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("newSmppServer when constrain is violated then return Http status error")
    void newSmppServerWhenConstrainIsViolatedThenReturnHttpStatusError() {
        // getting smpp server list before delete
        ResponseEntity<ApiResponse> response = smppServerController.getSmppServers();
        ApiResponse apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);

        // creating smpp server
        SmppServerDTO smppServerConfig = getSmppServerDTO("new test smpp server", "127.0.0.1", 2776);
        response = smppServerController.newSmppServer(smppServerConfig);

        assertTrue(response.getStatusCode().is5xxServerError());
        apiResponse = response.getBody();
        assertNotNull(apiResponse);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Updating smpp server when smpp server exist then do it successfully")
    void updateSmppServerWhenSmppServerExistThenDoItSuccessfully() {
        doNothing().when(utilsBase).sendNotificationSocket(UPDATE_SMPP_SERVER_LISTENER, "1");

        // Updating enabled from 1 to 0 , 1 is default value
        SmppServerDTO smppServerConfig = new SmppServerDTO();
        smppServerConfig.setId(1);
        smppServerConfig.setIp("127.0.0.1");
        smppServerConfig.setName(DEFAULT_SMPP_SERVER_NAME);
        smppServerConfig.setPort(2776);
        smppServerConfig.setTransactionTimer(5000);
        smppServerConfig.setWaitForBind(5000);
        smppServerConfig.setProcessorDegree(15);
        smppServerConfig.setQueueCapacity(1000);
        smppServerConfig.setEnabled(0);
        smppServerConfig.setStatus(DEFAULT_STATUS);

        ResponseEntity<ApiResponse> response = smppServerController.updateSmppServer(1, smppServerConfig);

        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        var smppServerResponse = (SmppServerDTO) Objects.requireNonNull(apiResponse.data());
        assertEquals(0, smppServerResponse.getEnabled()); // field enabled default is 1 updating from 1 to 0

        // updating (ip, port, queue capacity)
        SmppServerDTO updateWithoutChangingEnabled  = new SmppServerDTO();
        updateWithoutChangingEnabled.setId(1);
        updateWithoutChangingEnabled.setIp("192.168.0.1");
        updateWithoutChangingEnabled.setName("trying to update name");
        updateWithoutChangingEnabled.setPort(2777);
        updateWithoutChangingEnabled.setTransactionTimer(5000);
        updateWithoutChangingEnabled.setWaitForBind(500);
        updateWithoutChangingEnabled.setProcessorDegree(15);
        updateWithoutChangingEnabled.setQueueCapacity(500);
        updateWithoutChangingEnabled.setEnabled(0);
        updateWithoutChangingEnabled.setStatus(DEFAULT_STATUS);

        response = smppServerController.updateSmppServer(1, updateWithoutChangingEnabled);

        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponseWithoutChangingEnabled = response.getBody();
        assertNotNull(apiResponseWithoutChangingEnabled);
        var smppServerResponseWithoutChangingEnabled = (SmppServerDTO) Objects.requireNonNull(apiResponseWithoutChangingEnabled.data());

        // Comparing between the two update responses
        assertEquals(smppServerResponse.getId(), smppServerResponseWithoutChangingEnabled.getId());
        assertEquals(DEFAULT_SMPP_SERVER_NAME, smppServerResponseWithoutChangingEnabled.getName()); // name not was updated
        assertNotEquals(smppServerResponse.getIp(), smppServerResponseWithoutChangingEnabled.getIp());
        assertNotEquals(smppServerResponse.getPort(), smppServerResponseWithoutChangingEnabled.getPort());
        assertNotEquals(smppServerResponse.getQueueCapacity(), smppServerResponseWithoutChangingEnabled.getQueueCapacity());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Update smpp server when constrain is violated then return http status error")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void updateSmppServerWhenConstrainIsViolatedThenReturnHttpStatusError() {
        doNothing().when(utilsBase).sendNotificationSocket(UPDATE_SMPP_SERVER_LISTENER, "1");

        // Updating enabled from 1 to 0 , 1 is default value
        SmppServerDTO smppServerConfig = getSmppServerDTO("this a test smpp server", "127.0.0.1", 2771);

        // create smpp server
        ResponseEntity<ApiResponse> response = smppServerController.newSmppServer(smppServerConfig);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        var smppServerResponse = (SmppServerDTO) Objects.requireNonNull(apiResponse.data());
        int newSmppServerId = smppServerResponse.getId();
        smppServerConfig.setId(newSmppServerId);

        // Setting the same value as the default smpp server
        smppServerConfig.setIp("127.0.0.1");
        smppServerConfig.setPort(2776);

        // updating
        response = smppServerController.updateSmppServer(newSmppServerId, smppServerConfig);

        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
        apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.comment().contains("ERROR: duplicate key value violates unique constraint"));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Updating a non-existent smpp server then return http status error NOT_FOUND")
    void updateSmppServerWhenSmppServerDoesNotExistThenReturnHttpStatusError() {
        SmppServerDTO smppServerDTO =  getSmppServerDTO("this a non-existent smpp server", "127.0.0.1", 2771);
        smppServerDTO.setId(10);
        ResponseEntity<ApiResponse> response = smppServerController.updateSmppServer(10, smppServerDTO);
        checkAssertions(response, HttpStatus.NOT_FOUND);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Updating smpp when parameter id does not match the payload ID  then return http status error INTERNAL_SERVER_ERROR")
    void updateSmppServerWhenIDDoesNotMatchThePayloadIdThenReturnHttpStatusError() {
        SmppServerDTO smppServerDTO =  getSmppServerDTO("this a non-existent smpp server", "127.0.0.1", 2771);
        smppServerDTO.setId(1);
        ResponseEntity<ApiResponse> response = smppServerController.updateSmppServer(10, smppServerDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Deleting smpp server when smpp server exist then do it successfully")
    void deleteSmppServerWhenSmppServerExistThenDoItSuccessfully() {
        int countSmppServerBeforeDelete;
        int countSmppServerAfterDelete;
        int smppServerId;

        // create new smpp server for deleting test
        SmppServerDTO smppServerConfig = getSmppServerDTO("new smpp server for deleting test", "192.168.1.2", 2778);
        ResponseEntity<ApiResponse> response = smppServerController.newSmppServer(smppServerConfig);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        var newSmppServerResponse = (SmppServerDTO) Objects.requireNonNull(apiResponse.data());
        smppServerId = newSmppServerResponse.getId();

        // getting smpp server list before delete
        response = smppServerController.getSmppServers();
        apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        var smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        countSmppServerBeforeDelete = smppServerResponseList.size();

        response = smppServerController.deleteSmppServer(smppServerId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", Objects.requireNonNull(response.getBody()).message());
        assertNull(Objects.requireNonNull(response.getBody()).data());

        // getting list after delete
        response = smppServerController.getSmppServers();
        apiResponse = response.getBody();
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(apiResponse);
        smppServerResponseList = (List<?>) Objects.requireNonNull(apiResponse.data());
        countSmppServerAfterDelete = smppServerResponseList.size();

        // There is one less on the list
        assertEquals(1, (countSmppServerBeforeDelete - countSmppServerAfterDelete));
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Deleting a non-existent smpp server then return http status error NOT_FOUND")
    void deleteSmppServerWhenSmppServerDoesNotExistThenReturnHttpStatusError() {
        ResponseEntity<ApiResponse> response = smppServerController.deleteSmppServer(10);
        checkAssertions(response, HttpStatus.NOT_FOUND);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Deleting default smpp server then return http status error")
    void deleteSmppServerWhenIsDefaultSmppServerThenReturnHttpStatusError() {
        ResponseEntity<ApiResponse> response = smppServerController.deleteSmppServer(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    @DisplayName("Create smpp server with tlsEnabled true then tlsEnabled persists through create and update")
    void smppServerTlsEnabledPersistsThroughCreateAndUpdate() {
        SmppServerDTO createDto = getSmppServerDTO("tls test server", "10.10.0.1", 2790);
        createDto.setTlsEnabled(true);

        ResponseEntity<ApiResponse> createResponse = smppServerController.newSmppServer(createDto);
        checkAssertions(createResponse, HttpStatus.OK);
        ApiResponse createApiResponse = createResponse.getBody();
        assertNotNull(createApiResponse);
        var createdServer = (SmppServerDTO) Objects.requireNonNull(createApiResponse.data());
        assertEquals("tls test server", createdServer.getName());
        assertTrue(createdServer.isTlsEnabled());

        int serverId = createdServer.getId();
        SmppServerDTO updateDto = getSmppServerDTO("tls test server", "10.10.0.1", 2790);
        updateDto.setId(serverId);
        updateDto.setTlsEnabled(false);

        ResponseEntity<ApiResponse> updateResponse = smppServerController.updateSmppServer(serverId, updateDto);
        checkAssertions(updateResponse, HttpStatus.OK);
        ApiResponse updateApiResponse = updateResponse.getBody();
        assertNotNull(updateApiResponse);
        var updatedServer = (SmppServerDTO) Objects.requireNonNull(updateApiResponse.data());
        assertFalse(updatedServer.isTlsEnabled());
    }

    private SmppServerDTO getSmppServerDTO(String name, String ip, int port) {
        SmppServerDTO smppServerConfig = new SmppServerDTO();
        smppServerConfig.setIp(ip);
        smppServerConfig.setName(name);
        smppServerConfig.setPort(port);
        smppServerConfig.setTransactionTimer(5000);
        smppServerConfig.setWaitForBind(5000);
        smppServerConfig.setProcessorDegree(15);
        smppServerConfig.setQueueCapacity(1000);
        smppServerConfig.setEnabled(0);
        smppServerConfig.setStatus(DEFAULT_STATUS);

        return smppServerConfig;
    }

    public static void checkAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus) {
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
}