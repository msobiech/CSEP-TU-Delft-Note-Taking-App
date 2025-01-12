package server.controllers;

import models.Collection;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.NoteService;
import server.services.CollectionServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final NoteService noteService;
    private final CollectionServiceImpl collectionService;

    /**
     * Establishes the noteService implementation with autowiring
     * @param noteService to set the service to the controller
     * @param collectionService to set the service to the controller
     */
    @Autowired
    public CollectionController(NoteService noteService, CollectionServiceImpl collectionService) {
        this.noteService = noteService;
        this.collectionService = collectionService;
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

    @GetMapping("/get/{id}")
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable("id") long id) {
        try {
            if(!collectionService.collectionExists(id)){
                return ResponseEntity.notFound().build();
            }
            collectionService.deleteCollection(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalAccessException e){
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable("id") long id, @RequestBody Collection collection) {
        try {
            Collection updatedCollection = collectionService.updateCollection(id, collection);
            return ResponseEntity.ok(updatedCollection);
        } catch (IllegalAccessException e) {
            return ResponseEntity.notFound().build(); // if the note does not exist, return 404 Not Found
        }
    }

    @GetMapping("/default")
    public ResponseEntity<Collection> getDefaultCollection() {
        Collection defaultCollection = collectionService.getDefaultCollection();
        return ResponseEntity.ok(defaultCollection);
    }

    @PutMapping("/default")
    public ResponseEntity<Void> updateDefaultCollection(@RequestBody Long newDefaultCollectionId) {
        collectionService.updateDefaultCollection(newDefaultCollectionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<String> getCollectionStatus(@RequestParam String collectionName) {
        // Simulate logic for status
        if (!serverIsReachable()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Server not reachable");
        }
        boolean exists = collectionService.doesCollectionExist(collectionName);
        if (exists) {
            return ResponseEntity.ok("Collection exists");
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body("Collection will be created");
        }
    }

    private boolean serverIsReachable() {
        return true; // for now TODO: NEEDS TO BE CHANGED
    }

}
