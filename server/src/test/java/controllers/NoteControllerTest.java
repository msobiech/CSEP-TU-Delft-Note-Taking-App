package controllers;

import models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import server.controllers.NoteController;
import server.services.NoteService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteControllerTest {

    @Mock
    private NoteService noteService; // Mocked service

    @InjectMocks
    private NoteController noteController; // Controller under test

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testGetAll() {
        // Arrange
        Note note1 = new Note();
        note1.setId(1);
        note1.setTitle("Test Note 1");

        Note note2 = new Note();
        note2.setId(2);
        note2.setTitle("Test Note 2");

        when(noteService.getAllNotes()).thenReturn(List.of(note1, note2));

        // Act
        List<Note> notes = noteController.getAll();

        // Assert
        assertNotNull(notes);
        assertEquals(2, notes.size());
        assertEquals("Test Note 1", notes.get(0).getTitle());
        verify(noteService).getAllNotes();
    }

    @Test
    void testGetByIdFound() {
        // Arrange
        Note note = new Note();
        note.setId(1);
        note.setTitle("Test Note");

        when(noteService.getNoteById(1)).thenReturn(Optional.of(note));

        // Act
        ResponseEntity<Note> response = noteController.getById(1);

        // Assert
        assertNotNull(response);
        assertEquals(200,  response.getStatusCode().value());
        assertEquals("Test Note", response.getBody().getTitle());
        verify(noteService).getNoteById(1);
    }

    @Test
    void testGetByIdNotFound() {
        // Arrange
        when(noteService.getNoteById(2)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Note> response = noteController.getById(2);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value()); // Bad request
        verify(noteService).getNoteById(2);
    }

    @Test
    void testUpdateNoteSuccess() throws IllegalAccessException {
        // Arrange
        Note existingNote = new Note();
        existingNote.setId(1);
        existingNote.setTitle("Old Note");

        Note updatedNote = new Note();
        updatedNote.setId(1);
        updatedNote.setTitle("Updated Note");

        when(noteService.updateNote(1, updatedNote)).thenReturn(updatedNote);

        // Act
        ResponseEntity<Note> response = noteController.updateNote(1, updatedNote);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Updated Note", response.getBody().getTitle());
        verify(noteService, times(1)).updateNote(1, updatedNote);
    }

    @Test
    void testAddNoteSuccess() {
        // Arrange
        Note note = new Note();
        note.setTitle("New Note");

        Note savedNote = new Note();
        savedNote.setId(1);
        savedNote.setTitle("Unique New Note");

        when(noteService.generateUniqueTitle()).thenReturn("Unique New Note");
        when(noteService.saveNote(note)).thenReturn(savedNote);

        // Act
        ResponseEntity<Note> response = noteController.addNote(note);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Unique New Note", response.getBody().getTitle());
        verify(noteService ).generateUniqueTitle();
        verify(noteService).saveNote(note);
    }

    @Test
    void testDeleteNoteSuccess() throws IllegalAccessException {
        // Arrange
        when(noteService.noteExists(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = noteController.deleteNote(1);

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value()); // No content
        verify(noteService).noteExists(1);
        verify(noteService).deleteNote(1);
    }

    @Test
    void testDeleteNoteNotFound() throws IllegalAccessException {
        // Arrange
        when(noteService.noteExists(1)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = noteController.deleteNote(1);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value()); // Not found
        verify(noteService).noteExists(1);
        verify(noteService, never()).deleteNote(1);
    }
}
