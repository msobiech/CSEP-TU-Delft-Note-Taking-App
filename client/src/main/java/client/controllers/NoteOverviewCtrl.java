package client.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    private Button addNoteButton, removeNoteButton, refreshNotesButton, editTitleButton;

    private ObservableList<Pair<Long, String>>  notes; // pair of the note ID and note title
    // We don't want to store the whole note here since we only need to fetch the one that is currently selected.

    private final Parser markdownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();
    private Timer debounceTimer = new Timer();

    private int changeCountContent = 0;
    private int changeCountTitle = 0;
    private final int THRESHOLD = 5;
    private final int DELAY = 1000;
    private Runnable lastTask = null;

    private Long curNoteId = null;
    private Integer curNoteIndex = null;

    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, NoteService noteService, DebounceService debounceService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.noteService = noteService;
        this.debounceService = debounceService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        noteTitle.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) { // Trigger only on Enter key press
                updateTitle();
            }
        });
    }

    @FXML
    private void updateTitle() {
        if (curNoteId == null || curNoteIndex == null) {
            return; // No note is currently selected
        }
        String newTitle = noteTitle.getText().trim();
        if (newTitle.isEmpty()) {
            newTitle = noteService.generateUniqueTitle();
            noteTitle.setText(newTitle);
        }
        try {
            if (noteService.titleExists(newTitle)){
                if (!newTitle.equals(notes.get(curNoteIndex).getValue())) {
                    mainCtrl.showError("This title is already in use. Please choose a different title.");
                }
                return;
            }
            notes.set(curNoteIndex, new Pair<>(curNoteId, newTitle));
            noteService.updateNoteTitle(newTitle, curNoteId);
            refreshNotes();
            System.out.println("Title updated successfully!");
        } catch (Exception e) {
            mainCtrl.showError("Failed to update the title. Please try again.");
        }
    }

    private void handleNoteContentChange() {
        noteDisplay.textProperty().addListener((_, _, newValue) -> {
            changeCountContent++; // Count the amount of changes
            try {
                renderMarkdown(newValue);
            } catch (InterruptedException e) {
                mainCtrl.showError(e.toString());
            }


            debounce(() -> {
                if (curNoteId != null) {
                    try {
                        noteService.updateNoteContent(newValue, curNoteId);
                        changeCountContent = 0;
                    } catch (Exception e) {
                        System.out.println("Failed to update content: " + e.getMessage());
                    }
                }
            }, DELAY);
            if (changeCountContent >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
                if (curNoteId != null) {
                    try {
                        noteService.updateNoteContent(newValue, curNoteId);
                        changeCountContent = 0;
                        debounceTimer.cancel(); // Cancel any pending debounced update
                    } catch (Exception e) {
                        System.out.println("Failed to update content: " + e.getMessage());
                    }
                }
            }
        });
        removeNoteButton.setDisable(true);
    }

        private void handleNoteSelectionChange() {
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

            // Update the currently selected note's ID and index
            curNoteId = newNote.getKey();
            curNoteIndex = notesList.getSelectionModel().getSelectedIndex();

            // Display the title and content of the newly selected note
            noteTitle.setText(newNote.getValue());
            var id = newNote.getKey();
            var content = server.getNoteContentByID(id);

            // Display content and render markdown
            updateNoteDisplay(content);
            try {
                renderMarkdown(content);
            } catch (InterruptedException e) {
                mainCtrl.showError(e.toString());
            }
        } else {
            // Disable editing when no note is selected
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
                // Only update the content; title updates are handled by handleTitleUpdate
                try {
                    Note updatedNote = new Note();
                    updatedNote.setContent(currentContent);
                    server.updateNoteByID(curNoteId, updatedNote);
                } catch (Exception e) {
                    System.out.println("Failed to update note content on note switch: " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method allows you to schedule a task that will be executed in the given delay
     * @param task to schedule
     * @param delayMillis delay which we wait
     * Nice explanation of the concept <a href="https://www.geeksforgeeks.org/debouncing-in-javascript/">...</a>
     */
    private void debounce(Runnable task, int delayMillis) {
        debounceTimer.cancel(); // If the previously scheduled task is still there it means that the inactivity wasn't long enough and we can cancel.
        debounceTimer = new Timer(); // Setup new timer

        lastTask = task;

        debounceTimer.schedule(new TimerTask() { // Schedule new task TimerTask will schedule the task on your timer and execute in a given time
            @Override
            public void run() {
                Platform.runLater(task); // Run the task on a platform thread
            }
        }, delayMillis);
    }


    private void renderMarkdown(String markdownText) throws InterruptedException {

        String cssFile = null;
        String htmlContent = null;
        //Used markdown style is from here https://github.com/sindresorhus/github-markdown-css
        try {
            cssFile = Files.readString(Path.of(getClass().getResource("markdownStyle.css").toURI()));
            htmlContent = "<style>" + cssFile + "</style><article class=\"markdown-body\">";
        } catch (IOException | URISyntaxException e) {
            htmlContent = "<style> body { color-scheme: light;" +
                    "font-family: -apple-system,BlinkMacSystemFont,\"Segoe UI\"," +
                    "\"Noto Sans\",Helvetica,Arial,sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\";" +
                    "font-size: 16px; line-height: 1.5; word-wrap: break-word; }" +
                    "blockquote { margin: 20px 0; padding: 10px 20px;" +
                    "border-left: 5px solid #ccc; background-color: #f9f9f9; font-style: italic; color: #555; }" +
                    "table { width: 50%; border-collapse: collapse;}" +
                    "table, th, td { border: 1px solid #333; }" +
                    "th, td { padding: 8px; text-align: left; }" +
                    "th { background-color: #f2f2f2; } </style>";
        }
        htmlContent += htmlRenderer.render(markdownParser.parse(markdownText));
        markdownContent.getEngine().loadContent(htmlContent);
        markdownContent.setPrefHeight(markdownPreview.getHeight());
        markdownContent.setPrefWidth(markdownPreview.getWidth());
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