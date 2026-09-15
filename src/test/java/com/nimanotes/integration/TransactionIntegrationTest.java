package com.nimanotes.integration;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import com.nimanotes.repository.NoteRepository;
import com.nimanotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integrationstests fuer den Schwerpunkt "Transaktionspruefung / CRUD" (LB2).
 *
 * Deckt Create, Read, Update, Delete gegen die echte Datenbank ab sowie die
 * Atomicity-Eigenschaft aus ACID: Schlaegt eine Operation innerhalb einer
 * Transaktion fehl, duerfen keine Teilaenderungen dauerhaft gespeichert
 * werden (T-021 bis T-025).
 */
@DataJpaTest
class TransactionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // T-021: Create - eine neue Notiz kann angelegt und wiedergefunden werden
    @Test
    void createPersistsNewNote() {
        User user = userRepository.saveAndFlush(new User("create-user", "pw"));

        Note gespeichert = noteRepository.saveAndFlush(new Note("Einkaufsliste", "Milch, Brot", user));

        assertThat(gespeichert.getId()).isNotNull();
        assertThat(noteRepository.findById(gespeichert.getId())).isPresent();
    }

    // T-022: Read - eine vorhandene Notiz kann per ID gelesen werden
    @Test
    void readReturnsPersistedNoteById() {
        User user = userRepository.saveAndFlush(new User("read-user", "pw"));
        Note gespeichert = noteRepository.saveAndFlush(new Note("Notiz A", "Inhalt A", user));

        Optional<Note> gefunden = noteRepository.findById(gespeichert.getId());

        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getTitle()).isEqualTo("Notiz A");
        assertThat(gefunden.get().getContent()).isEqualTo("Inhalt A");
    }

    // T-023: Update - Aenderungen an einer Notiz werden dauerhaft gespeichert
    @Test
    void updateChangesArePersisted() {
        User user = userRepository.saveAndFlush(new User("update-user", "pw"));
        Note gespeichert = noteRepository.saveAndFlush(new Note("Alter Titel", "Alter Inhalt", user));

        gespeichert.setTitle("Neuer Titel");
        gespeichert.setContent("Neuer Inhalt");
        noteRepository.saveAndFlush(gespeichert);

        Note neuGeladen = noteRepository.findById(gespeichert.getId()).orElseThrow();
        assertThat(neuGeladen.getTitle()).isEqualTo("Neuer Titel");
        assertThat(neuGeladen.getContent()).isEqualTo("Neuer Inhalt");
    }

    // T-024: Delete - eine geloeschte Notiz ist danach nicht mehr auffindbar
    @Test
    void deleteRemovesNoteFromDatabase() {
        User user = userRepository.saveAndFlush(new User("delete-user", "pw"));
        Note gespeichert = noteRepository.saveAndFlush(new Note("Zu loeschen", "Inhalt", user));
        Long id = gespeichert.getId();

        noteRepository.delete(gespeichert);
        noteRepository.flush();

        assertThat(noteRepository.findById(id)).isEmpty();
    }

    // T-025: Atomicity - schlaegt eine Operation innerhalb einer Transaktion fehl,
    // werden alle vorherigen Aenderungen dieser Transaktion zurueckgerollt.
    @Test
    void failedOperationRollsBackWholeTransaction() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            // Erste Operation ist fuer sich genommen gueltig ...
            userRepository.saveAndFlush(new User("atomarer-nutzer", "pw1"));
            // ... die zweite verletzt den Unique-Constraint auf "username" und
            // muss die gesamte Transaktion scheitern lassen.
            userRepository.saveAndFlush(new User("atomarer-nutzer", "pw2"));
            return null;
        })).isInstanceOf(RuntimeException.class);

        // In einer neuen, unabhaengigen Pruefung darf weder der erste noch der
        // zweite User vorhanden sein - die Transaktion wurde vollstaendig zurueckgerollt.
        List<User> gefundene = userRepository.findAll().stream()
                .filter(u -> "atomarer-nutzer".equals(u.getUsername()))
                .toList();
        assertThat(gefundene).isEmpty();
    }
}
