package client.controllers;


import client.DialogFactory;
import client.WebSockets.GlobalWebSocketManager;
import client.WebSockets.WebSocketMessageListener;
import client.event.*;
import client.managers.*;
import client.utils.DebounceService;
import client.utils.NoteService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import models.Collection;
import models.EmbeddedFile;
import models.Note;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.tika.mime.*;


public class NoteOverviewCtrl implements Initializable, WebSocketMessageListener {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NoteService noteService;
    private final DebounceService debounceService;
    private final DialogFactory dialogFactory;
    private final EventBus eventBus;
    public Button showShortcutsButton;

    @FXML
    private ComboBox<Pair<String, String>> flagDropdown;

    @FXML
    private Label collectionText;

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
    private HBox fileListContainer;

    @FXML
    private ComboBox<Pair<Long, String>> collectionDropdown;

    @FXML
    private ComboBox<Pair<Long, String>> noteCollectionDropdown;

    @FXML
    private Button addNoteButton, removeNoteButton, refreshNotesButton, editTitleButton;

    @FXML
    private void toggleMode() {
        mainCtrl.toggleMode();
    }


    private ObservableList<Pair<Long, String>>  notes; // pair of the note ID and note title
    // We don't want to store the whole note here since we only need to fetch the one that is currently selected.

    private NoteManager noteManager;
    private NoteListManager noteListManager;
    private MarkdownRenderManager markdownRenderManager;
    private LanguageManager languageManager;

    private ResourceBundle language;

    private Runnable lastTask = null;

    private Long curNoteId = null;
    private Integer curNoteIndex = null;

    Map<String, String> aliases = new HashMap<>();

    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, NoteService noteService,
                            DebounceService debounceService, DialogFactory dialogFactory, EventBus eventbus) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.noteService = noteService;
        this.debounceService = debounceService;
        this.dialogFactory = dialogFactory;
        this.eventBus = eventbus;
        GlobalWebSocketManager.getInstance().addMessageListener(this);
        Platform.runLater(() -> new KeyEventManager(eventBus, searchBar));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        markdownRenderManager = new MarkdownRenderManager(markdownContent,markdownPreview,mainCtrl);
        noteManager = new NoteManager(noteService, server);
        noteListManager = new NoteListManager(notesList);
        languageManager = new LanguageManager();
        language = ResourceBundle.getBundle("client.controllers.language", LanguageManager.getLanguage());
        setupSearch();
        setupSelectCollection();

        setupNoteCollectionDropdown();
        handleNoteCollectionChange();

        handleCollectionSelectionChange();
        setupLanguageDropdown();
        handleNoteTitleChanged();
        handleNoteSelectionChange();
        handleNoteContentChange();
        handleLanguageChange();
        handleEscapeKeyPressed();
        handleNoteNavigation();

        notesList.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            removeNoteButton.setDisable(newValue == null);
        });

        handleEditCollectionsPressed();
        setupKeyboardShortcuts();
    }

    private void handleNoteNavigation() {
        boolean useCommand = isMacOS(); // Check if the app is running on macOS

        Platform.runLater(() -> {
            noteDisplay.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                if ((useCommand && event.isMetaDown()) || (!useCommand && event.isControlDown())) {
                    switch (event.getCode()) {
                        case UP:
                            eventBus.publish(new NoteNavigationEvent(NoteNavigationEvent.Direction.PREVIOUS));
                            event.consume();
                            break;
                        case DOWN:
                            eventBus.publish(new NoteNavigationEvent(NoteNavigationEvent.Direction.NEXT));
                            event.consume();
                            break;
                        default:
                            break;
                    }
                }
            });
        });
    }

    private void handleEscapeKeyPressed() {
        Platform.runLater(() -> {
            searchBar.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    eventBus.publish(new EscapeKeyEvent()); // Publish the ESC key event
                    event.consume(); // Prevent further propagation
                }
            });
        });
    }

    private void handleLanguageChange() {
        flagDropdown.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            System.out.println("Language changed to " +  newValue.getKey());
            eventBus.publish(new LanguageEvent(newValue.getKey()));
        });
    }

    private void handleEditCollectionsPressed() {
        collectionDropdown.setOnAction(event -> {
            Pair<Long, String> selectedOption = collectionDropdown.getValue();
            if ("Edit Collections...".equals(selectedOption.getValue())) {
                mainCtrl.showEditCollections(); // Call the method to show the popup
            }
        });
    }

    public void setupNoteCollectionDropdown() {
        List<Collection> collections = server.getAllCollectionsFromServer();
        List<Pair<Long, String>> listOfCollections = new ArrayList<>();
        for (Collection collection : collections) {
            listOfCollections.add(new Pair<>(collection.getId(), collection.getName()));
        }
        noteCollectionDropdown.setItems(FXCollections.observableList(listOfCollections));


        // Configure how items are displayed in the dropdown
        noteCollectionDropdown.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getValue());
            }
        });

        // Configure the button cell to display the selected collection
        noteCollectionDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getValue());
            }
        });
        notesList.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) -> {
            if (newValue != null) {
                // Step 3: Fetch the collection of the newly selected note
                Collection curNoteCollection = server.getCollectionByNoteID(newValue.getKey());
                if (curNoteCollection != null) {
                    // Find the corresponding Pair in the ComboBox items
                    Pair<Long, String> matchingCollection = noteCollectionDropdown.getItems().stream()
                            .filter(pair -> pair.getKey().equals(curNoteCollection.getId()))
                            .findFirst()
                            .orElse(null);

                    // Step 4: Update the ComboBox's value
                    noteCollectionDropdown.setValue(matchingCollection);
                    if(oldValue!=null) {
                        refreshNotes();
                        //refreshFiles();
                    }
                }
            }
        });
    }

    public void handleNoteCollectionChange() {
        noteCollectionDropdown.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) -> {
            if(newValue != null && oldValue != null && newValue != oldValue) {
                selectNewCollection(oldValue, newValue);
            }
        });
    }


    private void setupLanguageDropdown() {

        Locale currentLanguage = null;
        try {
            FileInputStream languageSetting = new FileInputStream("settings.ser");
            ObjectInputStream in = new ObjectInputStream(languageSetting);
            currentLanguage = (Locale)in.readObject();
        } catch (Exception e) {
            currentLanguage = new Locale("en");
        }

        System.out.println("Currently chosen language : " + currentLanguage.getLanguage());

        ObservableList<Pair<String,String>> flags = FXCollections.observableArrayList(
                new Pair<>("en", "flags/uk_flag.png"),
                new Pair<>("nl", "flags/nl_flag.png"),
                new Pair<>("pl", "flags/pl_flag.png"),
                new Pair<>("it", "flags/it_flag.png"),
                new Pair<>("ro", "flags/ro_flag.png")
        );
        flagDropdown.setItems(flags);


        for(var country:flags){
            if(country.getKey().equals(currentLanguage.getLanguage())){
                flagDropdown.getSelectionModel().select(country);
            }
        }
        //flagDropdown.getSelectionModel().select(0);
        //eventBus.publish(new LanguageEvent(flagDropdown.getSelectionModel().getSelectedItem().getKey()));

        flagDropdown.setCellFactory(_ -> createFlagCell());
        flagDropdown.setButtonCell(createFlagCell());

    }

    private ListCell<Pair<String, String>> createFlagCell() {
        return new ListCell<>() {
            private final ImageView flagImage = new ImageView();
            @Override
            public void updateItem(Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    updateFlagCell(this, flagImage, item.getValue());
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }

    private void updateFlagCell(ListCell<Pair<String, String>> cell, ImageView flagImage, String path) {
        Image tmp = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        flagImage.setImage(tmp);
        flagImage.setFitHeight(20); flagImage.setFitWidth(20); flagImage.setPreserveRatio(true);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(4,4,4,4)); cell.setGraphic(flagImage); cell.setText(null);
    }

    private boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    private void setupKeyboardShortcuts() {
        // Determine the appropriate modifier key based on the OS
        String modifierKey = isMacOS() ? "Meta" : "Ctrl"; // Use "Meta" for Command on macOS, "Ctrl" otherwise
        addNoteButton.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination(modifierKey + "+N"),
                        this::addNote
                );
            }
        });

        refreshNotesButton.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination(modifierKey + "+R"),
                        this::refreshNotes
                );
            }
        });

        removeNoteButton.sceneProperty().addListener((_, _, newScene) -> {
                if (newScene != null) {
                    newScene.getAccelerators().put(
                            KeyCombination.keyCombination(modifierKey + "+D"),
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
            //eventBus.publish(new NoteContentEvent(NoteEvent.EventType.TITLE_CHANGE, newValue, curNoteId, curNoteIndex));
        });
    }

    @FXML
    void updateTitle() {
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
            eventBus.publish(new NoteContentEvent(NoteEvent.EventType.CONTENT_CHANGE, newValue, curNoteId, curNoteIndex, aliases));
        });
        removeNoteButton.setDisable(true);
    }

    void handleNoteSelectionChange() {
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
            refreshFiles();
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

        Pair<Long, String> allNotesCollection = new Pair<>((long) -1, language.getString("collections.all.text"));

        List<Pair<Long, String>> listOfCollections = new ArrayList<>();
        for (Collection collection : collections) {
            listOfCollections.add(new Pair<>(collection.getId(), collection.getName()));
        }

        Pair<Long, String> editOption = new Pair<>((long)-2, language.getString("collections.edit.text"));

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

        if (noteListManager == null) {
            System.err.println("NoteListManager is not initialized. Skipping refresh.");
            return;
        }
        Pair<Long, String> selectedNote = notesList.getSelectionModel().getSelectedItem();
        Long selectedNoteId = (selectedNote != null) ? selectedNote.getKey() : null;
        List<Pair<Long, String>> notesAsPairs = new ArrayList<>();

        Pair<Long, String> selectedCollection = collectionDropdown.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            System.err.println("No collection selected. Skipping refresh.");
            return;
        }

        // Fetch notes from the server based on the selected collection
        if (selectedCollection.getKey() <= -1) { // All Notes
            var notesFromServer = server.getNoteTitles();
            for (var row : notesFromServer) {
                Long id = ((Number) row[0]).longValue();
                String title = (String) row[1];
                notesAsPairs.add(new Pair<>(id, title));
            }
        } else { // Specific Collection
            var collectionNotes = server.getNotesByCollectionId(selectedCollection.getKey());
            for (var note : collectionNotes) {
                notesAsPairs.add(new Pair<>(note.getId(), note.getTitle()));
            }
        }

        // Update the observable list
        notes = FXCollections.observableArrayList(notesAsPairs);

        // Synchronize the note list manager with the updated notes
        noteListManager.setNotes(notes);

        // Update the ListView with the refreshed notes
        notesList.setItems(notes);

        // Update the ListView's cell factory to display only the note titles
        notesList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<Long, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getValue()); // Display the title only
                }
            }
        });

        // Re-select the previously selected note, if it still exists
        if (selectedNoteId != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getKey().equals(selectedNoteId)) {
                    notesList.getSelectionModel().select(i); // Re-select the note by index
                    break;
                }
            }
        } else {
            notesList.getSelectionModel().clearSelection(); // Clear selection if no valid note
        }
    }

    public void refreshCollections() {

    }

    /**
     * Method to add notes
     */
    public void addNote(){
        System.out.println("Adding a new note");
        eventBus.publish(new NoteStatusEvent(NoteEvent.EventType.NOTE_ADD, null));
        refreshNotes();
    }

    @FXML
    void searchNotes() {
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
            Long id = ((Number) row[0]).longValue();
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

    @FXML
    private void selectNewCollection(Pair<Long, String> oldCollection, Pair<Long, String> newCollection) {
        Note curNote = new Note();
        if(curNoteId != null) {
            curNote = server.getNoteByID(curNoteId);
        }
        System.out.println("Note: " + curNote.getTitle() +
                "\nGot removed from collection: " + oldCollection.getValue() +
                "\nGot added to collection: " + newCollection.getValue());

        Collection removedNoteCollection = server.getCollectionByID(oldCollection.getKey());
        removedNoteCollection.removeNoteFromCollection(curNote);
        server.updateCollectionByID(oldCollection.getKey(), removedNoteCollection);

        Collection addedNoteCollection = server.getCollectionByID(newCollection.getKey());
        addedNoteCollection.addNoteToCollection(curNote);
        server.updateCollectionByID(newCollection.getKey(), addedNoteCollection);
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
    void removeNote() {
        var selectedNote = notesList.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            Optional<ButtonType> result = dialogFactory.createConfirmationDialog(
                    "Confirm deletion",
                    "Are you sure you want to delete this note?",
                    "You are trying to delete note: " + selectedNote.getValue() + ".\nDeleting a note is irreversible!"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Publish the deletion event
                eventBus.publish(new NoteStatusEvent(NoteEvent.EventType.NOTE_REMOVE, null, selectedNote.getKey(), null));
            } else {
                System.out.println("Deletion canceled.");
            }
        }
    }

    public void showShortcutsPopUp(ActionEvent actionEvent) {
        mainCtrl.showShortcuts();
    }

    private void refreshFiles(){
        var noteToSearch = notesList.getSelectionModel().getSelectedItem();
        var noteListIndex = notesList.getSelectionModel().getSelectedIndex();
        if(noteToSearch==null){
            return;
        }
        Long idToSearch = noteToSearch.getKey();

        System.out.println("Refreshing files for note " + idToSearch);
        List<EmbeddedFile> files = server.getFilesForNote(idToSearch);
        fileListContainer.getChildren().clear();
        aliases.clear();
        for(var file:files){
            fileListContainer.getChildren().add(createFileBox(file.getFileName(),file.getId(), file.getFileType(), idToSearch));
            String path = ServerUtils.getSERVER()+"files/"+idToSearch+"/"+file.getId()+"/download";
            String fileName = file.getFileName();
            if(file.getFileType().contains("image")){
                aliases.put(fileName,path);
            }
        }
        eventBus.publish(new NoteContentEvent(NoteEvent.EventType.CONTENT_CHANGE, noteDisplay.getText(), noteToSearch.getKey(), noteListIndex, aliases));
    }



    public void AddFile() throws IOException {
        if(curNoteId==null){
            return;
        }
        System.out.println("Adding a file");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"); // For now we allow all types of files
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Choose file to save");
        Stage curStage = (Stage) noteDisplay.getScene().getWindow();
        File file = fileChooser.showOpenDialog(curStage);

        if (file == null) {
            System.out.println("No file was chosen");
            return;
        } else{
            System.out.println("File selected: " + file.getAbsolutePath());
            URLConnection connection = file.toURL().openConnection();
            String mimeType = connection.getContentType();
            Note curNote = server.getNoteByID(curNoteId);
            EmbeddedFile EmbFile = new EmbeddedFile(EmbeddedFile.getNameWithoutExtension(file.getName()), mimeType, Files.readAllBytes(file.toPath()), curNote);
            System.out.println("Adding a file " + file.getName() + " to Note " + curNote.getId());
            server.addFile(EmbFile);
            refreshFiles();
        }

    }

    private void saveEdit(Label label, TextField textField, HBox box, int indexOfChange) {
        if(!textField.getText().isEmpty()){
            label.setText(textField.getText().trim().substring(0, Math.min(textField.getText().length(), 249)));
        }
        if(indexOfChange!=-1){
            box.getChildren().set(indexOfChange, label);
        }

    }

    private void handleEditAlias(HBox file, Label fileLabel) {
        int labelIndex = file.getChildren().indexOf(fileLabel);
        if (labelIndex == -1) {
            System.err.println("Couldn't change the title");
            return;
        }
        TextField textField = new TextField(fileLabel.getText());
        file.getChildren().set(labelIndex, textField);
        textField.requestFocus();
        Long fileId = Long.parseLong(file.getUserData().toString());
        textField.setOnAction(_ -> {
            saveEdit(fileLabel, textField, file, labelIndex);
            try {
                server.modifyFileName(fileId, fileLabel.getText());
            } catch (Exception e) {
                System.err.println("File couldn't be found");
                refreshFiles();
            }
        });
        textField.focusedProperty().addListener((_, _, isFocused) -> {
            if (!isFocused) {
                saveEdit(fileLabel, textField, file, labelIndex);

                try {
                    server.modifyFileName(fileId, fileLabel.getText());
                    refreshFiles();
                } catch (Exception e) {
                    System.err.println("File couldn't be found");
                    refreshFiles();
                }
            }
        });


    }


    private void handleDeleteFile(HBox box) {
        boolean delete = server.deleteFile(Long.parseLong(box.getUserData().toString()));
        if(delete){
            System.out.println("Deleted file " + box.getUserData().toString());
        } else{
            System.out.println("Failed to delete file " + box.getUserData().toString());
        }
        refreshFiles();
    }

    private String getExtensionFromMime(String fileType){
        String extension = null;
        try{
            extension = MimeTypes.getDefaultMimeTypes().forName(fileType).getExtension();
        }catch(Exception e){
            System.err.println("Couldn't convert the MIME Type to extension. Setting it to txt");
            extension = "txt";
        }
        return extension;
    }
    private HBox createFileBox(String name, Long fileId, String fileType, Long noteId) {
        HBox file = new HBox();
        file.setUserData(fileId);
        file.setAlignment(Pos.CENTER);
        file.setStyle("-fx-spacing: 3px; -fx-padding: 2px 5px; -fx-border-width: 1px 1px; -fx-border-color: rgba(0,0,0,0.47); -fx-border-radius: 15px");
        Label fileLabel = new Label(name);

        fileLabel.setOnMouseClicked((MouseEvent _) -> {
            downloadFile(fileId, fileType, noteId, fileLabel);
        });
        Button editButton = new Button();
        FontIcon editIcon = new FontIcon("fa-pencil");
        editButton.setGraphic(editIcon);
        editButton.getStyleClass().addAll("button-icon", "flat");

        Button deleteButton = new Button();
        FontIcon deleteIcon = new FontIcon("fa-trash");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().addAll("button-icon", "flat");

        file.getChildren().addAll(fileLabel, editButton, deleteButton);

        editButton.setOnAction(_ -> handleEditAlias(file,fileLabel));
        deleteButton.setOnAction(_ -> handleDeleteFile(file));
        return file;
    }

    private void downloadFile(Long fileId, String fileType, Long noteId, Label fileLabel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        String fileExtension = getExtensionFromMime(fileType);
        fileChooser.setInitialFileName(fileLabel.getText());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(fileType, "*"+fileExtension));
        File downloadLocation = fileChooser.showSaveDialog(noteDisplay.getScene().getWindow());

        if(downloadLocation != null){
            try {
                server.downloadFile(noteId, fileId, downloadLocation);
            } catch (FileNotFoundException e) {
                System.err.println("Error downloading file");
            }
        }
    }

    // Getters and setters for testing
    public Label getCollectionText() {
        return collectionText;
    }

    public TextField getNoteTitle() {
        return noteTitle;
    }

    public TextField getSearchBar() {
        return searchBar;
    }

    public ListView<Pair<Long, String>> getNotesList() {
        return notesList;
    }

    public TextArea getNoteDisplay() {
        return noteDisplay;
    }

    public ComboBox<Pair<String, String>> getFlagDropdown() {
        return flagDropdown;
    }

    public ComboBox<Pair<Long, String>> getCollectionDropdown() {
        return collectionDropdown;
    }

    public Button getAddNoteButton() {
        return addNoteButton;
    }

    public Button getRemoveNoteButton() {
        return removeNoteButton;
    }

    public Button getRefreshNotesButton() {
        return refreshNotesButton;
    }

    public ObservableList<Pair<Long, String>> getNotes() {
        return notes;
    }

    public Long getCurNoteId() {
        return curNoteId;
    }

    public Integer getCurNoteIndex() {
        return curNoteIndex;
    }

    public ServerUtils getServerUtils() {
        return server;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    // Setters
    public void setNoteTitle(TextField noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNotesList(ListView<Pair<Long, String>> notesList) {
        this.notesList = notesList;
    }

    public void setNoteDisplay(TextArea noteDisplay) {
        this.noteDisplay = noteDisplay;
    }

    public void setFlagDropdown(ComboBox<Pair<String, String>> flagDropdown) {
        this.flagDropdown = flagDropdown;
    }

    public void setCollectionDropdown(ComboBox<Pair<Long, String>> collectionDropdown) {
        this.collectionDropdown = collectionDropdown;
    }

    public void setAddNoteButton(Button addNoteButton) {
        this.addNoteButton = addNoteButton;
    }

    public void setRemoveNoteButton(Button removeNoteButton) {
        this.removeNoteButton = removeNoteButton;
    }

    public void setRefreshNotesButton(Button refreshNotesButton) {
        this.refreshNotesButton = refreshNotesButton;
    }

    public void setNotes(ObservableList<Pair<Long, String>> notes) {
        this.notes = notes;
    }

    public void setCurNoteId(Long curNoteId) {
        this.curNoteId = curNoteId;
    }

    public void setCurNoteIndex(Integer curNoteIndex) {
        this.curNoteIndex = curNoteIndex;
    }

    public void setNoteListManager(NoteListManager noteListManager) {
        this.noteListManager = noteListManager;
    }

    public void setSearchBar(TextField searchBar) {
        this.searchBar = searchBar;
    }

    @Override
    public void onMessageReceived(String message) {
        System.out.println("Received WebSocket message in NoteOverviewCtrl: " + message);
        Platform.runLater(()->{
            refreshNotes();
        });
    }

}