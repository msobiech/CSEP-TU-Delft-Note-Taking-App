package server.services;

import models.Collection;
import models.Note;

import java.util.List;
import java.util.Optional;

public interface NoteService {

    /**
     * Gets all notes
     * @return all notes present in the database
     */
    List<Note> getAllNotes();

    /**
     * Gets note by id
     * @param id to search note by
     * @return The note with given id or Optional empty if the note couldn't be found
     */
    Optional<Note> getNoteById(long id);

    /**
     * Checks if the note with given id exists
     * @param id to search by
     * @return true if note exists, false otherwise
     */
    boolean noteExists(long id);

    /**
     * Save note in the database
     * @param note to save
     * @return saved note
     */
    Note saveNote(Note note);

    /**
     * Search note by given keyword
     * @param keyword to search by
     * @return the list of notes that match the keyword
     */
    List<Note> searchNotesByKeyword(String keyword);

    /**
     * Gets the notes Ids and Titles from the database
     * @return List of Notes' Ids and Titles
     */
    List<Object[]> getNotesIdAndTitle();

    /**
     * Update Note in the database
     * @param id id of the note to update
     * @param note new note to update to
     * @return Update version of the note
     * @throws IllegalAccessException Exception that is caught during the execution
     */
    Note updateNote(long id, Note note) throws IllegalAccessException;

    /**
     * Search notes by keywords
     * @param keyword to match
     * @return list of notes that match the keyword
     */
    List<Object[]> searchNotes(String keyword);


    /**
     * Delete note with given id
     * @param id of note to delete
     * @throws IllegalAccessException Exception that is caught during the execution
     */
    void deleteNote(long id) throws IllegalAccessException;

    /**
     * Generates unique note Title
     * @return New unique title
     */
    String generateUniqueTitle();

    /**
     * Checks if a note title exists in the database.
     * @param title to check for.
     * @return true if the title exists, false otherwise.
     */
    boolean titleExists(String title);

    /**
     * Gets all collections from the database
     * @return The list of all collections
     */
    List<Collection> getAllCollections();

    /**
     * Adds collection to the database
     * @param collection collection to add
     * @return Added collection
     */
    Collection addCollection(Collection collection);

    /**
     * Gets collection by given id
     * @param id to search by
     * @return Found collection or empty Optional if collection couldn't be found
     */
    Optional<Collection> getCollectionById(long id);

    /**
     * Gets Notes connected to collection with given id
     * @param id of collection to search by
     * @return List of notes that are linked to the given collection
     */
    List<Note> getNotesByCollectionId(long id);

    /**
     * Gets Collections connected to note with given id
     * @param id of note to search by
     * @return List of Collections that are linked to the given note
     */
    List<Collection> getCollectionsByNoteId(long id);
}
