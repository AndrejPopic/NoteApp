package com.example.NoteApp.listeners;

import com.example.NoteApp.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
