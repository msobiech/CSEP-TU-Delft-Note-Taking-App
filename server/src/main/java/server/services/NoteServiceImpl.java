package server.services;

import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.repositories.NoteRepository;
import server.utils.StringUtils;

import java.util.*;

@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository repo;

    @Autowired
    public NoteServiceImpl(NoteRepository repo) {
        this.repo = repo;
    }

    public List<Note> getAllNotes() {
        return repo.findAll();
    }

    public Optional<Note> getNoteById(long id) {
        return id > 0 ? repo.findById(id) : Optional.empty();
    }

    public boolean noteExists(long id) {
        return id > 0 && repo.existsById(id);
    }

    public Note saveNote(Note note) {
        return repo.save(note);
    }

    public List<Note> searchNotesByKeyword(String keyword) {
        return repo.findByTitleOrContentContainingIgnoreCase(keyword);
    }

    public List<Object[]> getNotesIdAndTitle() {
        return repo.findIdAndTitle();
    }

    public Note updateNote(long id, Note note) throws IllegalAccessException {
        if (!noteExists(id)) {
            throw new IllegalAccessException("Note with id " + id + " does not exist.");
        }
        Note fetchedNote = repo.findById(id).orElseThrow();
        if (note.getContent() != null) {
            fetchedNote.setContent(note.getContent());  // Update content if changed
        }
        if (note.getTitle() != null) {
            fetchedNote.setTitle(note.getTitle());      // Update title if changed
        }
//        if (fetchedNote.getTitle().isEmpty()
//                && fetchedNote.getContent().isEmpty()) {
//            repo.deleteById(id);                        // Delete note it title and content are empty
//            return null;
//        }
        if (fetchedNote.getTitle().isEmpty()) {
            fetchedNote.setTitle(generateUniqueTitle());// Generate unique title if only title is empty
        }
        return repo.save(fetchedNote);
    }

    /**
     * Generates a unique title following the pattern "Untitled Note X".
     * If there are gaps in the sequence, it assigns the first available gap.
     * If no gaps exist, it assigns the next number after the largest suffix.
     * @return a unique title.
     */
    private String generateUniqueTitle() {
        // Get all Untitled Note titles
        List<String> titles = repo.findAll().stream()
                                            .map(Note::getTitle)
                                            .filter(title -> title.startsWith("Untitled Note "))
                                            .toList();
        // Get the numbers of the Untitled notes
        List<Integer> numbers = titles.stream()
                                    .map(title -> {
                                        String[] split = title.split(" ");
                                        try {
                                            return Integer.parseInt(split[2]);
                                        } catch (Exception e) {
                                            return -1; // Ignore titles that don't match the format
                                        }
                                    })
                                    .filter(n -> n > 0)
                                    .sorted()
                                    .toList();
        // Find the next available number in the sequence
        int nextNumber = 1;
        for (Integer number : numbers) {
            if (number != nextNumber) {
                break; // Found a gap
            }
            nextNumber++;
        }
        return "Untitled Note " + nextNumber;
    }


    public void deleteNote(long id) throws IllegalAccessException {
        if (!noteExists(id)) {
            throw new IllegalAccessException("Note with id " + id + " does not exist.");
        }
        repo.deleteById(id);
    }

    public List<Note> searchNotes(String keyword) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            return List.of();
        }
        return repo.findByTitleOrContentContainingIgnoreCase(keyword);
    }
}
