package com.smsc.management.app.server.service;

import com.paicbd.smsc.dto.SmppServerConfig;
import com.smsc.management.app.server.dto.SmppServerDTO;
import com.smsc.management.app.server.mapper.ServerMapper;
import com.smsc.management.app.server.model.entity.SmppServer;
import com.smsc.management.app.server.model.repository.SmppServerRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static com.smsc.management.utils.Constants.DEFAULT_STATUS;
import static com.smsc.management.utils.Constants.DISABLED_ENABLED_STATUS;
import static com.smsc.management.utils.Constants.STARTED_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SmppServerServiceTest {
    @Mock
    SmppServerRepository smppServerRepository;

    @Mock
    ServerMapper serverMapper;

    @Mock
    UtilsBase utilsBase;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    SmppServerService smppServerService;

    @Test
    @DisplayName("New smpp server when an exception occurs then HTTP Status Error returns")
    void createSmppServerWhenExceptionOccursThenReturnHttpStatusError() {
        SmppServerDTO smppServerConfig = new SmppServerDTO();
        smppServerConfig.setIp("127.0.0.1");
        smppServerConfig.setName("Default smpp server");
        smppServerConfig.setPort(2776);
        smppServerConfig.setTransactionTimer(5000);
        smppServerConfig.setWaitForBind(5000);
        smppServerConfig.setProcessorDegree(15);
        smppServerConfig.setQueueCapacity(1000);
        smppServerConfig.setEnabled(0);
        smppServerConfig.setStatus(DEFAULT_STATUS);

        SmppServer smppServer = new SmppServer();

        when(this.serverMapper.toEntity(smppServerConfig)).thenReturn(smppServer);
        when(this.smppServerRepository.saveAndFlush(any(SmppServer.class))).thenThrow(new RuntimeException("Error to throw exception test"));
        ApiResponse response = this.smppServerService.createSmppServer(smppServerConfig);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertNull(response.data());

        verify(this.serverMapper).toEntity(smppServerConfig);
        verify(this.smppServerRepository).saveAndFlush(smppServer);
    }

    @Test
    @DisplayName("Update smpp server when an exception occurs then HTTP Status Error returns")
    void updateSmppServerWhenExceptionOccursThenReturnHttpStatusError() {
        SmppServerDTO smppServerConfig = new SmppServerDTO();
        smppServerConfig.setId(2);
        smppServerConfig.setIp("192.0.0.1");
        smppServerConfig.setName("Not default smpp server");
        smppServerConfig.setPort(2776);
        smppServerConfig.setTransactionTimer(5000);
        smppServerConfig.setWaitForBind(5000);
        smppServerConfig.setProcessorDegree(15);
        smppServerConfig.setQueueCapacity(1000);
        smppServerConfig.setEnabled(0);
        smppServerConfig.setStatus(DEFAULT_STATUS);

        SmppServer smppServerMock = new SmppServer();
        smppServerMock.setId(2);
        smppServerMock.setIp("127.0.0.1");
        smppServerMock.setName("Not default smpp server");
        smppServerMock.setPort(2776);
        smppServerMock.setTransactionTimer(5000);
        smppServerMock.setWaitForBind(5000);
        smppServerMock.setProcessorDegree(15);
        smppServerMock.setQueueCapacity(1000);
        smppServerMock.setEnabled(0);
        smppServerMock.setStatus(DEFAULT_STATUS);

        when(this.smppServerRepository.findById(2)).thenReturn(smppServerMock);
        doThrow(new RuntimeException("Error to throw exception test")).when(this.serverMapper).updateEntityFromDTO(smppServerConfig, smppServerMock);
        ApiResponse response = this.smppServerService.updateSmppServer(2, smppServerConfig);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertNull(response.data());

        verify(this.smppServerRepository).findById(2);
        verify(this.serverMapper).updateEntityFromDTO(smppServerConfig, smppServerMock);
        verifyNoInteractions(this.utilsBase);
        verifyNoInteractions(this.appProperties);
    }

    @Test
    @DisplayName("Delete smpp server when an exception occurs then HTTP Status Error returns")
    void deleteSmppServerWhenExceptionOccursThenReturnHttpStatusError() {
        when(this.smppServerRepository.findById(1)).thenReturn(new SmppServer());
        when(this.serverMapper.toRedisDTOFromEntity(any(SmppServer.class))).thenReturn(new SmppServerConfig());
        doThrow(new RuntimeException("Error to test throw exception")).when(utilsBase).storeInRedis(anyString(), anyString(), anyString());

        ApiResponse response = this.smppServerService.deleteSmppServer(1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertNull(response.data());

        verify(this.smppServerRepository).findById(1);
        verify(this.smppServerRepository).delete(any(SmppServer.class));
        verify(this.serverMapper).toRedisDTOFromEntity(any(SmppServer.class));
        verify(this.utilsBase).storeInRedis(anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @MethodSource("updateSmppServerStatusTestCases")
    @DisplayName("Updating the status of an existing smpp server")
    void updatingStatusFromSmppServerCorsWhenSmppServerExistsThenDoItSuccessfully(SmppServer smppServer, String status) {
        when(this.smppServerRepository.findById(smppServer.getId())).thenReturn(smppServer);
        SmppServerConfig config = new SmppServerConfig();
        config.setId(smppServer.getId());
        when(this.serverMapper.toRedisDTOFromEntity(smppServer)).thenReturn(config);
        smppServerService.updatingStatusFromSmppServerCors(smppServer.getId(), status, "success");
        verify(this.smppServerRepository).findById(smppServer.getId());
        ArgumentCaptor<Integer> enabledCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(this.smppServerRepository)
                .updateStatus(eq(smppServer.getId()), eq(status), eq("success"), enabledCaptor.capture());
        verify(this.serverMapper).toRedisDTOFromEntity(any(SmppServer.class));
        verify(this.utilsBase).storeInRedis(anyString(), anyString(), anyString());
        // changing status
        if (DEFAULT_STATUS.equals(status)) {
            assertEquals(DISABLED_ENABLED_STATUS, enabledCaptor.getValue());
        } else if (STARTED_STATUS.equals(status)) {
            assertEquals(1, enabledCaptor.getValue());
        } else if ("FORCED_STOPPED_STATUS".equals(status)) {
            assertEquals(1, enabledCaptor.getValue());
        }
        verifyNoInteractions(this.appProperties);
    }


    @Test
    @DisplayName("Updating the status of a smpp server that does not exist")
    void updatingStatusFromSmppServerCorsWhenSmppServerDoesNotExistThenDoNothing() {
        when(this.smppServerRepository.findById(1)).thenReturn(null);
        smppServerService.updatingStatusFromSmppServerCors(1, STARTED_STATUS, "success");

        verify(this.smppServerRepository).findById(1);
        verify(this.smppServerRepository, never()).updateStatus(1, STARTED_STATUS, "success", 1);
        verifyNoInteractions(this.serverMapper);
        verifyNoInteractions(this.utilsBase);
        verifyNoInteractions(this.appProperties);
    }

    @Test
    @DisplayName("Update smpp server with tlsEnabled true then Redis receives config with tls_enabled true")
    void updateSmppServerWithTlsEnabledTrueStoresCorrectConfigInRedis() {
        SmppServerDTO dto = new SmppServerDTO();
        dto.setId(1);
        dto.setIp("127.0.0.1");
        dto.setName("tls server");
        dto.setPort(2776);
        dto.setTransactionTimer(5000);
        dto.setWaitForBind(5000);
        dto.setProcessorDegree(15);
        dto.setQueueCapacity(1000);
        dto.setEnabled(0);
        dto.setStatus(DEFAULT_STATUS);
        dto.setTlsEnabled(true);

        SmppServer existingServer = new SmppServer();
        existingServer.setId(1);
        existingServer.setIp("127.0.0.1");
        existingServer.setName("tls server");
        existingServer.setPort(2776);
        existingServer.setEnabled(0);
        existingServer.setStatus(DEFAULT_STATUS);

        SmppServerConfig configWithTls = SmppServerConfig.builder().id(1).tlsEnabled(true).build();

        when(smppServerRepository.findById(1)).thenReturn(existingServer);
        when(smppServerRepository.save(existingServer)).thenReturn(existingServer);
        when(serverMapper.toRedisDTOFromEntity(existingServer)).thenReturn(configWithTls);
        when(serverMapper.toDTO(existingServer)).thenReturn(dto);

        ApiResponse response = smppServerService.updateSmppServer(1, dto);

        assertEquals(HttpStatus.OK.value(), response.status());
        verify(serverMapper).updateEntityFromDTO(dto, existingServer);
        verify(serverMapper).toRedisDTOFromEntity(existingServer);
        ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(utilsBase).storeInRedis(anyString(), eq("1"), redisValueCaptor.capture());
        assertTrue(redisValueCaptor.getValue().contains("\"tls_enabled\":true"));
    }

    private static Stream<Arguments> updateSmppServerStatusTestCases() {
        SmppServer smppServerStoppedForPreDestroy = new SmppServer();
        smppServerStoppedForPreDestroy.setId(1);
        smppServerStoppedForPreDestroy.setName("test smpp server pre destroy");
        smppServerStoppedForPreDestroy.setEnabled(1);
        smppServerStoppedForPreDestroy.setStatus(STARTED_STATUS);

        SmppServer smppServerFromStoppedToStarted = new SmppServer();
        smppServerFromStoppedToStarted.setId(2);
        smppServerFromStoppedToStarted.setName("test smpp server");
        smppServerFromStoppedToStarted.setEnabled(1);
        smppServerFromStoppedToStarted.setStatus(DEFAULT_STATUS);

        SmppServer smppServerForcedStop = new SmppServer();
        smppServerForcedStop.setId(3);
        smppServerForcedStop.setName("test smpp server forced stop");
        smppServerForcedStop.setEnabled(1);
        smppServerForcedStop.setStatus("FORCED_STOPPED_STATUS");


        return Stream.of(
                Arguments.of(smppServerStoppedForPreDestroy, DEFAULT_STATUS),
                Arguments.of(smppServerFromStoppedToStarted, STARTED_STATUS),
                Arguments.of(smppServerForcedStop, "FORCED_STOPPED_STATUS")
        );
    }
}