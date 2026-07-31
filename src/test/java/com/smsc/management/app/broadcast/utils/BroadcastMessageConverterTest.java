package com.smsc.management.app.broadcast.utils;

import com.paicbd.smsc.dto.MessageEvent;
import com.smsc.management.app.broadcast.dto.BroadcastParamsDTO;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BroadcastMessageConverterTest {

    private static BroadcastParamsDTO defaultParams() {
        return new BroadcastParamsDTO(1, 0, 1, 0, 0);
    }

    private static ServiceProvider buildSp() {
        ServiceProvider sp = new ServiceProvider();
        sp.setNetworkId(1);
        sp.setSystemId("NMB-SP");
        sp.setProtocol("HTTP");
        sp.setMessagePriority("MEDIUM");
        return sp;
    }

    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<BroadcastMessageConverter> constructor =
                BroadcastMessageConverter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void completeMessageEventSetsExpectedFieldsOnMessageEvent() {
        ServiceProvider sp = buildSp();
        MessageEvent event = new MessageEvent();
        String messageId = System.currentTimeMillis() + "-" + System.nanoTime();

        BroadcastMessageConverter.completeMessageEvent(event, "739769082", sp, true, messageId, defaultParams());

        assertEquals("NMB-SP", event.getSystemId());
        assertEquals(1, event.getOriginNetworkId());
        assertEquals("HTTP", event.getOriginProtocol());
        assertEquals("SP", event.getOriginNetworkType());
        assertEquals(0, event.getCommandStatus());
        assertEquals(1, event.getRegisteredDelivery());
        assertEquals("MEDIUM", event.getSmscMessagePriority());
        assertEquals(messageId, event.getMessageId());
        assertNull(event.getCustomParams()); // custom params are injected by the routing module, not here
    }
}
