package com.nimanotes;

import com.nimanotes.model.LoginSession;
import com.nimanotes.model.User;
import com.nimanotes.repository.LoginSessionRepository;
import com.nimanotes.service.LoginSessionService;
import com.nimanotes.service.SessionCleanupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Faktor 3: kombiniert Time-Freezing (fixer Clock)
 */
@ExtendWith(MockitoExtension.class)
class SessionCleanupServiceTest {

    @Mock
    private LoginSessionRepository loginSessionRepository;

    private final LoginSessionService loginSessionService = new LoginSessionService();

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
    private final Duration ttl = Duration.ofMinutes(30);

    @Test
    void deletesOnlyExpiredSessionsOldestFirst() {
        User user = new User("u", "p");
        LoginSession veryExpired = session(user, "2026-01-01T10:00:00Z"); // 2h alt -> abgelaufen
        LoginSession justExpired = session(user, "2026-01-01T11:20:00Z"); // 40min alt -> abgelaufen
        LoginSession stillValid = session(user, "2026-01-01T11:50:00Z"); // 10min alt -> gültig

        when(loginSessionRepository.findAll()).thenReturn(List.of(stillValid, veryExpired, justExpired));

        SessionCleanupService service = new SessionCleanupService(loginSessionRepository, loginSessionService);
        int deleted = service.purgeExpiredSessions(fixedClock, ttl);

        assertThat(deleted).isEqualTo(2);
        verify(loginSessionRepository, never()).delete(stillValid);

        InOrder inOrder = inOrder(loginSessionRepository);
        inOrder.verify(loginSessionRepository).delete(veryExpired);
        inOrder.verify(loginSessionRepository).delete(justExpired);
    }

    @Test
    void noSessionsAreDeletedWhenAllWithinTtl() {
        User user = new User("u", "p");
        LoginSession fresh = session(user, "2026-01-01T11:55:00Z");
        when(loginSessionRepository.findAll()).thenReturn(List.of(fresh));

        SessionCleanupService service = new SessionCleanupService(loginSessionRepository, loginSessionService);
        int deleted = service.purgeExpiredSessions(fixedClock, ttl);

        assertThat(deleted).isZero();
        verify(loginSessionRepository, never()).delete(any());
    }

    @Test
    void continuesCleanupWhenDeletionOfOneSessionFails() {
        User user = new User("u", "p");
        LoginSession broken = session(user, "2026-01-01T09:00:00Z");
        LoginSession normal = session(user, "2026-01-01T09:30:00Z");
        when(loginSessionRepository.findAll()).thenReturn(List.of(broken, normal));

        doThrow(new RuntimeException("DB constraint violation")).when(loginSessionRepository).delete(broken);

        SessionCleanupService service = new SessionCleanupService(loginSessionRepository, loginSessionService);
        int deleted = service.purgeExpiredSessions(fixedClock, ttl);

        assertThat(deleted).isEqualTo(1);
        verify(loginSessionRepository).delete(normal);
    }

    private LoginSession session(User user, String createdAt) {
        LoginSession session = new LoginSession("token", user);
        session.setCreatedAt(Instant.parse(createdAt));
        return session;
    }
}
