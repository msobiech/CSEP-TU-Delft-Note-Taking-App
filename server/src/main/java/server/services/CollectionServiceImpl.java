package server.services;

import models.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.repositories.CollectionRepository;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepo;

    /**
     * Establishes repositories used in the service
     * @param collectionRepo the repository with collections
     */
    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepo) {
//        this.noteRepo = noteRepo;
        this.collectionRepo = collectionRepo;
    }


    @Override
    public boolean collectionExists(long id) {
        return id > 0 && collectionRepo.existsById(id);
    }

    @Override
    public void deleteCollection(long id) throws IllegalAccessException{
        if(!collectionExists(id)) {
            throw new IllegalAccessException("Collection with id " + id + " does not exist");
        }
        collectionRepo.deleteById(id);
    }

    @Override
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

    @Override
    public Collection getDefaultCollection() {
        return collectionRepo.findDefaultCollection();
    }


    @Override
    @Transactional
    public void updateDefaultCollection(Long newDefaultCollectionId) {
        collectionRepo.unsetDefaultCollection();

        collectionRepo.setDefaultCollection(newDefaultCollectionId);
    }
}
