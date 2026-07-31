package com.smsc.management.app.broadcast.controller;

import com.paicbd.smsc.utils.BroadcastMessageStatus;
import com.smsc.management.app.broadcast.component.BroadcastQueryExecutor;
import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.dto.BroadcastStatusRequestDTO;
import com.smsc.management.app.broadcast.dto.BroadcastTestDTO;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastFileRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.service.BroadcastLogsService;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import static com.smsc.management.app.broadcast.utilsTest.Utils.checkBroadcastAssertions;
import static com.smsc.management.app.broadcast.utilsTest.Utils.getBroadcastDTO;
import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BroadcastControllerTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BroadcastController broadcastController;

    @MockBean
    private BroadcastRepository broadcastRepository;

    @MockBean
    private BroadcastDevicesRepository broadcastDevicesRepository;

    @MockBean
    private BroadcastFileRepository broadcastFileRepository;

    @MockBean
    private ServiceProviderRepository serviceProviderRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserServiceProviderRepository userServiceProviderRepository;

    @MockBean
    private BroadcastLogsService broadcastLogsService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private MultipartFile file;

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private Scheduler scheduler;

    @Mock
    private BroadcastQueryExecutor broadcastQueryExecutor;

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void getTest() {
        Mockito.when(broadcastRepository.getAllBroadcast()).thenReturn(List.of(Utils.getBroadcastViewer()));
        ResponseEntity<ApiResponse> response = broadcastController.get();
        checkBroadcastAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(broadcastRepository).getAllBroadcast();
        response = broadcastController.get();
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void getByBroadcastIdTest() {
        Broadcast broadcast = Utils.getBroadcast(true);
        BroadcastFile broadcastFile = Utils.getBroadcastFile();

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));
        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(broadcastFile);
        Mockito.when(broadcastQueryExecutor.getStatistics(anyInt())).thenReturn(Utils.getBroadcastStatistics());
        ResponseEntity<ApiResponse> response = broadcastController.getByBroadcastId(1);
        checkBroadcastAssertions(response, HttpStatus.OK);

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.empty());
        response = broadcastController.getByBroadcastId(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));
        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(null);
        response = broadcastController.getByBroadcastId(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void getCreateTest() throws IOException {
        setupMockAdminUser();
        BroadcastFile broadcastFile = Utils.getBroadcastFile();
        Broadcast broadcast = Utils.getBroadcast(true);

        Mockito.when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt())).thenReturn(true);
        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(null);
        ResponseEntity<ApiResponse> response = broadcastController.create(Utils.getBroadcastDTO());
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        File tempFile = File.createTempFile("broadcast-test-", ".csv");
        Files.write(tempFile.toPath(), "1322888089,1,1,1976606028,1,1,test\n".getBytes());

        String destinationDir = System.getProperty("file.upload.broadcast.dir", "/tmp/broadcast/uploads/");
        File targetDir = new File(destinationDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File finalPath = new File(destinationDir + tempFile.getName());
        Files.move(tempFile.toPath(), finalPath.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        broadcastFile.setFilename(tempFile.getName());
        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(broadcastFile);
        Mockito.when(broadcastRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));

        response = broadcastController.create(Utils.getBroadcastDTO());
        checkBroadcastAssertions(response, HttpStatus.OK);
        BroadcastDTO dto = (BroadcastDTO) Optional.ofNullable(response.getBody())
                .map(ApiResponse::data)
                .orElseThrow(() -> new AssertionError("Response body or data is null"));

        assertTrue(dto.getIsImmediate(), "Expected isImmediate to be true");

        Mockito.when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt())).thenReturn(false);
        response = broadcastController.create(Utils.getBroadcastDTO());
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        finalPath.deleteOnExit();
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    @DisplayName("Create new broadcast with invalid date then do nothing")
    void createBroadcastTestWhenMaxExecutionDateTimeIsBeforeStartDateTimeThenDoNothing() {
        BroadcastDTO broadcastDTO = Utils.getBroadcastDTO();
        broadcastDTO.setStartDateTime(LocalDateTime.now());
        broadcastDTO.setMaxExecutionDateTime(LocalDateTime.now().minusMinutes(5));
        ResponseEntity<ApiResponse> response = broadcastController.create(broadcastDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @ParameterizedTest
    @MethodSource("broadcastDeviceProvider")
    void cloneBroadcastTest(boolean broadcastExists) {
        if (!broadcastExists) {
            Mockito.when(broadcastRepository.findById(1)).thenReturn(null);
            ResponseEntity<ApiResponse> response = broadcastController.clone(1);
            checkBroadcastAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        Broadcast broadcast = Utils.getBroadcast(true,1);
        broadcast.setFile(Utils.getBroadcastFile());

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));

        Mockito.when(broadcastFileRepository.save(any())).thenReturn(Utils.getBroadcastFile());
        Mockito.when(broadcastRepository.save(any())).thenReturn(Utils.getBroadcast(true,1));

        ResponseEntity<ApiResponse> response = broadcastController.clone(broadcast.getId());
        checkBroadcastAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void cloneBroadcastTestWithDifferentDeviceConfigs() {
        Broadcast broadcast = Utils.getBroadcast(true,1);
        Mockito.when(broadcastRepository.findById(1)).thenReturn(null);
        ResponseEntity<ApiResponse> response = broadcastController.clone(broadcast.getId());
        checkBroadcastAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @ParameterizedTest(name = "updateTest with status {0} expects HTTP {1}")
    @MethodSource("provideBroadcastStatusesForUpdate")
    void updateTest(BroadcastStatus status, HttpStatus expectedStatus) throws IOException {
        setupMockAdminUser();
        BroadcastFile broadcastFile = Utils.getBroadcastFile();
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(status);

        Mockito.when(broadcastFileRepository.save(any())).thenReturn(broadcastFile);
        Mockito.when(broadcastFileRepository.findById(anyInt())).thenReturn(broadcastFile);

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));
        Mockito.when(broadcastRepository.save(any())).thenReturn(broadcast);
        Mockito.when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt()))
                .thenReturn(true);
        Mockito.when(executor.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(invocation -> null);

        String uploadDir = System.getProperty("file.upload.broadcast.dir", "/tmp/broadcast/uploads/");
        String generatedFilename = UUID.randomUUID() + "_test-file.csv";
        File finalFile = new File(uploadDir, generatedFilename);

        finalFile.getParentFile().mkdirs();
        Files.write(finalFile.toPath(), "1322888089,1,1,1976606028,1,1,test\n".getBytes());

        Mockito.when(file.getOriginalFilename()).thenReturn("test-file.csv");
        Mockito.when(file.getSize()).thenReturn(finalFile.length());
        Mockito.when(file.getInputStream()).thenReturn(new FileInputStream(finalFile));
        Mockito.doAnswer(invocation -> {
            File destination = invocation.getArgument(0);
            Files.copy(finalFile.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(file).transferTo(any(File.class));

        broadcastFile.setFilename(generatedFilename);

        boolean updatedFile = (status != BroadcastStatus.REJECTED); // Only false for REJECTED test
        ResponseEntity<ApiResponse> response = broadcastController.update(1, getBroadcastDTO(), updatedFile);

        checkBroadcastAssertions(response, expectedStatus);

        finalFile.deleteOnExit();
    }

    private static Stream<Arguments> provideBroadcastStatusesForUpdate() {
        return Stream.of(
                Arguments.of(BroadcastStatus.FAILED, HttpStatus.OK),
                Arguments.of(BroadcastStatus.PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(BroadcastStatus.CREATING, HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(BroadcastStatus.UPDATING, HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(BroadcastStatus.COMPLETED, HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(BroadcastStatus.REJECTED, HttpStatus.OK)
        );
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    @DisplayName("Update campaign with only messageTemplate change triggers async update successfully")
    void updateBroadcastWithMessageTemplateChangeTest() {
        setupMockAdminUser();
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.REJECTED);
        broadcast.setMessageTemplate("Sr. {{name}} your t-shirt is cost {{cost}}");

        BroadcastDTO updatedDto = getBroadcastDTO();
        updatedDto.setMessageTemplate("Sr. {{name}} your t-shirt is cost {{cost}} and delivered tomorrow");

        BroadcastDevices device = new BroadcastDevices();
        device.setMessageId("msg-12345");
        device.setBroadcastId(broadcast.getId());
        device.setMessage("old_message");
        device.setColumnMappingDataStr("{\"name\":\"John\"}");
        device.setStatus(BroadcastMessageStatus.PENDING.getValue());

        List<BroadcastDevices> devicesList = List.of(device);
        Page<BroadcastDevices> page = new org.springframework.data.domain.PageImpl<>(devicesList);

        Mockito.when(broadcastRepository.findByIdAndStatusNot(anyInt(), eq(BroadcastStatus.DELETED)))
                .thenReturn(Optional.of(broadcast));

        Mockito.when(serviceProviderRepository.existsByNetworkIdAndProtocolAndEnabledNot(anyInt(), anyString(), anyInt()))
                .thenReturn(true);

        Mockito.when(broadcastDevicesRepository.findByBroadcastIdAndStatus(
                anyInt(),
                eq(BroadcastMessageStatus.PENDING.getValue()),
                any(Pageable.class)
        )).thenReturn(page).thenReturn(Page.empty());

        ArgumentCaptor<List<BroadcastDevices>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.when(broadcastDevicesRepository.saveAllAndFlush(captor.capture()))
                .thenReturn(devicesList);

        Mockito.when(broadcastRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });

        ResponseEntity<ApiResponse> response = broadcastController.update(1, updatedDto, false);

        checkBroadcastAssertions(response, HttpStatus.OK);

        ArgumentCaptor<Broadcast> broadcastCaptor = ArgumentCaptor.forClass(Broadcast.class);
        verify(broadcastRepository, atLeastOnce()).save(broadcastCaptor.capture());

        Broadcast savedBroadcast = broadcastCaptor.getValue();
        assertEquals(
                updatedDto.getMessageTemplate(),
                savedBroadcast.getMessageTemplate(),
                "Expected the messageTemplate to be updated in the saved Broadcast"
        );
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void startDownloadLogsTest() {
        BroadcastFile broadcastFile = Utils.getBroadcastFile();

        ApiResponse errorResponse = Mockito.mock(ApiResponse.class);
        Mockito.when(errorResponse.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.when(errorResponse.message()).thenReturn("error");

        ApiResponse okResponse = Mockito.mock(ApiResponse.class);
        Mockito.when(okResponse.status()).thenReturn(HttpStatus.OK.value());
        Mockito.when(okResponse.message()).thenReturn("success");
        Mockito.when(okResponse.data()).thenReturn(broadcastFile);

        Mockito.when(broadcastRepository.existsById(anyInt())).thenReturn(false);
        Mockito.when(broadcastLogsService.startDownloadProcess(1)).thenReturn(errorResponse);

        ResponseEntity<ApiResponse> response = broadcastController.startDownloadLogs(1);
        checkBroadcastAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(broadcastRepository.existsById(anyInt())).thenReturn(true);
        Mockito.when(broadcastFileRepository.save(any())).thenReturn(broadcastFile);
        Mockito.when(executor.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(inv -> null);
        Mockito.when(broadcastLogsService.startDownloadProcess(1)).thenReturn(okResponse);

        response = broadcastController.startDownloadLogs(1);
        checkBroadcastAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void monitorDownloadProcessTest() {
        ApiResponse error = ResponseMapping.exceptionMessage(
                "Monitor download process request with error",
                new RuntimeException("boom")
        );

        BroadcastFile bfProcessing = Utils.getBroadcastFile();
        bfProcessing.setStatus(BroadcastStatus.PROCESSING);
        ApiResponse okProcessing = ResponseMapping.successMessage(
                "Monitor download process request",
                bfProcessing
        );

        BroadcastFile bfCreated = Utils.getBroadcastFile();
        bfCreated.setStatus(BroadcastStatus.CREATED);
        ApiResponse okCreated = ResponseMapping.successMessage(
                "Monitor download process request",
                bfCreated
        );

        Mockito.when(broadcastLogsService.monitorDownloadProcess(Mockito.anyInt()))
                .thenReturn(error)
                .thenReturn(okProcessing)
                .thenReturn(okCreated);

        ResponseEntity<ApiResponse> r1 = broadcastController.monitorDownloadProcess(1);
        checkBroadcastAssertions(r1, HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<ApiResponse> r2 = broadcastController.monitorDownloadProcess(1);
        checkBroadcastAssertions(r2, HttpStatus.OK);

        ResponseEntity<ApiResponse> r3 = broadcastController.monitorDownloadProcess(1);
        checkBroadcastAssertions(r3, HttpStatus.OK);
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void downloadProcessTest() {
        ResponseEntity<InputStreamResource> notFound = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Mockito.when(broadcastLogsService.downloadFile("testTokenForDownloadFile"))
                .thenReturn(notFound);

        ResponseEntity<InputStreamResource> response =
                broadcastController.downloadProcess("testTokenForDownloadFile");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Mockito.verify(broadcastLogsService).downloadFile("testTokenForDownloadFile");
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    void testForSendingMessageTestWithValidAndInvalidNetworkId(int networkId) {
        BroadcastTestDTO broadcastTestDTO = new BroadcastTestDTO();
        broadcastTestDTO.setNetworkId(networkId);
        broadcastTestDTO.setMessageTemplate("Sr. {{name}} your t-shirt is cost {{cost}}");
        broadcastTestDTO.setSourceAddr("");
        broadcastTestDTO.setDestinationAddr("50558499393");
        broadcastTestDTO.setSourceAddrTon(1);
        broadcastTestDTO.setSourceAddrNpi(2);
        broadcastTestDTO.setDestAddrTon(1);
        broadcastTestDTO.setDestAddrNpi(2);
        broadcastTestDTO.setDataCoding(0);

        Map<String, String> columnMapping = Map.of("ton", "sourceAddrTon", "npi", "sourceAddrNpi", "source_addr", "sourceAddr");
        Map<String, Object> columnMappingData = Map.of("source_addr", "12334", "ton", 1, "npi", 1, "cost", 2345, "name", "Juan Perez");

        broadcastTestDTO.setColumnMapping(columnMapping);
        broadcastTestDTO.setColumnMappingData(columnMappingData);

        ServiceProvider sp = new ServiceProvider();
        sp.setSystemId("test");
        sp.setNetworkId(1);
        sp.setProtocol("HTTP");
        sp.setAddressNpi(0);
        sp.setAddressTon(0);

        if (networkId == 1) {
            when(serviceProviderRepository.findByNetworkIdAndProtocolAndEnabledNot(1, "HTTP", DELETED_ENABLED_STATUS))
                    .thenReturn(sp);

            ResponseEntity<ApiResponse> response = broadcastController.messageTest(broadcastTestDTO);
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("success", response.getBody().message());
        } else {
            ResponseEntity<ApiResponse> response = broadcastController.messageTest(broadcastTestDTO);
            checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @WithMockUser(roles = "CAMPAIGN_OPERATOR")
    @Test
    void testForSendingMessageTestWhenColumnMappingHasOnlyColumnThenDoItSuccessfully() {
        BroadcastTestDTO broadcastTestDTO = new BroadcastTestDTO();
        broadcastTestDTO.setNetworkId(1);
        broadcastTestDTO.setMessageTemplate("Sr. {{name}} your t-shirt is cost {{cost}}");
        broadcastTestDTO.setSourceAddr("123456789");
        broadcastTestDTO.setDestinationAddr("50558499393");
        broadcastTestDTO.setSourceAddrTon(1);
        broadcastTestDTO.setSourceAddrNpi(2);
        broadcastTestDTO.setDestAddrTon(1);
        broadcastTestDTO.setDestAddrNpi(2);
        broadcastTestDTO.setDataCoding(0);

        Map<String, String> columnMapping = Map.of("ton", "destAddrTon", "npi", "destAddrNpi");
        broadcastTestDTO.setColumnMapping(columnMapping);
        Map<String, Object> columnMappingData = Map.of("ton", 1, "npi", 1, "cost", 2345, "name", "Juan Perez");
        broadcastTestDTO.setColumnMappingData(columnMappingData);

        ServiceProvider sp = new ServiceProvider();
        sp.setSystemId("test");
        sp.setNetworkId(1);
        sp.setProtocol("HTTP");

        when(serviceProviderRepository.findByNetworkIdAndProtocolAndEnabledNot(1, "HTTP", DELETED_ENABLED_STATUS))
                .thenReturn(sp);

        CompletableFuture<SendResult<String, String>> okFuture = new CompletableFuture<>();
        okFuture.complete(Mockito.mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(okFuture);

        ResponseEntity<ApiResponse> response = broadcastController.messageTest(broadcastTestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().message());
    }

    @WithMockUser(roles = { "ROOT", "CAMPAIGN_APPROVER" })
    @Test
    @DisplayName("Change status pending to approved broadcast status")
    void changeStatusTestWhenDataIsOkThenDoItSuccessfully() throws SchedulerException {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("APPROVED");
        statusRequestDTO.setComment("");
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.PENDING);

        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(new Date());
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(broadcastRepository.findById(broadcast.getId())).thenReturn(Optional.of(broadcast));
        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(broadcast.getId(), statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.OK);

        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        BroadcastDTO dataResponse = (BroadcastDTO) apiResponse.data();

        assertEquals(BroadcastStatus.SCHEDULED, dataResponse.getStatus());
    }

    @WithMockUser(roles = {"ROOT", "CAMPAIGN_APPROVER"})
    @Test
    @DisplayName("Change status pending to rejected broadcast status")
    void changeStatusTestFromPendingToRejectedWhenDataIsOkThenDoItSuccessfully() throws SchedulerException {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("REJECTED");
        statusRequestDTO.setComment("for testing");

        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.PENDING);

        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(new Date());
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(broadcastRepository.findById(broadcast.getId())).thenReturn(Optional.of(broadcast));
        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(broadcast.getId(), statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.OK);

        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        BroadcastDTO dataResponse = (BroadcastDTO) apiResponse.data();

        assertEquals(BroadcastStatus.valueOf("REJECTED"), dataResponse.getStatus());
        assertEquals("for testing", dataResponse.getComment());
    }

    @WithMockUser(roles = {"ROOT", "CAMPAIGN_APPROVER"})
    @Test
    @DisplayName("Change status scheduled to canceled broadcast status")
    void changeStatusTestFromScheduledToCanceledWhenDataIsOkThenDoItSuccessfully() throws SchedulerException {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("CANCELED");
        statusRequestDTO.setComment("");
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.SCHEDULED);

        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(new Date());
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(broadcastRepository.findById(broadcast.getId())).thenReturn(Optional.of(broadcast));
        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(broadcast.getId(), statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.OK);

        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        BroadcastDTO dataResponse = (BroadcastDTO) apiResponse.data();

        assertEquals(BroadcastStatus.valueOf("CANCELED"), dataResponse.getStatus());
    }

    @WithMockUser(roles = {"ROOT", "CAMPAIGN_APPROVER"})
    @Test
    @DisplayName("Change status draft to deleted broadcast status")
    void changeStatusTestFromDraftToRejectedWhenDataIsOkThenDoItSuccessfully() throws SchedulerException {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("DELETED");
        statusRequestDTO.setComment("");
        statusRequestDTO.setComment("deleted for testing");

        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.DRAFT);

        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenReturn(new Date());
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(broadcastRepository.findById(broadcast.getId())).thenReturn(Optional.of(broadcast));
        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(broadcast.getId(), statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.OK);

        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        BroadcastDTO dataResponse = (BroadcastDTO) apiResponse.data();

        assertEquals(BroadcastStatus.valueOf("DELETED"), dataResponse.getStatus());
        assertEquals("deleted for testing", dataResponse.getComment());
    }

    @WithMockUser(roles = "CAMPAIGN_APPROVER")
    @Test
    @DisplayName("Change status when new status is not allowed FAILED_TO_APPROVED")
    void changeStatusTestWithNewStatusNotAllowedThenHttpStatusErrorResponse() {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("APPROVED");
        statusRequestDTO.setComment("");

        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setStatus(BroadcastStatus.FAILED);

        when(broadcastRepository.findById(broadcast.getId())).thenReturn(Optional.of(broadcast));
        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(broadcast.getId(), statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = "CAMPAIGN_APPROVER")
    @Test
    @DisplayName("Change status when broadcast not found")
    void changeStatusTestWhenBroadcastNotFoundThenHttpStatusErrorResponse() {
        BroadcastStatusRequestDTO statusRequestDTO = new BroadcastStatusRequestDTO();
        statusRequestDTO.setBroadcastStatus("APPROVED");
        statusRequestDTO.setComment("");

        ResponseEntity<ApiResponse> response = broadcastController.changeStatus(1, statusRequestDTO);
        Utils.checkBroadcastAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static Stream<Arguments> broadcastDeviceProvider() {
        return Stream.of(
                arguments(true),
                arguments(true),
                arguments(true),
                arguments(false));
    }

    private void setupMockAdminUser() {
        Users mockUser = new Users();
        mockUser.setId(1);
        mockUser.setUserName("user");
        mockUser.setRoles(List.of("ROOT"));
        mockUser.setAllServiceProviders(true);
        Mockito.when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(mockUser));
    }
}
