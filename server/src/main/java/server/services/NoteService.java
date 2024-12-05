package server.services;

import models.Note;

import java.util.List;
import java.util.Optional;

public interface NoteService {
    List<Note> getAllNotes();

    Optional<Note> getNoteById(long id);

    boolean noteExists(long id);

    Note saveNote(Note note);

    List<Note> searchNotesByKeyword(String keyword);

    List<Object[]> getNotesIdAndTitle();

    Note updateNote(long id, Note note) throws IllegalAccessException;

    List<Note> searchNotes(String keyword);

    void deleteNote(long id) throws IllegalAccessException;

    String generateUniqueTitle();
}
