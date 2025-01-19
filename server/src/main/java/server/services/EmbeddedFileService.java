package server.services;

import models.EmbeddedFile;

import java.util.List;

public interface EmbeddedFileService {

    boolean fileExists(Long id);

    EmbeddedFile getFileById(Long id);

    void deleteFileById(Long id) throws IllegalAccessException;

    EmbeddedFile modifyFileNameById(Long id, String newName) throws IllegalAccessException;

    List<EmbeddedFile> getFilesByNoteId(Long noteId);

    EmbeddedFile saveFile(EmbeddedFile file) throws IllegalAccessException;

    List<EmbeddedFile> getFilesTitleAndId(Long noteId);
}
