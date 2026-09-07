package com.nimanotes;

import com.nimanotes.model.LoginSession;
import com.nimanotes.model.User;
import com.nimanotes.service.LoginSessionService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

// Basis: Time-Freezing via fixem Clock statt Instant.now().
class LoginSessionServiceTest {

    private final LoginSessionService service = new LoginSessionService();

    @Test
    void timeFreezingExpiry() {
        User user = new User("u", "p");
        LoginSession session = new LoginSession("token", user);
        session.setCreatedAt(Instant.parse("2023-01-01T00:00:00Z"));
        Clock later = Clock.fixed(Instant.parse("2023-01-01T01:01:00Z"), ZoneOffset.UTC);
        boolean expired = service.isExpired(session, later, Duration.ofMinutes(60));
        assertThat(expired).isTrue();
    }
}
