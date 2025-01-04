package server.services;

import models.Collection;
import models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.repositories.CollectionRepository;
import server.repositories.NoteRepository;
import server.utils.StringUtils;

import java.util.*;

@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepo;
    private final CollectionRepository collectionRepo;

    /**
     * Establishes repositories used in the service
     * @param noteRepo the repository with notes
     * @param collectionRepo the repository with collections
     */
    @Autowired
    public NoteServiceImpl(NoteRepository noteRepo, CollectionRepository collectionRepo) {
        this.noteRepo = noteRepo;
        this.collectionRepo = collectionRepo;
    }

    @Override
    public List<Note> getAllNotes() {
        return noteRepo.findAll();
    }

    @Override
    public Optional<Note> getNoteById(long id) {
        return id > 0 ? noteRepo.findById(id) : Optional.empty();
    }

    @Override
    public boolean noteExists(long id) {
        return id > 0 && noteRepo.existsById(id);
    }

    @Override
    public Note saveNote(Note note) {
        return noteRepo.save(note);
    }

    @Override
    public List<Note> searchNotesByKeyword(String keyword) {
        return noteRepo.findByTitleOrContentContainingIgnoreCase(keyword);
    }

    @Override
    public List<Object[]> getNotesIdAndTitle() {
        return noteRepo.findIdAndTitle();
    }

    @Override
    public Note updateNote(long id, Note note) throws IllegalAccessException {
        if (!noteExists(id)) {
            throw new IllegalAccessException("Note with id " + id + " does not exist.");
        }
        Note fetchedNote = noteRepo.findById(id).orElseThrow();
        if (note.getContent() != null) {
            fetchedNote.setContent(note.getContent());  // Update content if changed
        }
        if (note.getTitle() != null) {
            fetchedNote.setTitle(note.getTitle());      // Update title if changed
        }
        if (fetchedNote.getTitle().isEmpty()) {
            fetchedNote.setTitle(generateUniqueTitle());// Generate unique title if only title is empty
        }
        fetchedNote.setCollections(note.getCollections());
        Set<Collection> newCollections = new HashSet<Collection>();
        for(Long collectionId:note.getCollectionIds()){
            Optional<Collection> collection = getCollectionById(collectionId);
            if(collection.isPresent()){
                newCollections.add(collection.get());
            } else{
                throw new IllegalAccessException("Collection with id " + collectionId + " does not exist.");
            }
        }
        fetchedNote.setCollections(newCollections);
        return noteRepo.save(fetchedNote);
    }

    /**
     * Generates a unique title following the pattern "Untitled Note X".
     * If there are gaps in the sequence, it assigns the first available gap.
     * If no gaps exist, it assigns the next number after the largest suffix.
     * @return a unique title.
     */
    @Override
    public String generateUniqueTitle() {
        // Get all Untitled Note titles
        List<String> titles = noteRepo.findAll().stream()
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


    @Override
    public void deleteNote(long id) throws IllegalAccessException {
        if (!noteExists(id)) {
            throw new IllegalAccessException("Note with id " + id + " does not exist.");
        }
        noteRepo.deleteById(id);
    }


    @Override
    public List<Object[]> searchNotes(String keyword) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            return List.of();
        }
        return noteRepo.findByTitleOrContentContainingIgnoreCase(keyword).stream()
                .map(note -> new Object[] {note.getId(), note.getTitle()})
                .toList();
    }

    @Override
    public boolean titleExists(String title) {
        return noteRepo.existsByTitle(title);
    }

    @Override
    public List<models.Collection> getAllCollections() {
        return collectionRepo.findAll();

    }

    @Override
    public models.Collection addCollection(Collection collection) {
        return collectionRepo.save(collection);
    }

    @Override
    public Optional<Collection> getCollectionById(long id) {
        return id > 0 ? collectionRepo.findById(id) : Optional.empty();
    }

    @Override
    public List<Note> getNotesByCollectionId(long id){
        return noteRepo.findNotesByCollectionsId(id);
    }

    @Override
    public List<Collection> getCollectionsByNoteId(long id) {
        return collectionRepo.findCollectionsByNotesId(id);
    }
}
