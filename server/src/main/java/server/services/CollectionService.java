package server.services;

import models.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.repositories.CollectionRepository;
import server.repositories.NoteRepository;

@Service
public class CollectionService {
    private final NoteRepository noteRepo;
    private final CollectionRepository collectionRepo;

    /**
     * Establishes repositories used in the service
     * @param noteRepo the repository with notes
     * @param collectionRepo the repository with collections
     */
    @Autowired
    public CollectionService(NoteRepository noteRepo, CollectionRepository collectionRepo) {
        this.noteRepo = noteRepo;
        this.collectionRepo = collectionRepo;
    }


    public boolean collectionExists(long id) {
        return id > 0 && collectionRepo.existsById(id);
    }

    public void deleteCollection(long id) throws IllegalAccessException{
        if(!collectionExists(id)) {
            throw new IllegalAccessException("Collection with id " + id + " does not exist");
        }
        collectionRepo.deleteById(id);
    }

    public Collection updateCollection(long id, Collection collection) throws IllegalAccessException{
        if(!collectionExists(id)) {
            throw new IllegalAccessException("Collection with id " + id + " does not exist.");
        }
        Collection fetchedCollection = collectionRepo.findById(id).orElseThrow();
        if(collection.getName() != null) {
            fetchedCollection.setName(collection.getName());    //update name if changed
        }
        if(collection.getNotes() != null) {
            fetchedCollection.setNotes(collection.getNotes());  //update notes in collection if changed
        }
        return collectionRepo.save(fetchedCollection);
    }
}
