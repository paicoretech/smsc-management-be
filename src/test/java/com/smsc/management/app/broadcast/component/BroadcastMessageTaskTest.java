package com.smsc.management.app.broadcast.component;

import com.paicbd.smsc.utils.BroadcastMessageStatus;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import com.smsc.management.app.broadcast.model.entity.BroadcastDevices;
import com.smsc.management.app.broadcast.model.repository.BroadcastDevicesRepository;
import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.app.broadcast.utils.BroadcastStatus;
import com.smsc.management.app.broadcast.utilsTest.Utils;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.utils.AppProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.kafka.support.SendResult;

import static com.paicbd.smsc.kafka.KafkaTopicsConstants.PRE_MESSAGE_MEDIUM_TOPIC;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_COLUMN_MAPPING;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE;
import static com.smsc.management.app.broadcast.utils.HelperBroadcastQuery.BROADCAST_MESSAGE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastMessageTaskTest {
    @Mock
    private BroadcastDevicesRepository broadcastDevicesRepository;

    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private BroadcastQueryExecutor broadcastQueryExecutor;

    @InjectMocks
    private BroadcastMessageTask broadcastMessageTask;

    @Mock
    private AppProperties appProperties;

    private void mockCommonAppProps() {
        when(appProperties.getBroadcastValidNumberRegex()).thenReturn("^\\+?\\d+$");
    }

    @SuppressWarnings("unchecked")
    private void mockKafkaSendOk() {
        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(future);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void processBroadcastAsync(boolean ok) {
        mockCommonAppProps();

        BroadcastDevices device = Utils.getBroadcastDevices();
        List<Map<String, Object>> devicesMap = new ArrayList<>();
        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(BROADCAST_MESSAGE_ID, device.getMessageId());
        deviceMap.put(BROADCAST_MESSAGE, device.getMessage());
        deviceMap.put(BROADCAST_COLUMN_MAPPING, device.getColumnMappingDataStr());
        devicesMap.add(deviceMap);

        if (ok) {
            mockKafkaSendOk();
            when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap()))
                    .thenReturn(devicesMap, new ArrayList<>());


            Assertions.assertDoesNotThrow(() ->
                    broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), Utils.getBroadcast(true)));
            verify(kafkaTemplate, atLeastOnce()).send(eq(PRE_MESSAGE_MEDIUM_TOPIC), anyString());

            reset(kafkaTemplate, broadcastDevicesRepository);
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("error to test"));

            when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap())).thenReturn(devicesMap, new ArrayList<>());

            Assertions.assertDoesNotThrow(() ->
                    broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), Utils.getBroadcast(true)));

            reset(broadcastDevicesRepository);
            when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap())).thenReturn(new ArrayList<>());

            Assertions.assertDoesNotThrow(() ->
                    broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), Utils.getBroadcast(true)));

            reset(broadcastDevicesRepository);
            AtomicInteger calls = new AtomicInteger(0);
            when(broadcastDevicesRepository.findByBroadcastIdAndStatus(anyInt(), anyInt(), any(Pageable.class)))
                    .thenAnswer(inv -> switch (calls.getAndIncrement()) {
                        case 0 -> devicesMap;
                        case 1 -> devicesMap;
                        default -> new ArrayList<>();
                    });

            Assertions.assertDoesNotThrow(() ->
                    broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), Utils.getBroadcast(true)));

        } else {
            when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap())).thenReturn(new ArrayList<>());

            Assertions.assertDoesNotThrow(() ->
                    broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), Utils.getBroadcast(false)));

            verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        }
    }

    @Test
    void processBroadcastAsyncWhenProcessAllBatchesThrowsExceptionThenCatchIsCovered() {
       Broadcast broadcast = Utils.getBroadcast(true);

        when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Simulated failure in processAllBatches"));

        Assertions.assertDoesNotThrow(() ->
                broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), broadcast)
        );
    }

    @Test
    void processBroadcastStopsOnMaxExecutionTimeReached() {
        BroadcastDevices device = Utils.getBroadcastDevices();
        List<Map<String, Object>> devicesMap = new ArrayList<>();
        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(BROADCAST_MESSAGE_ID, device.getMessageId());
        deviceMap.put(BROADCAST_MESSAGE, device.getMessage());
        deviceMap.put(BROADCAST_COLUMN_MAPPING, device.getColumnMappingDataStr());
        devicesMap.add(deviceMap);

        Broadcast expiredBroadcast = Utils.getBroadcast(true);
        expiredBroadcast.setMaxExecutionDateTime(LocalDateTime.now().minusMinutes(5)); // simulate expired

        when(appProperties.getBroadcastValidNumberRegex()).thenReturn("^\\+?\\d+$");
        when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap()))
                .thenReturn(devicesMap);

        Assertions.assertDoesNotThrow(() ->
                broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), expiredBroadcast));

        verify(broadcastQueryExecutor, atLeastOnce()).massiveUpdateForStoppingBroadcast(anyInt(), any(BroadcastMessageStatus.class));
    }

    @Test
    void processBroadcastAsyncWhenThreadIsInterruptedThenBreaksExecution() {
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setMaxExecutionDateTime(LocalDateTime.now().plusMinutes(10));

        Thread.currentThread().interrupt();

        Assertions.assertDoesNotThrow(() ->
                broadcastMessageTask.processBroadcastAsync(1, getServiceProvider(), broadcast)
        );

        Thread.interrupted();
    }

    private ServiceProvider getServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setNetworkId(1);
        serviceProvider.setSystemId("broadcastSP");
        serviceProvider.setAddressNpi(0);
        serviceProvider.setAddressTon(0);
        serviceProvider.setProtocol("HTTP");

        return serviceProvider;
    }

    @Test
    void updateMessageTemplateAsyncCoversSuccessAndErrorCases() {
        Broadcast broadcast = Utils.getBroadcast(true);
        broadcast.setMessageTemplate("Sr. {{name}} your t-shirt is cost {{cost}}");

        BroadcastDevices device = Utils.getBroadcastDevices();
        device.setColumnMappingDataStr("{\"name\":\"John\",\"cost\":\"20\"}");
        device.setMessage("old_message");

        List<Map<String, Object>> devicesMap = new ArrayList<>();
        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(BROADCAST_MESSAGE_ID, device.getMessageId());
        deviceMap.put(BROADCAST_MESSAGE, device.getMessage());
        deviceMap.put(BROADCAST_COLUMN_MAPPING, device.getColumnMappingDataStr());
        devicesMap.add(deviceMap);

        when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap()))
                .thenReturn(devicesMap, new ArrayList<>());


        broadcastMessageTask.updateMessageTemplateAsync(10, broadcast);

        ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
        verify(broadcastQueryExecutor, atLeastOnce()).executeUpdateBroadcastMessageTemplate(captor.capture(), anyInt());

        List<Map<String, Object>> capturedList = captor.getAllValues().get(0);
        Map<String, Object> savedDevice = capturedList.get(0);

        assertEquals(
                "Sr. John your t-shirt is cost 20",
                savedDevice.get(BROADCAST_MESSAGE),
                "Expected updated messageTemplate in BroadcastDevices"
        );

        verify(broadcastRepository).changeStatus(eq(1), eq(BroadcastStatus.PENDING), any(LocalDateTime.class), contains("Message template updated"));

        // Simulate repository error and expect FAILED status
        reset(broadcastRepository, broadcastDevicesRepository);

        when(broadcastQueryExecutor.executeQueryAndGetListMap(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Simulated failure for test"));

        broadcastMessageTask.updateMessageTemplateAsync(10, broadcast);

        verify(broadcastRepository).changeStatus(
                eq(1),
                eq(BroadcastStatus.FAILED),
                any(LocalDateTime.class),
                contains("Error during message template update"));

        // Simulate thread interruption and expect CANCELED status
        reset(broadcastRepository, broadcastDevicesRepository);
        Thread.currentThread().interrupt();

        broadcastMessageTask.updateMessageTemplateAsync(10, broadcast);
        verify(broadcastRepository).changeStatus(
                eq(1),
                eq(BroadcastStatus.CANCELED),
                any(LocalDateTime.class),
                contains("Canceled during message template update")
        );
    }

}