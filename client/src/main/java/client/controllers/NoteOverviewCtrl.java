package client.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import client.event.EventBus;
import client.event.MainEventBus;
import client.event.NoteContentEvent;
import client.event.NoteEvent;
import client.managers.MarkdownRenderManager;
import client.managers.NoteListManager;
import client.managers.NoteManager;
import client.utils.DebounceService;
import client.utils.NoteService;
import com.google.inject.Inject;

import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebView;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import javafx.util.Pair;
import models.Collection;
import models.Note;


public class NoteOverviewCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NoteService noteService;
    private final DebounceService debounceService;
    private static final EventBus eventBus = MainEventBus.getInstance();

    @FXML
    private TextField noteTitle;

    @FXML
    private TextField searchBar;

    @FXML
    private ListView<Pair<Long, String>> notesList;

    @FXML
    private TextArea noteDisplay;

    @FXML
    private ScrollPane markdownPreview;

    @FXML
    private WebView markdownContent;

    @FXML
    private ComboBox<Pair<Long, String>> collectionDropdown;

    @FXML
    private Button addNoteButton, removeNoteButton, refreshNotesButton;

    private ObservableList<Pair<Long, String>>  notes; // pair of the note ID and note title
    // We don't want to store the whole note here since we only need to fetch the one that is currently selected.

    private final NoteManager noteManager;
    private final NoteListManager noteListManager;
    private  MarkdownRenderManager markdownRenderManager;

    private Runnable lastTask = null;

    private Long curNoteId = null;
    private Integer curNoteIndex = null;

    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, NoteService noteService, DebounceService debounceService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.noteService = noteService;
        this.debounceService = debounceService;
        this.noteManager = new NoteManager(noteService);
        this.noteListManager = new NoteListManager();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        markdownRenderManager = new MarkdownRenderManager(markdownContent,markdownPreview,mainCtrl);
        setupSearch();
        setupSelectCollection();
        handleCollectionSelectionChange();
        handleNoteTitleChanged();
        handleNoteSelectionChange();
        handleNoteContentChange();

        notesList.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            removeNoteButton.setDisable(newValue == null);
        });

        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        addNoteButton.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination("Ctrl+N"),
                        this::addNote
                );
            }
        });

        refreshNotesButton.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination("Ctrl+R"),
                        this::refreshNotes
                );
            }
        });

        removeNoteButton.sceneProperty().addListener((_, _, newScene) -> {
                if (newScene != null) {
                    newScene.getAccelerators().put(
                            KeyCombination.keyCombination("Ctrl+D"),
                            this::removeNote
                    );
                }
        });
    }

    private void handleNoteTitleChanged() {
        noteTitle.textProperty().addListener((_, _, newValue) -> {
            if (curNoteIndex == null || curNoteId == null) {
                return; // Ignore updates when no note is selected
            }
            eventBus.publish(new NoteContentEvent(NoteEvent.EventType.TITLE_CHANGE, newValue, curNoteId, curNoteIndex));
        });
    }


    private void handleNoteContentChange() {
        noteDisplay.textProperty().addListener((_, _, newValue) -> {
            eventBus.publish(new NoteContentEvent(NoteEvent.EventType.CONTENT_CHANGE, newValue, curNoteId, curNoteIndex));
        });
        removeNoteButton.setDisable(true);
    }

    private void handleNoteSelectionChange() {
        noteTitle.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) { // Focus lost on title
                handleTitleOnFocusLost();
            }
        });
        noteDisplay.focusedProperty().addListener((_, _, hasFocus) -> {});
        // Listener for switching notes (ensures deletion only happens when switching notes)
        notesList.getSelectionModel().selectedItemProperty().addListener((_, oldNote, newNote) -> {
            if (oldNote != null && !(noteTitle.isFocused() || noteDisplay.isFocused())) { // If switching notes and no fields are focused
                handleNoteSwitch();
            }
        });
        noteDisplay.setEditable(false);
        notesList.getSelectionModel().selectedItemProperty().addListener((_, oldNote, newNote) -> {
            updateContentAndTitle(oldNote, newNote);
        });
    }

    private void handleCollectionSelectionChange() {
        collectionDropdown.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) -> {
            if(newValue != null) {
                selectCollection(newValue);
            }
        });
    }

    private void updateContentAndTitle(Pair<Long, String> oldNote, Pair<Long, String> newNote) {
        if (newNote != null) {
            noteDisplay.setEditable(true);
            noteTitle.setEditable(true);
            debounceService.runTask(lastTask);
            curNoteId = newNote.getKey();
            curNoteIndex = notesList.getSelectionModel().getSelectedIndex();
            var newTitle = newNote.getValue();
            if(oldNote !=null && !Objects.equals(oldNote.getKey(), newNote.getKey())) {
                updateNoteTitle(newTitle);
            } else if(oldNote ==null){
                updateNoteTitle(newTitle);
            }
            var id = newNote.getKey();

            var content = server.getNoteContentByID(id);
            updateNoteDisplay(content);
        } else{
            noteDisplay.setEditable(false);
        }
    }

    private void setupSearch() {
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) { // Check if the Enter key was pressed
                searchNotes(); // Trigger the searchNotes method
            }
        });
    }

    /**
     * setup for 'select collection' menu.
     * adds 'All' collection, user collections and the edit collection
     */
    private void setupSelectCollection() {
        List<Collection> collections = server.getAllCollectionsFromServer();       //server.getAllCollections();

        Pair<Long, String> allNotesCollection = new Pair<>((long) -1, "All");

        List<Pair<Long, String>> listOfCollections = new ArrayList<>();
        for (Collection collection : collections) {
            listOfCollections.add(new Pair<>(collection.getId(), collection.getName()));
        }

        Pair<Long, String> editOption = new Pair<>((long)-2, "Edit Collections...");

        collectionDropdown.getItems().clear();
        collectionDropdown.getItems().add(allNotesCollection);
        collectionDropdown.getItems().addAll(FXCollections.observableArrayList(listOfCollections));
        collectionDropdown.getItems().add(editOption);
        collectionDropdown.setValue(allNotesCollection);

        collectionDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getValue()); // Display name of collection in collection options
                }
            }
        });
        collectionDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getValue()); // Display name of collection in selected collection box
                }
            }
        });
    }

    private void handleTitleOnFocusLost() {
        if (curNoteId != null) {
            try {
                Note updatedNote = new Note();
                updatedNote.setTitle(noteTitle.getText());
                updatedNote.setContent(noteDisplay.getText());
                server.updateNoteByID(curNoteId, updatedNote);
            } catch (Exception e) {
                System.out.println("Failed to update note on focus loss: " + e.getMessage());
            }
            refreshNotes();
        }
    }

    /**
     * Handles logic for when the user leaves a note (e.g., switches to a different note).
     */
    private void handleNoteSwitch() {
        // Do nothing if either field still has focus
        if (noteTitle.isFocused() || noteDisplay.isFocused()) {
            return;
        }
        if (curNoteId != null) {
            String currentTitle = noteTitle.getText() != null ? noteTitle.getText().trim() : "";
            String currentContent = noteDisplay.getText() != null ? noteDisplay.getText().trim() : "";
            if (currentTitle.isEmpty() && currentContent.isEmpty()) {
                try {
                    var selectedNote = notesList.getSelectionModel().getSelectedItem();
                    server.deleteNoteByID(curNoteId);
                    notesList.getItems().remove(selectedNote);
                    curNoteId = null;
                    refreshNotes();
                } catch (Exception e) {
                    System.out.println("Failed to delete empty note: " + e.getMessage());
                }
            } else {
                // Save changes to the note
                try {
                    Note updatedNote = new Note();
                    updatedNote.setTitle(noteTitle.getText());
                    updatedNote.setContent(noteDisplay.getText());
                    server.updateNoteByID(curNoteId, updatedNote);
                } catch (Exception e) {
                    System.out.println("Failed to update note on note switch: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Method to set the noteTitle to given parameter
     * @param note the content to set the title to
     * This method only fills out the client fields. It does not communicate with the server in order to save the title.
     */
    private void updateNoteTitle(String note) {
        noteTitle.setText(note);
    }

    /**
     * Method to set the noteDisplay to given parameter
     * @param note the content to set the display to
     * This method only fills out the client fields. It does not communicate with the server in order to save the content.
     */
    private void updateNoteDisplay(String note) {
        noteDisplay.setText(note);
    }

    /**
     * Method that refreshes the notes.
     */
    public void refreshNotes() {
        System.out.println("Refreshed the note list");
        Pair<Long, String> selectedNote = notesList.getSelectionModel().getSelectedItem();
        Long selectedNoteId = (selectedNote != null) ? selectedNote.getKey() : null;
        List<Pair<Long, String>> notesAsPairs = new ArrayList<>();

        Pair<Long, String> selectedCollection = collectionDropdown.getSelectionModel().getSelectedItem();

        if(selectedCollection.getKey() <= -1) {
            var notesFromServer = server.getNoteTitles();
            for(var row: notesFromServer){
                Long id = ((Integer)row[0]).longValue();
                String title = (String)row[1];
                notesAsPairs.add(new Pair<>(id, title));
            }
        } else {
            var collectionNotes = server.getNotesByCollectionId(selectedCollection.getKey());
            for(var note: collectionNotes){
                notesAsPairs.add( new Pair<>(note.getId(), note.getTitle()));
            }
        }
        notes = FXCollections.observableArrayList(notesAsPairs);
        noteListManager.setNotes(notes);
        notesList.setItems(notes);
        //Since the list is of the pairs, and they are not really observable objects (They do not implement Observable)
        //We have to change the list to only display the note title (The code is strongly from the internet)
        notesList.setCellFactory(_ -> new ListCell<>() { //The cell factory is responsible for rendering the data contained
            // within each TableCell for a single table column.
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) { //updateItem is called whenever a cell needs to be updated
                super.updateItem(item, empty); //supposedly its necessary from what I understand the updateItem
                // needs to call its parents class in order to do all the basic checks
                if (item == null || empty) {
                    setText(null); //If the item is empty then set the text to null (Shouldn't happen I think)
                } else {
                    setText(item.getValue());  // Display only the title (second value of the pair)
                }
            }
        });
        // Re-select the previously selected note (if it exists)
        if (selectedNoteId != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getKey().equals(selectedNoteId)) {
                    notesList.getSelectionModel().select(i); // Select the note by index
                    break;
                }
            }
        } else {
            notesList.getSelectionModel().clearSelection(); // Clear selection if no valid note
        }
    }
    /**
     * Method to add notes
     */
    public void addNote(){
        System.out.println("Adding a new note");
        server.addNote();
        refreshNotes();
    }

    @FXML
    private void searchNotes() {
        String query = searchBar.getText();
        System.out.println(query);
        Pair<Long, String> selectedNote = notesList.getSelectionModel().getSelectedItem();
        Long selectedNoteId = (selectedNote != null) ? selectedNote.getKey() : null;
        List<Pair<Long, String>> notesAsPairs = new ArrayList<>();

        Pair<Long, String> selectedCollection = collectionDropdown.getSelectionModel().getSelectedItem();
        List<Note> selectedCollectionNotes = server.getNotesByCollectionId(selectedCollection.getKey());
        List<Long> selectedCollectionNoteIds = new ArrayList<>();
        for(Note note: selectedCollectionNotes){
            selectedCollectionNoteIds.add(note.getId());
        }

        if(selectedCollection.getKey() <= -1) {
        var notesFromServer = server.searchNotes(query);
        for(var row: notesFromServer){
            Long id = ((Integer)row[0]).longValue();
            String title = (String)row[1];
            notesAsPairs.add(new Pair<>(id, title));
            }
        } else {
            var collectionNotes = server.getNotesByCollectionId(selectedCollection.getKey());
            for(var note: collectionNotes){
                notesAsPairs.add( new Pair<>(note.getId(), note.getTitle()));
            }
        }
        notes = FXCollections.observableArrayList(notesAsPairs);
        noteListManager.setNotes(notes);
        notesList.setItems(notes);
        //Since the list is of the pairs, and they are not really observable objects (They do not implement Observable)
        //We have to change the list to only display the note title (The code is strongly from the internet)
        notesList.setCellFactory(_ -> new ListCell<>() { //The cell factory is responsible for rendering the data contained
            // within each TableCell for a single table column.
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) { //updateItem is called whenever a cell needs to be updated
                super.updateItem(item, empty); //supposedly its necessary from what I understand the updateItem
                // needs to call its parents class in order to do all the basic checks
                if (item == null || empty) {
                    setText(null); //If the item is empty then set the text to null (Shouldn't happen I think)
                } else {
                    setText(item.getValue());  // Display only the title (second value of the pair)
                }
            }
        });
        // Re-select the previously selected note (if it exists)
        if (selectedNoteId != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getKey().equals(selectedNoteId)) {
                    notesList.getSelectionModel().select(i); // Select the note by index
                    break;
                }
            }
        } else {
            notesList.getSelectionModel().clearSelection(); // Clear selection if no valid note
        }
    }


    /**
     * action button for dropdown menu
     */
    @FXML
    private void selectCollection(Pair<Long, String> selectedCollection) {
        System.out.println("selected collection: " + selectedCollection.getValue());
        List<Pair<Long, String>> collectionNotes = new ArrayList<>();

        int choice = Math.toIntExact(selectedCollection.getKey());
        switch(choice) {
            case 0:
                return;
            case -1:
                List<Note> serverNotes = server.getAllNotesFromServer();
                for(Note note: serverNotes){
                    collectionNotes.add(new Pair<>(note.getId(), note.getTitle()));
                }
                notes = FXCollections.observableArrayList(collectionNotes);
                noteListManager.setNotes(notes);
                notesList.setItems(notes);
                refreshNotes();
                break;
            case -2:
                //showEditCollectionsScreen();
                refreshNotes();
                break;
            default:
                List<Note> notesInCollection = server.getNotesByCollectionId(selectedCollection.getKey());
                for(Note note: notesInCollection){
                    collectionNotes.add(new Pair<>(note.getId(), note.getTitle()));
                }
                notes = FXCollections.observableArrayList(collectionNotes);
                noteListManager.setNotes(notes);
                notesList.setItems(notes);
                refreshNotes();
        }
    }

    /**
     * Method to remove notes form UI
     */
    @FXML
    private void removeNote() {
        var selectedNote = notesList.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm deletion");
            alert.setHeaderText("Are you sure you want to delete this note?");
            alert.setContentText("You are trying to delete note: " + selectedNote.getValue() + "." +
                    "\nDeleting a note is irreversible!");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    server.deleteNoteByID(selectedNote.getKey());
                    notesList.getItems().remove(selectedNote);
                    // Handle the case when the list is empty
                    if (notesList.getItems().isEmpty()) {
                        curNoteId = null;
                        curNoteIndex = null;
                        updateNoteTitle(""); // Clear title field
                        updateNoteDisplay(""); // Clear content field
                    } else {
                        // Select the first note in the list
                        notesList.getSelectionModel().select(0);
                    }
                } catch (Exception e) {
                    System.out.println("Error while deleting note: " + e.getMessage());
                }

            } else {
                System.out.println("Deletion canceled.");
            }
        }
    }


}