package services;

import models.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import server.repositories.CollectionRepository;
import server.services.CollectionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private CollectionService collectionService;

    private Collection collection;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        collection = new Collection();
        collection.setName("Test Collection");
        collection.setId(1L);
    }

    @Test
    public void testCollectionExistsTrue() {
        when(collectionRepository.existsById(anyLong())).thenReturn(true);
        boolean result = collectionService.collectionExists(1);
        assertTrue(result);
    }

    @Test
    public void testCollectionExistsFalse() {
        when(collectionRepository.existsById(anyLong())).thenReturn(false);
        boolean result = collectionService.collectionExists(24);
        assertFalse(result);
    }

    @Test
    public void testDeleteCollectionSuccess() throws IllegalAccessException {
        when(collectionRepository.existsById(anyLong())).thenReturn(true);
        collectionService.deleteCollection(1);
        verify(collectionRepository).deleteById(1L);
    }

    @Test
    public void testUpdateCollectionSuccess() throws IllegalAccessException {
        Collection existingCollection = new Collection();
        existingCollection.setName("Existing Collection");
        existingCollection.setId(1L);
        existingCollection.setNotes(null);
        when(collectionRepository.existsById(anyLong())).thenReturn(true);
        when(collectionRepository.findById(anyLong())).thenReturn(java.util.Optional.of(existingCollection));
        when(collectionRepository.save(any(Collection.class))).thenReturn(existingCollection);

        Collection updatedCollection = collectionService.updateCollection(1, collection);

        assertNotNull(updatedCollection);
        assertEquals("Test Collection", updatedCollection.getName());
    }


    @Test
    public void testGetDefaultCollection() {
        Collection defaultCollection = new Collection();
        defaultCollection.setName("Default Collection");
        defaultCollection.setId(1L);
        when(collectionRepository.findDefaultCollection()).thenReturn(defaultCollection);

        Collection result = collectionService.getDefaultCollection();

        assertNotNull(result);
        assertEquals("Default Collection", result.getName());
    }

    @Test
    public void testUpdateDefaultCollection() {
        doNothing().when(collectionRepository).unsetDefaultCollection();
        doNothing().when(collectionRepository).setDefaultCollection(anyLong());

        collectionService.updateDefaultCollection(2L);

        verify(collectionRepository).unsetDefaultCollection();
        verify(collectionRepository).setDefaultCollection(2L);
    }
}

