package client.controllers;

import client.DialogFactory;
import client.event.EventBus;
import client.event.NoteEvent;
import client.event.NoteStatusEvent;
import client.managers.NoteListManager;
import client.utils.DebounceService;
import client.utils.NoteService;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteOverviewCtrlTest extends ApplicationTest {

    @Mock
    private ServerUtils serverUtils;
    @Mock
    private NoteService noteService;
    @Mock
    private DebounceService debounceService;
    @Mock
    private NoteListManager noteListManager;
    @Mock
    private DialogFactory dialogFactory;
    @Mock
    private EventBus mockEventBus;

    private NoteOverviewCtrl noteOverviewCtrl;

    private ListView<Pair<Long, String>> notesList;
    private ComboBox<Pair<Long, String>> collectionDropdown;
    private TextField noteTitle;
    private TextArea noteDisplay;
    private TextField searchBar;

    @BeforeEach
    void checkHeadlessEnvironment() {
        if (System.getProperty("java.awt.headless", "false").equals("true")) {
            Assumptions.assumeTrue(false, "Skipping tests in headless environment");
        }
    }
    @BeforeEach
    void setUp() {
        // Mock dependencies
        serverUtils = mock(ServerUtils.class);
        noteService = mock(NoteService.class);
        debounceService = mock(DebounceService.class);
        noteListManager = mock(NoteListManager.class);
        dialogFactory = mock(DialogFactory.class);
        mockEventBus = mock(EventBus.class);

        // Create NoteOverviewCtrl and inject mocked dependencies
        noteOverviewCtrl = new NoteOverviewCtrl(
                serverUtils,
                mock(MainCtrl.class),
                noteService,
                debounceService,
                dialogFactory,
                mockEventBus
        );

        noteOverviewCtrl.setNoteListManager(noteListManager);

        // Initialize UI components
        notesList = new ListView<>();
        collectionDropdown = new ComboBox<>();
        noteTitle = new TextField();
        noteDisplay = new TextArea();
        searchBar = new TextField();

        noteOverviewCtrl.setNotesList(notesList);
        noteOverviewCtrl.setCollectionDropdown(collectionDropdown);
        noteOverviewCtrl.setNoteTitle(noteTitle);
        noteOverviewCtrl.setNoteDisplay(noteDisplay);
        noteOverviewCtrl.setSearchBar(searchBar);

        mockDefaultData();
    }

    private void mockDefaultData() {
        // Mock data for getNoteTitles()
        when(serverUtils.getNoteTitles()).thenReturn(List.of(
                new Object[]{1L, "Note 1"},
                new Object[]{2L, "Note 2"}
        ));

        // Mock data for collectionDropdown
        collectionDropdown.setItems(FXCollections.observableArrayList(
                new Pair<>(-1L, "All Notes"),
                new Pair<>(1L, "Collection 1"),
                new Pair<>(2L, "Collection 2")
        ));
        collectionDropdown.setValue(collectionDropdown.getItems().get(0)); // Set default value
    }

    @Test
    void testAddNote() {
        noteOverviewCtrl.addNote();

        verify(serverUtils, times(1)).getNoteTitles();
        verify(noteListManager, times(1)).setNotes(any(ObservableList.class));
        assertNotNull(noteOverviewCtrl.getNotes());
        assertEquals(2, noteOverviewCtrl.getNotes().size());
    }

    @Test
    void testUpdateTitle() {
        // Mock initial note titles
        List<Object[]> initialNoteTitles = List.of(
                new Object[]{1L, "Note 1"},
                new Object[]{2L, "Note 2"}
        );
        when(serverUtils.getNoteTitles()).thenReturn(initialNoteTitles);

        Platform.runLater(() -> {
            noteOverviewCtrl.refreshNotes(); // Populate the notes list
            noteOverviewCtrl.setCurNoteId(1L); // Simulate selecting "Note 1"
            noteOverviewCtrl.setCurNoteIndex(0); // Index of "Note 1"
            noteTitle.setText("Updated Note Title");
        });

        when(noteService.titleExists("Updated Note Title")).thenReturn(false);

        // Mock the behavior of updating the note title
        doAnswer(invocation -> {
            when(serverUtils.getNoteTitles()).thenReturn(List.of(
                    new Object[]{1L, "Updated Note Title"}, // Reflect the updated title
                    new Object[]{2L, "Note 2"}
            ));
            return null;
        }).when(noteService).updateNoteTitle("Updated Note Title", 1L);

        // Perform the title update
        Platform.runLater(noteOverviewCtrl::updateTitle);

        // Verify the updated title is reflected in the notes
        WaitForAsyncUtils.waitForFxEvents(); // Ensure JavaFX updates are processed
        assertEquals("Updated Note Title", noteTitle.getText());
    }

    @Test
    void testRefreshNotes() {
        noteOverviewCtrl.refreshNotes();

        verify(serverUtils, times(1)).getNoteTitles();
        verify(noteListManager, times(1)).setNotes(any(ObservableList.class));
        assertNotNull(notesList.getItems());
        assertEquals(2, notesList.getItems().size());
    }

    @Test
    void testRemoveNote() {
        Platform.runLater(() -> {
            ObservableList<Pair<Long, String>> mockNotes = FXCollections.observableArrayList(
                    new Pair<>(1L, "Note 1"),
                    new Pair<>(2L, "Note 2")
            );

            noteOverviewCtrl.setNotes(mockNotes);
            notesList.setItems(mockNotes);
            notesList.getSelectionModel().select(0); // Select "Note 1"

            when(dialogFactory.createConfirmationDialog(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.of(ButtonType.OK));

            noteOverviewCtrl.removeNote();
        });

        WaitForAsyncUtils.waitForFxEvents();

        // Verify the event is published
        verify(mockEventBus, times(1)).publish(
                argThat(event -> event instanceof NoteStatusEvent
                        && ((NoteStatusEvent) event).getEventType() == NoteEvent.EventType.NOTE_REMOVE
                        && ((NoteStatusEvent) event).getChangeID() == 1L)
        );
    }

    @Test
    void testSearchNotes() {
        when(serverUtils.searchNotes("Note")).thenReturn(List.of(
                new Object[]{1L, "Note 1"},
                new Object[]{3L, "Note 3"}
        ));
        searchBar.setText("Note");
        noteOverviewCtrl.searchNotes();

        verify(serverUtils, times(1)).searchNotes("Note");
        assertEquals(2, notesList.getItems().size());
        assertEquals("Note 1", notesList.getItems().get(0).getValue());
        assertEquals("Note 3", notesList.getItems().get(1).getValue());
    }
}