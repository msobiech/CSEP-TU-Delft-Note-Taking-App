package server.controllers;

import models.EmbeddedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.EmbeddedFileService;
import server.services.EmbeddedFileServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/files")
public class EmbeddedFileController {
    private final EmbeddedFileService embeddedFileService;

    @Autowired
    public EmbeddedFileController(EmbeddedFileServiceImpl fileService) {
        this.embeddedFileService = fileService;
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<List<EmbeddedFile>> getByNoteId(@PathVariable("noteId") Long noteId){
        List<EmbeddedFile> list = embeddedFileService.getFilesTitleAndId(noteId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{noteId}/{fileId}")
    public ResponseEntity<EmbeddedFile> getFileByNoteIdAndFileId(@PathVariable("noteId") Long noteId, @PathVariable("fileId") Long fileId){
        EmbeddedFile file = embeddedFileService.getFileById(fileId);
        if(file.getNote().getId()!=noteId){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }

    @GetMapping("/{noteId}/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("noteId") Long noteId, @PathVariable("fileId") Long fileId){
        EmbeddedFile file = embeddedFileService.getFileById(fileId);
        if(file==null){
            return ResponseEntity.notFound().build();
        } else if(file.getNote().getId()!=noteId){
            return ResponseEntity.notFound().build();
        }
        ByteArrayResource resource = new ByteArrayResource(file.getFileContent());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(resource);
    }

    @PostMapping
    public ResponseEntity<EmbeddedFile> createFile(@RequestBody EmbeddedFile file) {
        EmbeddedFile addedFile = null;
        try {
            addedFile = embeddedFileService.saveFile(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("Created new file with id: " + addedFile.getId());
        return ResponseEntity.ok(addedFile);
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<EmbeddedFile> updateFile(@PathVariable("fileId") Long fileId, @RequestBody String newName){
        EmbeddedFile file = null;
        try {
            file = embeddedFileService.modifyFileNameById(fileId, newName);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }

    @DeleteMapping("/{fileid}")
    public ResponseEntity<Boolean> deleteFile(@PathVariable("fileid") Long fileId){
        try{
            embeddedFileService.deleteFileById(fileId);
            return ResponseEntity.ok(true);
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }

    }
}
