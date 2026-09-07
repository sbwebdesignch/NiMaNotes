package com.nimanotes;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import com.nimanotes.repository.NoteRepository;
import com.nimanotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Ergänzender Integrationstes (kein Whitebox-Kategorietest).
@DataJpaTest
class NoteRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void saveAndFindNotesForUser() {
        User user = new User("testuser", "secret");
        userRepository.save(user);

        Note note = new Note("Erste Notiz", "Hallo Welt", user);
        noteRepository.save(note);

        List<Note> notes = noteRepository.findByUser(user);

        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getTitle()).isEqualTo("Erste Notiz");
        assertThat(notes.get(0).getContent()).isEqualTo("Hallo Welt");
    }

    @Test
    void registerUserAndLoadByUsername() {
        User user = new User("newuser", "pw123");
        userRepository.save(user);

        User found = userRepository.findByUsername("newuser").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("newuser");
    }
}
