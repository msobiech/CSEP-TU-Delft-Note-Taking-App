package controllers;

import models.EmbeddedFile;
import models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server.controllers.EmbeddedFileController;
import server.services.EmbeddedFileServiceImpl;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmbeddedFileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmbeddedFileServiceImpl fileService;

    @InjectMocks
    private EmbeddedFileController fileController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    public void testGetByNoteId() throws Exception {
        Note note = new Note();
        note.setId(1L);

        EmbeddedFile file1 = new EmbeddedFile();
        file1.setFileName("Test File 1");
        file1.setFileId(1L);
        file1.setNote(note);

        EmbeddedFile file2 = new EmbeddedFile();
        file2.setFileName("Test File 2");
        file2.setFileId(2L);
        file2.setNote(note);


        when(fileService.getFilesTitleAndId(1L)).thenReturn(Arrays.asList(file1, file2));

        mockMvc.perform(get("/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("Test File 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fileName").value("Test File 2"));
    }

}
