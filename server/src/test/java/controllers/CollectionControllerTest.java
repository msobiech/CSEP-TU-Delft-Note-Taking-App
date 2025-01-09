package controllers;


import models.Collection;
import models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server.controllers.CollectionController;
import server.services.NoteService;
import server.services.CollectionService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CollectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NoteService noteService;

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private CollectionController collectionController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(collectionController).build();
    }

    @Test
    public void testGetAll() throws Exception {
        Collection collection1 = new Collection();
        Collection collection2 = new Collection();

        collection1.setName("Collection 1");
        collection1.setId(1L);

        collection2.setName("Collection 2");
        collection2.setId(2L);
        when(noteService.getAllCollections()).thenReturn(Arrays.asList(collection1, collection2));

        mockMvc.perform(get("/collections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Collection 1"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Collection 2"));
    }

    @Test
    public void testAddCollection() throws Exception {
        Collection newCollection = new Collection();
        newCollection.setName("New Collection");
        newCollection.setId(3L);

        when(noteService.addCollection(any(Collection.class))).thenReturn(newCollection);

        mockMvc.perform(post("/collections")
                .contentType("application/json")
                .content("{\"id\": 3, \"name\": \"New Collection\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("New Collection"));
    }

    @Test
    public void testGetCollectionById() throws Exception {
        Collection collection = new Collection();
        collection.setName("Collection 1");
        collection.setId(1L);
        when(noteService.getCollectionById(1)).thenReturn(Optional.of(collection));

        mockMvc.perform(get("/collections/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Collection 1"));
    }

    @Test
    public void testGetNotesByCollectionId() throws Exception {
        Note note = new Note("Note 1", "content");
        when(noteService.getNotesByCollectionId(1)).thenReturn(Arrays.asList(note));

        mockMvc.perform(get("/collections/1/notes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(0))
            .andExpect(jsonPath("$[0].content").value("content"));
    }

    @Test
    public void testDeleteCollection() throws Exception {
        when(collectionService.collectionExists(1L)).thenReturn(true);

        mockMvc.perform(delete("/collections/delete/1"))
            .andExpect(status().isNoContent());

        verify(collectionService).deleteCollection(1L);
    }

    @Test
    public void testUpdateCollection() throws Exception {
        Collection updatedCollection = new Collection();
        updatedCollection.setName("Updated Collection");
        updatedCollection.setId(1L);
        when(collectionService.updateCollection(anyLong(), any(Collection.class)))
            .thenReturn(updatedCollection);

        mockMvc.perform(put("/collections/update/1")
                .contentType("application/json")
                .content("{\"id\": 1, \"name\": \"Updated Collection\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Collection"));
    }

    @Test
    public void testGetDefaultCollection() throws Exception {
        Collection defaultCollection = new Collection();
        defaultCollection.setName("Default Collection");
        defaultCollection.setId(1L);
        when(collectionService.getDefaultCollection()).thenReturn(defaultCollection);

        mockMvc.perform(get("/collections/default"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Default Collection"));
    }

    @Test
    public void testUpdateDefaultCollection() throws Exception {
        doNothing().when(collectionService).updateDefaultCollection(2L);

        mockMvc.perform(put("/collections/default")
                .contentType("application/json")
                .content("2"))
            .andExpect(status().isOk());

        verify(collectionService).updateDefaultCollection(2L);
    }
}
