package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import models.Collection;


@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    /**
     * Retrieves collections connected to the note with given id
     * @param noteId to search by
     * @return a list of match {@link Collection} objects, or an empty list if no matches are found.
     */
    Collection findCollectionsByNotesId(Long noteId);


    @Query("SELECT c FROM Collection c WHERE c.isDefault = true")
    Collection findDefaultCollection();


    @Modifying
    @Query("UPDATE Collection c SET c.isDefault = false WHERE c.isDefault = true")
    void unsetDefaultCollection();


    @Modifying
    @Query("UPDATE Collection c SET c.isDefault = true WHERE c.id = :id")
    void setDefaultCollection(@Param("id") Long id);

    @Query("SELECT COUNT(c) > 0 FROM Collection c WHERE c.isDefault = :isDefault")
    boolean defaultExists(@Param("isDefault") boolean isDefault);
}
