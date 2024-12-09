package services;

import models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.repositories.NoteRepository;
import server.services.NoteServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note testNote;

    @BeforeEach
    void setUp() {
        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test");
        testNote.setContent("This is a test note.");
    }

    @Test
    void testGetAllNotes() {
        when(noteRepository.findAll()).thenReturn(List.of(testNote));

        List<Note> notes = noteService.getAllNotes();

        assertNotNull(notes);
        assertEquals(1, notes.size());
        assertEquals("Test", notes.get(0).getTitle());
        verify(noteRepository).findAll();
    }

    @Test
    void testGetNoteById() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        Optional<Note> note = noteService.getNoteById(1L);

        assertTrue(note.isPresent());
        assertEquals("Test", note.get().getTitle());
        verify(noteRepository).findById(1L);
    }

    @Test
    void testNoteExists() {
        when(noteRepository.existsById(1L)).thenReturn(true);

        boolean exists = noteService.noteExists(1L);
        assertTrue(exists);
        verify(noteRepository).existsById(1L);
    }

    @Test
    void testSaveNote() {
        when(noteRepository.save(testNote)).thenReturn(testNote);

        Note savedNote = noteService.saveNote(testNote);

        assertNotNull(savedNote);
        assertEquals("Test", savedNote.getTitle());
        verify(noteRepository, times(1)).save(testNote);
    }

    @Test
    void testSearchNotesByKeyword() {
        when(noteRepository.findByTitleOrContentContainingIgnoreCase("test"))
            .thenReturn(List.of(testNote));

        List<Note> notes = noteService.searchNotesByKeyword("test");

        assertNotNull(notes);
        assertEquals(1, notes.size());
        verify(noteRepository, times(1))
            .findByTitleOrContentContainingIgnoreCase("test");
    }

    @Test
    void testUpdateNote() throws IllegalAccessException {
        Note updatedNote = new Note();
        updatedNote.setContent("Updated content");

        when(noteRepository.existsById(1L)).thenReturn(true);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note result = noteService.updateNote(1L, updatedNote);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals("Test", result.getTitle()); // Title is unchanged
        verify(noteRepository, times(1)).save(testNote);
    }

    @Test
    void testGenerateUniqueTitle() {
        Note note1 = new Note();
        note1.setTitle("Untitled Note 1");

        Note note2 = new Note();
        note2.setTitle("Untitled Note 2");

        when(noteRepository.findAll()).thenReturn(List.of(note1, note2));

        String uniqueTitle = noteService.generateUniqueTitle();

        assertEquals("Untitled Note 3", uniqueTitle);
        verify(noteRepository, times(1)).findAll();
    }

    @Test
    void testDeleteNote() throws IllegalAccessException {
        when(noteRepository.existsById(1L)).thenReturn(true);

        noteService.deleteNote(1L);

        verify(noteRepository, times(1)).deleteById(1L);
    }
}
