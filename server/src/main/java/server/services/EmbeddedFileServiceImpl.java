package server.services;

import models.EmbeddedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.repositories.EmbeddedFileRepository;

import java.util.List;

@Service
public class EmbeddedFileServiceImpl implements EmbeddedFileService {
    private final EmbeddedFileRepository fileRepo;

    @Autowired
    public EmbeddedFileServiceImpl(EmbeddedFileRepository fileRepo) {
        this.fileRepo = fileRepo;
    }

    @Override
    public boolean fileExists(Long id){
        return fileRepo.existsById(id);
    }

    public EmbeddedFile getFileById(Long id){
        return fileRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteFileById(Long id) throws IllegalAccessException {
        if (!fileExists(id)) {
            throw new IllegalAccessException("File with id " + id + " does not exist.");
        }
        fileRepo.deleteById(id);
    }

    @Override
    public EmbeddedFile modifyFileNameById(Long id, String newName) throws IllegalAccessException{
        if (!fileExists(id)) {
            throw new IllegalAccessException("File with id " + id + " does not exist.");
        }

        EmbeddedFile file = fileRepo.findById(id).orElseThrow();
        file.setFileName(newName);
        return fileRepo.save(file);

    }

    @Override
    public List<EmbeddedFile> getFilesByNoteId(Long noteId){
        return fileRepo.findByNoteId(noteId);
    }

    @Override
    public EmbeddedFile saveFile(EmbeddedFile file){
        return fileRepo.save(file);
    }
}
