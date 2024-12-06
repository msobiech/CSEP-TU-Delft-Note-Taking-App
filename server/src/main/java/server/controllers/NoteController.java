package server.controllers;

import javafx.util.Pair;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
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
}
