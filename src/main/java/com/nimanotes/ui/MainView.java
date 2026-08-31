package com.nimanotes.ui;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import com.nimanotes.repository.NoteRepository;
import com.nimanotes.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Route("")
@PermitAll
public class MainView extends VerticalLayout {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    private final TextField titleField = new TextField("Titel");
    private final TextArea contentArea = new TextArea("Notiz");
    private final Button saveButton = new Button("Speichern");
    private final Button clearButton = new Button("Neu");
    private final Button deleteButton = new Button("Löschen");
    private final Grid<Note> noteGrid = new Grid<>(Note.class, false);

    private Long selectedNoteId;

    public MainView(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;

        addClassName("app-view");
        setPadding(false);
        setSpacing(true);
        setAlignItems(Alignment.STRETCH);

        H2 title = new H2("NiMaNotes");
        HorizontalLayout header = new HorizontalLayout(title);
        header.addClassName("app-header");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        noteGrid.addColumn(Note::getTitle).setHeader("Titel");
        noteGrid.addColumn(Note::getContent).setHeader("Inhalt");
        noteGrid.setHeight("300px");
        noteGrid.asSingleSelect().addValueChangeListener(event -> {
            Note selected = event.getValue();
            if (selected != null) {
                selectedNoteId = selected.getId();
                titleField.setValue(selected.getTitle());
                contentArea.setValue(selected.getContent());
            }
        });

        saveButton.addClickListener(event -> saveNote());
        clearButton.addClickListener(event -> clearForm());
        deleteButton.addClickListener(event -> deleteSelectedNote());

        HorizontalLayout toolbar = new HorizontalLayout(saveButton, clearButton, deleteButton);
        toolbar.addClassName("app-toolbar");

        VerticalLayout card = new VerticalLayout(titleField, contentArea, toolbar, noteGrid);
        card.addClassName("notes-card");
        card.setPadding(false);
        card.setSpacing(true);

        add(header, card);
        loadNotes();
    }

    private void saveNote() {
        User user = currentUser();

        if (titleField.getValue().isBlank() || contentArea.getValue().isBlank()) {
            return;
        }

        if (selectedNoteId != null) {
            Note note = noteRepository.findById(selectedNoteId).orElse(null);
            if (note != null && note.getUser().getId().equals(user.getId())) {
                note.setTitle(titleField.getValue());
                note.setContent(contentArea.getValue());
                noteRepository.save(note);
            }
        } else {
            Note note = new Note(titleField.getValue(), contentArea.getValue(), user);
            noteRepository.save(note);
        }

        clearForm();
        loadNotes();
    }

    private void deleteSelectedNote() {
        if (selectedNoteId == null) {
            return;
        }

        User user = currentUser();
        Note note = noteRepository.findById(selectedNoteId).orElse(null);
        if (note != null && note.getUser().getId().equals(user.getId())) {
            noteRepository.delete(note);
        }

        clearForm();
        loadNotes();
    }

    private void clearForm() {
        selectedNoteId = null;
        titleField.clear();
        contentArea.clear();
    }

    private void loadNotes() {
        User user = currentUser();
        List<Note> notes = noteRepository.findByUser(user);
        noteGrid.setItems(notes);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
    }
}
