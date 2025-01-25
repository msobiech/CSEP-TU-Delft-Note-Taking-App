package models;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CollectionTest {

    @Test
    void testEquals() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(1L);
        collection2.setName("Test Collection");

        assertEquals(collection1, collection2);
    }

    @Test
    void testNotEquals() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(2L); // Different ID
        collection2.setName("Test Collection");

        assertNotEquals(collection1, collection2);
    }

    @Test
    void testHashCodeWithDifferentObjects() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(2L); // Different ID
        collection2.setName("Test Collection");

        assertNotEquals(collection1.hashCode(), collection2.hashCode());
    }

    @Test
    void testHashCode() {
        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setName("Test Collection");

        Collection collection2 = new Collection();
        collection2.setId(1L);
        collection2.setName("Test Collection");

        assertEquals(collection1.hashCode(), collection2.hashCode());
    }

    @Test
    void testToString() {
        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("My Collection");

        assertEquals("Collection{id=1, name='My Collection'}", collection.toString());
    }

    @Test
    void testSetAndGetId() {
        Collection collection = new Collection();
        collection.setId(1L);
        assertEquals(1L, collection.getId(), "The id should match the set value");
    }

    @Test
    void testSetAndGetName() {
        Collection collection = new Collection();
        collection.setName("My Collection");
        assertEquals("My Collection", collection.getName(), "The name should match the set value");
    }

    @Test
    void testSetAndGetNotes() {
        Collection collection = new Collection();
        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Test Note 1");
        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Test Note 2");
        collection.setNotes(Set.of(note1, note2));
        assertEquals(Set.of(note1, note2), collection.getNotes(), "The notes should match the given set");
    }

    @Test
    void testIsDefaultIsTrue() {
        Collection collection = new Collection();
        collection.setDefault(true);
        assertTrue(collection.getIsDefault(), "The collection should be the default collection");
    }

    @Test
    void testIsDefaultIsFalse() {
        Collection collection = new Collection();
        collection.setDefault(false);
        assertFalse(collection.getIsDefault(), "The collection should not be the default collection");
    }
}