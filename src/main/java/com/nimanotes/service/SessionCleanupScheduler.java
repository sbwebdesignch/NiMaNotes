package com.nimanotes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
public class SessionCleanupScheduler {

    private static final Duration SESSION_TTL = Duration.ofMinutes(60);

    private final SessionCleanupService sessionCleanupService;

    public SessionCleanupScheduler(SessionCleanupService sessionCleanupService) {
        this.sessionCleanupService = sessionCleanupService;
    }

    @Scheduled(fixedRate = 15, timeUnit = java.util.concurrent.TimeUnit.MINUTES, initialDelay = 1)
    public void cleanupExpiredSessions() {
        sessionCleanupService.purgeExpiredSessions(Clock.systemUTC(), SESSION_TTL);
    }
}
