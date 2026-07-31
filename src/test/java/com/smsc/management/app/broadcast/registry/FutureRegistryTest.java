package com.smsc.management.app.broadcast.registry;

import com.smsc.management.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FutureRegistryTest extends BaseIntegrationTest  {

    private FutureRegistry futureRegistry;
    private Future<?> mockProcessingFuture;

    @BeforeEach
    void setUp() {
        futureRegistry = new FutureRegistry();
        mockProcessingFuture = mock(Future.class);
    }

    @Test
    void testRegisterAndExists() {
        Integer broadcastId = 1;

        futureRegistry.register(broadcastId, mockProcessingFuture);

        assertTrue(futureRegistry.exists(broadcastId), "Expected future to exist after registration");
    }

    @Test
    void testCancelScenarios() {
        Integer broadcastId1 = 2;
        Future<?> processing1 = mock(Future.class);

        when(processing1.cancel(true)).thenReturn(true);

        futureRegistry.register(broadcastId1, processing1);
        boolean result1 = futureRegistry.cancel(broadcastId1);

        assertTrue(result1, "Expected cancel to return true when at least one task is cancelled");
        assertFalse(futureRegistry.exists(broadcastId1), "Expected task to be removed after cancellation");

        verify(processing1).cancel(true);
    }

    @Test
    void testCancelWhenNoTaskExists() {
        Integer nonExistingId = 99;

        boolean result = futureRegistry.cancel(nonExistingId);

        assertFalse(result, "Expected cancel to return false when no task exists");
    }

    @Test
    void testRemove() {
        Integer broadcastId = 3;

        futureRegistry.register(broadcastId, mockProcessingFuture);
        assertTrue(futureRegistry.exists(broadcastId));

        futureRegistry.remove(broadcastId);

        assertFalse(futureRegistry.exists(broadcastId), "Expected future to be removed after calling remove()");
    }
}
