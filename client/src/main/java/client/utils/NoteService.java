package client.utils;

import models.Note;

public class NoteService {

    ServerUtils server = new ServerUtils();

    public void updateNoteTitle(String newTitle, Long curNoteId) {
        Note updatedNote = new Note();
        updatedNote.setTitle(newTitle);
        server.updateNoteByID(curNoteId, updatedNote);
    }

    public void updateNoteContent(String newContent, Long curNoteId) {
        Note updatedNote = new Note();
        updatedNote.setContent(newContent);
        server.updateNoteByID(curNoteId, updatedNote);
    }

    public void setServerURL(String url){
        server.setServerURL(url);
    }

    /**
     * Checks if the given title is a duplicate by calling the server.
     *
     * @param title the title to check.
     * @return true if the title exists, false otherwise.
     */
    public boolean titleExists(String title) {
        return server.titleExists(title);
    }

    /**
     * Requests a new unique title from the server.
     *
     * @return a unique title.
     */
    public String generateUniqueTitle() {
        try {
            return server.generateUniqueTitle();
        } catch (Exception e) {
            System.err.println("Error generating unique title: " + e.getMessage());
            return "Untitled Note"; // Fallback if the server call fails
        }
    }

    /**
     * Fetches the current title of the note by its ID.
     *
     * @param noteId the ID of the note.
     * @return the current title of the note.
     */
    public String getNoteTitle(Long noteId) {
        try {
            Note note = server.getNoteByID(noteId);
            return note.getTitle();
        } catch (Exception e) {
            System.err.println("Error fetching note title: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches the current content of the note by its ID.
     *
     * @param noteId the ID of the note.
     * @return the current content of the note.
     */
    public String getNoteContent(Long noteId) {
        try {
            Note note = server.getNoteByID(noteId);
            return note.getContent();
        } catch (Exception e) {
            System.err.println("Error fetching note content: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches the entire note object by its ID.
     *
     * @param noteId the ID of the note.
     * @return the note object.
     */
    public Note getNoteByID(Long noteId) {
        try {
            return server.getNoteByID(noteId);
        } catch (Exception e) {
            System.err.println("Error fetching note: " + e.getMessage());
            return null;
        }
    }
}
