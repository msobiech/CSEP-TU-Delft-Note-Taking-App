package server.repositories;

import models.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    /**
     * Retrieves a list of note IDs and titles.
     *
     * @return a {@link List} of {@code Object[]} with each array containing the ID and title of a note.
     */
    @Query("SELECT n.id, n.title FROM Note n")
    List<Object[]> findIdAndTitle();

    /**
     * Retrieves notes whose titles contain the specified keyword, ignoring case.
     *
     * @param keyword the substring to search for in note titles.
     * @return a list of matching {@link Note} objects, or an empty list if no matches are found.
     */
    @Query("SELECT n FROM Note n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Note> findByTitleOrContentContainingIgnoreCase(String keyword);

    /**
     * Checks if a note with the specified title exists in the database.
     *
     * @param title the title to check for.
     * @return {@code true} if a note with the specified title exists, {@code false} otherwise.
     */
    @Query("SELECT COUNT(n) > 0 FROM Note n WHERE n.title = :title")
    boolean existsByTitle(@Param("title") String title);

    /**
     * Retrieves notes connected to the collection with given id
     * @param collectionId to search by
     * @return a list of match {@link Note} objects, or an empty list if no matches are found.
     */
    List<Note> findNotesByCollectionsId(Long collectionId);

}
