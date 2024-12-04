package server.controllers;

import models.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.repositories.NoteRepository;

import java.util.List;

// CREATE READ UPDATE DELETE
//  POST   GET  PUT   DELETE

@Controller
@ResponseBody
@RequestMapping("/notes")
public class NoteController {
    private final NoteRepository repo;

    public NoteController(NoteRepository repo) {
        this.repo = repo;
    }

    @GetMapping(path = {"", "/"})
    public List<Note> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id) {
        if (id < 0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findById(id).orElse(null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable("id") long id, @RequestBody Note updatedNote) {
        if (id < 0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        Note existingNote = repo.findById(id).orElse(null);
        if (existingNote == null) {
            return ResponseEntity.notFound().build();
        }
        // Handle title updates
        if (updatedNote.getTitle() != null) {
            existingNote.setTitle(updatedNote.getTitle());
            if (updatedNote.getTitle().isEmpty()) {
                // Would be nice to add a method to generate a unique name (e.g. Untitled Note 1, 2 etc)
                existingNote.setTitle("Untitled Note");
            }
        }
        if (updatedNote.getContent() != null) {
            existingNote.setContent(updatedNote.getContent());
        }
        // Save the updated note
        Note savedNote = repo.save(existingNote);
        return ResponseEntity.ok(savedNote);
    }

    @PostMapping
    public ResponseEntity<Note> add(@RequestBody Note note) {
        if (isNullOrEmpty(note.getTitle()) || note.getContent() == null) {
            return ResponseEntity.badRequest().build();
        }
        Note saved = repo.save(note);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/titles")
    public List<Object[]> getTitles() {
        return repo.findIdAndTitle();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        } else {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
    }


    /**
     * Checks if a given string is null or empty.
     * @param s the string to check
     * @return true if the string is null or empty, false otherwise
     */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
