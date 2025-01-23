package server.controllers;

import models.Collection;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.CollectionServiceImpl;
import server.services.NoteService;

import javax.sound.midi.SysexMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @RestController is the same as @Controller + @ResponseBody
 *
 */
@RestController
@RequestMapping("/notes")
public class NoteController {
    private final NoteService noteService;
    private final CollectionServiceImpl collectionService;

    /**
     * Establishes NoteService implementation with Autowiring
     * @param noteService to establish the service
     * @param collectionService to establish the service
     */
    @Autowired
    public NoteController(NoteService noteService, CollectionServiceImpl collectionService) {
        this.noteService = noteService;
        this.collectionService = collectionService;
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
            if (note.getTitle() != null && noteService.titleExists(note.getTitle())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null); // 409 Conflict if title already exists
            }
            Note updatedNote = noteService.updateNote(id, note);
            System.out.println("Note " + id + " updated successfully");
            return ResponseEntity.ok(updatedNote);
        } catch (IllegalAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Note> addNote(@RequestBody Note note) {
        try {
            System.out.println("PROCESSING ADDITION");
            System.out.println(note);
            note.setTitle(noteService.generateUniqueTitle());
            Collection defaultCollection = collectionService.getDefaultCollection();
            note.addCollection(defaultCollection);
            System.out.println("DEFAULT COLLECTION : " + defaultCollection.getId());
            Note savedNote = noteService.saveNote(note);
            defaultCollection.addNoteToCollection(savedNote);
            System.out.println("SAVED NOTE: " + savedNote.toString());
            System.out.println(savedNote);
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

    @GetMapping("/get/{id}/collection")
    public ResponseEntity<Collection> getCollectionByNoteId(@PathVariable int id) {
        Collection collection = noteService.getCollectionByNoteId(id);
        return new ResponseEntity<>(collection, HttpStatus.OK);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkTitleExists(@RequestParam("title") String title) {
        boolean exists = noteService.titleExists(title);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/generate-title")
    public ResponseEntity<String> generateUniqueTitle() {
        String uniqueTitle = noteService.generateUniqueTitle();
        return ResponseEntity.ok(uniqueTitle);
    }
}
