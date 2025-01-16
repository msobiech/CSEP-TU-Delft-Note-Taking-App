package server.services;

import models.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.repositories.CollectionRepository;

import java.util.Optional;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepo;

    private final CustomWebSocketHandler webSocketHandler;

    /**
     * Establishes repositories used in the service
     * @param collectionRepo the repository with collections
     * @param webSocketHandler the handler
     */
    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepo, CustomWebSocketHandler webSocketHandler) {
//        this.noteRepo = noteRepo;
        this.collectionRepo = collectionRepo;
        this.webSocketHandler = webSocketHandler;
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

    public boolean doesCollectionExist(String collectionName) {
        Optional<Collection> collection = collectionRepo.findByName(collectionName);
        return collection.map(c -> collectionExists(c.getId())).orElse(false);
    }

    public void notifyClients(String message) {
        webSocketHandler.broadcastMessage(message);
    }


}
