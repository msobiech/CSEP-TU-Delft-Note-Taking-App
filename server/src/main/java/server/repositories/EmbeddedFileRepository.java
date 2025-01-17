package server.repositories;

import models.EmbeddedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmbeddedFileRepository extends JpaRepository<EmbeddedFile, Long> {
    List<EmbeddedFile> findByNoteId(Long noteId);
}
