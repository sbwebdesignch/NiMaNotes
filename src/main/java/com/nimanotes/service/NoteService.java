package com.nimanotes.service;

import com.nimanotes.model.Note;
import com.nimanotes.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private static final int DEFAULT_LIMIT = 5;

    public boolean canCreateNote(User user, List<Note> currentNotes, boolean isPremium) {
        if (user == null) return false;
        if (currentNotes == null) return true;
        if (isPremium) return true;
        return currentNotes.size() < DEFAULT_LIMIT;
    }
}
