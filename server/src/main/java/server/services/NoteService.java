package server.services;

import models.Collection;
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

    List<Collection> getAllCollections();

    Collection addCollection(Collection collection);

    Optional<Collection> getCollectionById(long id);

    List<Note> getNotesByCollectionId(long id);

    List<Collection> getCollectionsByNoteId(long id);
}
