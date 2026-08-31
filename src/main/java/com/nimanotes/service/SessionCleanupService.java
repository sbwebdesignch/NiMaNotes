package com.nimanotes.service;

import com.nimanotes.model.LoginSession;
import com.nimanotes.repository.LoginSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SessionCleanupService {

    private final LoginSessionRepository loginSessionRepository;
    private final LoginSessionService loginSessionService;

    public SessionCleanupService(LoginSessionRepository loginSessionRepository, LoginSessionService loginSessionService) {
        this.loginSessionRepository = loginSessionRepository;
        this.loginSessionService = loginSessionService;
    }

    public int purgeExpiredSessions(Clock clock, Duration ttl) {
        List<LoginSession> sessions = new ArrayList<>(loginSessionRepository.findAll());
        sessions.sort(Comparator.comparing(LoginSession::getCreatedAt));

        int deletedCount = 0;
        for (LoginSession session : sessions) {
            if (loginSessionService.isExpired(session, clock, ttl)) {
                try {
                    loginSessionRepository.delete(session);
                    deletedCount++;
                } catch (RuntimeException e) {
                    // Eine fehlgeschlagene Löschung darf die Bereinigung der übrigen
                    // abgelaufenen Sessions nicht abbrechen.
                }
            }
        }
        return deletedCount;
    }
}
