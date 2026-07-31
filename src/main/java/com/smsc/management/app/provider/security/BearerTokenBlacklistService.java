package com.smsc.management.app.provider.security;

import com.paicbd.smsc.scylla.ScyllaManager;
import com.smsc.management.utils.Constants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
public class BearerTokenBlacklistService {

    private final ScyllaManager scyllaManager;
    private final Clock clock;

    @PostConstruct
    public void init() {
        try {
            scyllaManager.createBearerTokenBlacklistTable();
            log.info("Bearer token blacklist table initialized");
        } catch (Exception e) {
            log.error("Failed to initialize bearer token blacklist table: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void addToBlacklist(String jti, long expiresAtEpochSeconds) {
        if (!StringUtils.hasText(jti)) {
            log.warn("Attempted to blacklist a blank JTI — skipping");
            return;
        }

        long now = clock.instant().getEpochSecond();
        long remainingSeconds = expiresAtEpochSeconds - now;

        if (remainingSeconds <= 0) {
            log.debug("Old Bearer token JTI {} is already expired — skipping blacklist insertion", jti);
            return;
        }

        long ttl = remainingSeconds + Constants.BLACKLIST_TTL_BUFFER_SECONDS;

        try {
            scyllaManager.insertIntoBearerTokenBlacklist(jti, (int) ttl);
            log.info("Blacklisted Bearer token JTI {} with TTL {} seconds", jti, ttl);
        } catch (Exception e) {
            log.error("Failed to blacklist JTI {}: {}", jti, e.getMessage(), e);
            throw e;
        }
    }
}
