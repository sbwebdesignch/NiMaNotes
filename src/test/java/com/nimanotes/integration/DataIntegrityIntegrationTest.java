package com.nimanotes.integration;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import com.nimanotes.repository.NoteRepository;
import com.nimanotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integrationstests fuer den Schwerpunkt "Datenintegritaet" (LB2).
 *
 * Geprueft werden die in den JPA-Entities definierten Constraints (unique,
 * not-null, Fremdschluessel) tatsaechlich gegen eine echte Datenbank
 * (H2 im MariaDB-Kompatibilitaetsmodus, siehe src/test/resources/application.properties),
 * sowie die verlustfreie Speicherung/Abfrage von Daten (T-011 bis T-015).
 *
 * Hinweis: Sobald eine DB-Operation innerhalb einer Testmethode eine
 * Constraint-Verletzung ausloest, darf laut Hibernate dieselbe Session
 * danach nicht mehr fuer weitere Abfragen verwendet werden. Die Tests
 * pruefen daher direkt, dass die erwartete Exception geworfen wird, ohne
 * die (dann ungueltige) Session anschliessend weiter zu benutzen.
 */
@DataJpaTest
class DataIntegrityIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    // T-011: Unique-Constraint auf User.username wird von der Datenbank durchgesetzt
    @Test
    void duplicateUsernameViolatesUniqueConstraint() {
        userRepository.saveAndFlush(new User("gleicherName", "passwort1"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User("gleicherName", "passwort2")))
                .isInstanceOf(RuntimeException.class);
    }

    // T-012: Not-Null-Constraint auf Note.user (Fremdschluessel) wird durchgesetzt
    @Test
    void noteWithoutUserViolatesNotNullConstraint() {
        Note noteOhneUser = new Note("Titel", "Inhalt ohne Besitzer", null);

        assertThatThrownBy(() -> noteRepository.saveAndFlush(noteOhneUser))
                .isInstanceOf(RuntimeException.class);
    }

    // T-013: Referentielle Integritaet - ein User mit vorhandenen Notizen kann nicht geloescht werden
    @Test
    void cannotDeleteUserWhoStillHasNotes() {
        User user = userRepository.saveAndFlush(new User("besitzer", "passwort"));
        noteRepository.saveAndFlush(new Note("Notiz", "Inhalt", user));

        assertThatThrownBy(() -> {
            userRepository.delete(user);
            userRepository.flush();
        }).isInstanceOf(RuntimeException.class);
    }

    // T-014: Not-Null-Constraint auf User.password wird durchgesetzt
    @Test
    void userWithoutPasswordViolatesNotNullConstraint() {
        User userOhnePasswort = new User("ohnepasswort", null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(userOhnePasswort))
                .isInstanceOf(RuntimeException.class);
    }

    // T-015: Daten werden unveraendert (verlustfrei) gespeichert und wieder ausgelesen
    @Test
    void dataRoundTripPreservesContentExactly() {
        User user = userRepository.saveAndFlush(new User("roundtrip", "passwort"));

        String titelMitSonderzeichen = "Ünïcödé-Tïtel äöü 日本語 😀";
        String inhalt2000Zeichen = "x".repeat(2000); // Grenzwert von Note.content (length = 2000)

        Note note = noteRepository.saveAndFlush(new Note(titelMitSonderzeichen, inhalt2000Zeichen, user));

        Note geladeneNote = noteRepository.findById(note.getId()).orElseThrow();

        assertThat(geladeneNote.getTitle()).isEqualTo(titelMitSonderzeichen);
        assertThat(geladeneNote.getContent()).isEqualTo(inhalt2000Zeichen);
        assertThat(geladeneNote.getContent()).hasSize(2000);
    }
}
