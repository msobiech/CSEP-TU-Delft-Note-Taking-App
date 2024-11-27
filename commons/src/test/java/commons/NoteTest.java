package commons;

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
        firstNote = new Note("First note");
        secondNote= new Note("Second note");
        thirdNote = new Note("First note");
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
