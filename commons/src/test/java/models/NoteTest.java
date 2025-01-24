package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NoteTest {
    Note firstNote;
    Note secondNote;
    Note thirdNote;;
    Note fourthNote;

    @BeforeEach
    void setup() {
        firstNote = new Note("1", "First note");
        secondNote= new Note("2", "Second note");
        thirdNote = new Note("1", "First note");
    }

    @Test
    void testEquals() {
        assertEquals(firstNote, thirdNote);
        assertEquals(fourthNote, null);
    }

    @Test
    void testNotEquals() {
        assertNotEquals(firstNote, secondNote);
        assertNotEquals(thirdNote, secondNote);
        assertNotEquals(firstNote, null);
    }

    @Test
    void testHashCode() {
        assertEquals(firstNote.hashCode(), firstNote.hashCode());
        assertNotEquals(firstNote.hashCode(), secondNote.hashCode());
        assertEquals(firstNote.hashCode(), thirdNote.hashCode());
    }

    @Test
    void testSetAndGetCollectionIds() {
        Collection collection = new Collection();
        collection.setId(1L);
        Set<Long> collectionIds = new HashSet<>();
        collectionIds.add(collection.getId());
        Note note = new Note();
        note.setCollectionIds(collectionIds);
        assertEquals(collectionIds, note.getCollectionIds(), "The list of collectionIds should match the set value");
    }

    @Test
    void testSetAndGetTitle() {
        Note note = new Note();
        note.setTitle("Title");
        assertEquals("Title", note.getTitle(), "The title should match the set value");
    }

    @Test
    void testSetAndGetContent() {
        Note note = new Note();
        note.setContent("Content");
        assertEquals("Content", note.getContent(), "The content should match the set value");
    }

    @Test
    void testSetAndGetId() {
        Note note = new Note();
        note.setId(1L);
        assertEquals(1L, note.getId(), "The id should match the set value");
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

}
