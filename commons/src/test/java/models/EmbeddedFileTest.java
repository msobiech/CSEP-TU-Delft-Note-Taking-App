package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedFileTest {

    @Test
    void testGetNameWithoutExtension_WithExtension() {
        String fileName = "example.txt";
        String result = EmbeddedFile.getNameWithoutExtension(fileName);
        assertEquals("example", result, "The name without extension should be 'example'");
    }

    @Test
    void testGetNameWithoutExtension_WithoutExtension() {
        String fileName = "example";
        String result = EmbeddedFile.getNameWithoutExtension(fileName);
        assertEquals("example", result, "The name without extension should be the full name if no extension exists");
    }

    @Test
    void testGetNameWithoutExtension_EmptyString() {
        String fileName = "";
        String result = EmbeddedFile.getNameWithoutExtension(fileName);
        assertEquals("", result, "The result should be an empty string for an empty input");
    }

    @Test
    void testSetAndGetFileName() {
        EmbeddedFile file = new EmbeddedFile();
        file.setFileName("test.txt");
        assertEquals("test.txt", file.getFileName(), "The file name should be 'test.txt'");
    }

    @Test
    void testSetAndGetFileContent() {
        EmbeddedFile file = new EmbeddedFile();
        byte[] content = {1, 2, 3};
        file.setFileContent(content);
        assertArrayEquals(content, file.getFileContent(), "The file content should match the input byte array");
    }

    @Test
    void testConstructorWithAllArguments() {
        Note note = new Note();
        byte[] content = {1, 2, 3};
        EmbeddedFile file = new EmbeddedFile("test.txt", "text/plain", content, note);

        assertEquals("test.txt", file.getFileName(), "The file name should match");
        assertEquals("text/plain", file.getFileType(), "The file type should match");
        assertArrayEquals(content, file.getFileContent(), "The file content should match");
        assertEquals(note, file.getNote(), "The associated note should match");
    }

    @Test
    void testSetAndGetNote() {
        EmbeddedFile file = new EmbeddedFile();
        Note note = new Note();
        file.setNote(note);
        assertEquals(note, file.getNote(), "The note should match the set value");
    }

    @Test
    void testConstructorWithPartialArguments() {
        EmbeddedFile file = new EmbeddedFile("sample.txt", "text/plain", 123L);

        assertEquals("sample.txt", file.getFileName(), "The file name should match");
        assertEquals("text/plain", file.getFileType(), "The file type should match");
        assertEquals(123L, file.getId(), "The ID should match");
    }

    @Test
    void testDefaultConstructor() {
        EmbeddedFile file = new EmbeddedFile();
        assertNull(file.getFileName(), "The file name should be null by default");
        assertNull(file.getFileType(), "The file type should be null by default");
        assertNull(file.getFileContent(), "The file content should be null by default");
        assertNull(file.getNote(), "The note should be null by default");
    }

    @Test
    void testGetNameWithoutExtension_WithMultipleDots() {
        String fileName = "my.document.backup.txt";
        String result = EmbeddedFile.getNameWithoutExtension(fileName);
        assertEquals("my.document.backup", result, "The name without extension should be 'my.document.backup'");
    }
}
