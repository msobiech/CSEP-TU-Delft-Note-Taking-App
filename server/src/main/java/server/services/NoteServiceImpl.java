package server.services;

import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.repositories.NoteRepository;
import server.utils.StringUtils;

import java.util.List;
import java.util.Optional;

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
        if (note.getTitle() != null) {
            if (note.getTitle().isEmpty()) {
                fetchedNote.setTitle("Untitled Note");
            } else {
                fetchedNote.setTitle(note.getTitle());
            }
        }
        if (note.getContent() != null) {
            fetchedNote.setContent(note.getContent());
        }
        return repo.save(fetchedNote);
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
