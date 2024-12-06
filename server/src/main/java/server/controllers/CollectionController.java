package server.controllers;

import models.Collection;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.NoteService;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final NoteService noteService;

    @Autowired
    public CollectionController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Collection> getAll() {
        return noteService.getAllCollections();
    }

    @PostMapping
    public ResponseEntity<Collection> addCollection(@RequestBody Collection collection) {
        System.out.println(collection);
        Collection addedCollection = noteService.addCollection(collection);
        return ResponseEntity.ok(addedCollection);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Collection> getCollectionById(@PathVariable int id) {
        return noteService.getCollectionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<Note>> getNotesByCollectionId(@PathVariable int id) {
        List<Note> notes = noteService.getNotesByCollectionId(id);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

}
