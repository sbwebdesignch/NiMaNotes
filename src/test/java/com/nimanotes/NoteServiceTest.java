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
        User user = new User("u", "p");
        List<Note> notes = List.of(new Note(), new Note()); // 2 von 5
        assertThat(noteService.canCreateNote(user, notes, false)).isTrue();
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

    // ---- Sammlungen & Listen ----

    @Test
    void collectionsFilterByKeyword() {
        User user = new User("u", "p");
        Note a = new Note("hello", "world", user);
        Note b = new Note("foo", "bar", user);
        List<Note> filtered = noteService.filterByKeyword(List.of(a, b), "hello");
        assertThat(filtered).hasSize(1).contains(a);
    }

    @Test
    void collectionsFilterByKeywordNoMatchReturnsEmptyList() {
        User user = new User("u", "p");
        Note a = new Note("hello", "world", user);
        List<Note> filtered = noteService.filterByKeyword(List.of(a), "xyz");
        assertThat(filtered).isEmpty();
    }

    @Test
    void collectionsFilterByKeywordNullInputsReturnEmptyList() {
        assertThat(noteService.filterByKeyword(null, "x")).isEmpty();
        User user = new User("u", "p");
        assertThat(noteService.filterByKeyword(List.of(new Note("a", "b", user)), null)).isEmpty();
    }
}
