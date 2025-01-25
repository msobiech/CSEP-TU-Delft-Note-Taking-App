package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {
    Note firstNote;
    Note secondNote;
    Note thirdNote;
    Note fourthNote;

    @BeforeEach
    void setup() {
        firstNote = new Note("1", "First note");
        secondNote = new Note("2", "Second note");
        thirdNote = new Note("1", "First note");
        fourthNote = new Note("4", "Fourth note");
    }

    @Test
    void testEquals() {
        assertEquals(firstNote, thirdNote); // Same content, should be equal
        assertNotEquals(firstNote, secondNote); // Different content, should not be equal
        assertNotEquals(firstNote, null); // A Note should not equal null
        assertNotEquals(firstNote, fourthNote); // Different content, should not be equal
    }

    @Test
    void testHashCode() {
        assertEquals(firstNote.hashCode(), firstNote.hashCode()); // Same object, same hashcode
        assertNotEquals(firstNote.hashCode(), secondNote.hashCode()); // Different objects, different hashcodes
        assertEquals(firstNote.hashCode(), thirdNote.hashCode()); // Same content, same hashcode
        assertNotEquals(firstNote.hashCode(), fourthNote.hashCode()); // Different content, different hashcode
    }

    @Test
    void testSetAndGetTitle() {
        Note note = new Note();
        note.setTitle("Updated Title");
        assertEquals("Updated Title", note.getTitle(), "The title should match the set value");
    }

    @Test
    void testSetAndGetContent() {
        Note note = new Note();
        note.setContent("Updated Content");
        assertEquals("Updated Content", note.getContent(), "The content should match the set value");
    }

    @Test
    void testSetAndGetId() {
        Note note = new Note();
        note.setId(3L);
        assertEquals(3L, note.getId(), "The id should match the set value");
    }

    @Test
    void testSetAndGetCollectionIds() {
        Note note = new Note();
        Set<Long> collectionIds = Set.of(1L, 2L);
        note.setCollectionIds(collectionIds);
        assertEquals(collectionIds, note.getCollectionIds(), "The collectionIds should match the given set");
    }

    @Test
    void testSetAndGetCollections() {
        Note note = new Note();
        Collection collection1 = new Collection();
        collection1.setId(1L);
        Collection collection2 = new Collection();
        collection2.setId(2L);
        note.setCollections(Set.of(collection1, collection2));
        assertEquals(Set.of(collection1, collection2), note.getCollections(), "The collections should match the given set");
    }

    @Test
    void testNullCollectionIds() {
        Note note = new Note();
        note.setCollectionIds(null);
        assertNull(note.getCollectionIds(), "The collectionIds should be null when set to null");
    }

    @Test
    void testEmptyCollectionIds() {
        Note note = new Note();
        note.setCollectionIds(Set.of());
        assertTrue(note.getCollectionIds().isEmpty(), "The collectionIds should be empty");
    }

    @Test
    void testEqualsDifferentTypes() {
        assertNotEquals(firstNote, new Object()); // Check that a Note is not equal to a random object
    }


    @Test
    void testEqualsNull() {
        Note note = new Note();
        assertNotEquals(note, null); // Check if a Note is not equal to null
    }

    @Test
    void testSetAndGetTitleAndContent() {
        Note note = new Note();
        note.setTitle("New Title");
        note.setContent("New Content");
        assertEquals("New Title", note.getTitle(), "Title should be 'New Title'");
        assertEquals("New Content", note.getContent(), "Content should be 'New Content'");
    }

    @Test
    void testSetAndGetCollectionIdsWithDifferentValues() {
        Note note = new Note();
        Set<Long> collectionIds = Set.of(3L, 4L);
        note.setCollectionIds(collectionIds);
        assertEquals(Set.of(3L, 4L), note.getCollectionIds(), "The collectionIds should be updated correctly");
    }

    @Test
    void testSetCollectionsWithDifferentObjects() {
        Note note = new Note();
        Collection collection1 = new Collection();
        collection1.setId(5L);
        Collection collection2 = new Collection();
        collection2.setId(6L);
        note.setCollections(Set.of(collection1, collection2));
        assertEquals(Set.of(collection1, collection2), note.getCollections(), "The collections should be correctly set with different objects");
    }

    @Test
    void testGetCollectionsWithNoCollections() {
        Note note = new Note();
        note.setCollections(Set.of());
        assertTrue(note.getCollections().isEmpty(), "The collections set should be empty");
    }

    @Test
    void testSetCollectionIdsWithEmptySet() {
        Note note = new Note();
        note.setCollectionIds(Set.of());
        assertTrue(note.getCollectionIds().isEmpty(), "The collectionIds set should be empty after setting an empty set");
    }

    @Test
    void testSetNullCollections() {
        Note note = new Note();
        note.setCollections(null);
        assertNull(note.getCollections(), "The collections should be null when set to null");
    }

    @Test
    void testHashCodeWithNullCollectionIds() {
        Note note = new Note();
        note.setCollectionIds(null);
        assertNotEquals(firstNote.hashCode(), note.hashCode(), "Hashcodes should not match when collectionIds are null");
    }
}
