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
}
