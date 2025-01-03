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
}
