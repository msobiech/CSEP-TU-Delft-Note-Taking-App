package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
