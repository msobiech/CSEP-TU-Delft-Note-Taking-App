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

    @PutMapping("/setContent/{id}")
    public ResponseEntity<Note> setContentById(@PathVariable("id") long id, @RequestBody String content) {
        if (id < 0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        Note foundNote = repo.findById(id).get();
        foundNote.content = content;
        return ResponseEntity.ok(repo.save(foundNote));
    }

    @PostMapping
    public ResponseEntity<Note> add(@RequestBody Note note) {
        if (false) {
            return ResponseEntity.badRequest().build();
        }
        Note saved = repo.save(note);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/titles")
    public List<Object[]> getTitles() {
        return repo.findIdAndTitle();
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
