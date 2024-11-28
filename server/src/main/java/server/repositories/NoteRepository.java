package server.repositories;

import models.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n.id, n.title FROM Note n")
    List<Object[]> findIdAndTitle();
}
