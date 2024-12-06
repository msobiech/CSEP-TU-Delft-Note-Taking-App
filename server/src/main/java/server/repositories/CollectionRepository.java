package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import models.Collection;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findCollectionsByNotesId(Long noteId);
}
