package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import models.Collection;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    /**
     * Retrieves collections connected to the note with given id
     * @param noteId to search by
     * @return a list of match {@link Collection} objects, or an empty list if no matches are found.
     */
    List<Collection> findCollectionsByNotesId(Long noteId);
}
