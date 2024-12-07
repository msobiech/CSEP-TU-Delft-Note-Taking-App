package server.controllers;

import models.Collection;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.NoteService;

import java.util.List;

/**
 *
 * @RestController is the same as @Controller + @ResponseBody
 *
 */
@RestController
@RequestMapping("/notes")
public class NoteController {
    private final NoteService noteService;

    /**
     * Establishes NoteService implementation with Autowiring
     * @param noteService to establish the service
     */
    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/get")
    public List<Note> getAll() {
        return noteService.getAllNotes();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Updates note with given id and structure
     * @param id of note to update
     * @param note to update to
     * @return Updated note
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable("id") long id, @RequestBody Note note) {
        try {
            Note updatedNote = noteService.updateNote(id, note);
            return ResponseEntity.ok(updatedNote);
        } catch (IllegalAccessException e) {
            return ResponseEntity.notFound().build(); // if the note does not exist, return 404 Not Found
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Note> addNote(@RequestBody Note note) {
        try {
            note.setTitle(noteService.generateUniqueTitle());
            Note savedNote = noteService.saveNote(note);
            return ResponseEntity.ok(savedNote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // catch unexpected errors and return 500 Internal Server Error
        }
    }

    /**
     * Deletes note with given id
     * @param id of note to delete
     * @return (TODO: Change it to return deleted note) Returns nothing
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable("id") long id) {
        try {
            if (!noteService.noteExists(id)) {
                return ResponseEntity.notFound().build();
            }
            noteService.deleteNote(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(500).build();
        }
    }
    @GetMapping("/titles")
    public List<Object[]> getTitles() {
        return noteService.getNotesIdAndTitle();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Object[]>> searchNote(@RequestParam("keyword") String keyword) {
        List<Object[]> notes = noteService.searchNotes(keyword);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/get/{id}/collections")
    public ResponseEntity<List<Collection>> getCollectionsByNoteId(@PathVariable int id) {
        List<Collection> collections = noteService.getCollectionsByNoteId(id);
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }
}
