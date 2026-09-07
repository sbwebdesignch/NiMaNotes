package com.nimanotes;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import com.nimanotes.service.NoteService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoteServiceTest {

    private final NoteService noteService = new NoteService();

    // ---- Kombinatorische Logik: canCreateNote(user, notes, premium) ----

    @Test
    void nonPremiumUnderLimitCanCreate() {
        User user = new User("u", "p"); // Arrange: Vorbereitung
        List<Note> notes = List.of(new Note(), new Note()); // Testdaten aufbauen
        assertThat(noteService.canCreateNote(user, notes, false)).isTrue(); // Act & Assert: Aufruf der Methode und
                                                                            // Überprüfung des Ergebnisses
    }

    @Test
    void nonPremiumAtLimitCannotCreate() {
        User user = new User("u", "p");
        List<Note> notes = List.of(new Note(), new Note(), new Note(), new Note(), new Note());
        assertThat(noteService.canCreateNote(user, notes, false)).isFalse();
    }

    @Test
    void premiumIgnoresLimit() {
        User user = new User("u", "p");
        List<Note> notes = List.of(new Note(), new Note(), new Note(), new Note(), new Note());
        assertThat(noteService.canCreateNote(user, notes, true)).isTrue();
    }

    @Test
    void nullNotesListIsAllowed() {
        User user = new User("u", "p");
        assertThat(noteService.canCreateNote(user, null, false)).isTrue();
    }

    @Test
    void nullUserCannotCreate() {
        List<Note> notes = List.of(new Note());
        assertThat(noteService.canCreateNote(null, notes, false)).isFalse();
        assertThat(noteService.canCreateNote(null, notes, true)).isFalse();
    }
}
