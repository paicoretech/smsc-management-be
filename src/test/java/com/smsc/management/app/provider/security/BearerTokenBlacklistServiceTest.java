package com.smsc.management.app.provider.security;

import com.paicbd.smsc.scylla.ScyllaManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BearerTokenBlacklistServiceTest {

    private ScyllaManager scyllaManager;
    private Clock clock;
    private BearerTokenBlacklistService service;

    private static final long NOW_EPOCH = 1_700_000_000L;

    @BeforeEach
    void setUp() {
        scyllaManager = mock(ScyllaManager.class);
        clock = Clock.fixed(Instant.ofEpochSecond(NOW_EPOCH), ZoneOffset.UTC);
        service = new BearerTokenBlacklistService(scyllaManager, clock);
        service.init();
    }

    @Test
    void init_shouldCreateBlacklistTable() {
        verify(scyllaManager).createBearerTokenBlacklistTable();
    }

    @Test
    void init_shouldPropagateException_whenTableCreationFails() {
        ScyllaManager failingManager = mock(ScyllaManager.class);
        doThrow(new RuntimeException("Cannot create table"))
                .when(failingManager).createBearerTokenBlacklistTable();

        BearerTokenBlacklistService failingService =
                new BearerTokenBlacklistService(failingManager, clock);

        assertThrows(RuntimeException.class, failingService::init);
    }

    @Test
    void addToBlacklist_shouldInsertWithCorrectTtl_whenTokenIsStillValid() {
        long expiresAt = NOW_EPOCH + 300L; // TTL = 310

        service.addToBlacklist("jti-abc", expiresAt);

        verify(scyllaManager).insertIntoBearerTokenBlacklist("jti-abc", 310);
    }

    @Test
    void addToBlacklist_shouldSkip_whenTokenIsAlreadyExpired() {
        long expiresAt = NOW_EPOCH - 1L;

        service.addToBlacklist("jti-expired", expiresAt);

        verify(scyllaManager, never()).insertIntoBearerTokenBlacklist(anyString(), anyInt());
    }

    @Test
    void addToBlacklist_shouldSkip_whenTokenExpiresExactlyNow() {
        long expiresAt = NOW_EPOCH;

        service.addToBlacklist("jti-boundary", expiresAt);

        verify(scyllaManager, never()).insertIntoBearerTokenBlacklist(anyString(), anyInt());
    }

    @Test
    void addToBlacklist_shouldSkip_whenJtiIsBlank() {
        service.addToBlacklist("   ", NOW_EPOCH + 300L);
        service.addToBlacklist("", NOW_EPOCH + 300L);
        service.addToBlacklist(null, NOW_EPOCH + 300L);

        verify(scyllaManager, never()).insertIntoBearerTokenBlacklist(anyString(), anyInt());
    }

    @Test
    void addToBlacklist_shouldPropagateException_whenScyllaFails() {
        long expiresAt = NOW_EPOCH + 300L;
        doThrow(new RuntimeException("Scylla unavailable"))
                .when(scyllaManager).insertIntoBearerTokenBlacklist(anyString(), anyInt());

        assertThrows(RuntimeException.class, () -> service.addToBlacklist("jti-fail", expiresAt));
    }

    @Test
    void addToBlacklist_withMinimalRemainingTime_shouldUseTtlOfEleven() {
        long expiresAt = NOW_EPOCH + 1L; // TTL = 11

        assertDoesNotThrow(() -> service.addToBlacklist("jti-minimal", expiresAt));

        verify(scyllaManager).insertIntoBearerTokenBlacklist("jti-minimal", 11);
    }
}
