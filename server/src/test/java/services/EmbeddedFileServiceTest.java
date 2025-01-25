package services;

import models.EmbeddedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.repositories.EmbeddedFileRepository;
import server.services.EmbeddedFileServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmbeddedFileServiceTest {

    @Mock
    private EmbeddedFileRepository embeddedFileRepository;

    @InjectMocks
    private EmbeddedFileServiceImpl embeddedFileService;

    private EmbeddedFile testEmbeddedFile;

    @BeforeEach
    public void setUp() {
        testEmbeddedFile = new EmbeddedFile();
        testEmbeddedFile.setFileName("Test File");
        testEmbeddedFile.setFileId(1L);
    }

    @Test
    public void testEmbeddedFileExistsTrue() {
        when(embeddedFileRepository.existsById(anyLong())).thenReturn(true);
        boolean result = embeddedFileService.fileExists(1L);
        assertTrue(result);
    }

    @Test
    public void testEmbeddedFileExistsFalse() {
        when(embeddedFileRepository.existsById(anyLong())).thenReturn(false);
        boolean result = embeddedFileService.fileExists(1L);
        assertFalse(result);
    }

    @Test
    public void getFileByIdTest() {
        when(embeddedFileRepository.findById(1L)).thenReturn(Optional.of(testEmbeddedFile));
        EmbeddedFile embeddedFile = embeddedFileService.getFileById(1L);
        assertEquals("Test File", embeddedFile.getFileName() );
        verify(embeddedFileRepository).findById(1L);
    }

    @Test
    public void deleteFileByIdTest() throws IllegalAccessException {
        when(embeddedFileRepository.existsById(1L)).thenReturn(true);
        embeddedFileService.deleteFileById(1L);
        verify(embeddedFileRepository).deleteById(1L);
    }

}