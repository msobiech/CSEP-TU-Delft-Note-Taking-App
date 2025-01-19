package server.services;

import models.Collection;

public interface CollectionService {
    boolean collectionExists(long id);

    void deleteCollection(long id) throws IllegalAccessException;

    Collection updateCollection(long id, Collection collection) throws IllegalAccessException;

    void updateDefaultCollection(Long newDefaultCollectionId);

    Collection getDefaultCollection();
}
