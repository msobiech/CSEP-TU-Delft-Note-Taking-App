package client.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.google.inject.Inject;

import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import javafx.util.Pair;


public class NoteOverviewCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

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
    private Button addNoteButton, removeNoteButton, refreshNotesButton;

    private ObservableList<Pair<Long, String>> notes; // pair of the note ID and note title
    // We don't want to store the whole note here since we only need to fetch the one that is currently selected.

    private final Parser markdownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();
    private Timer debounceTimer = new Timer();

    private int changeCountContent = 0;
    private int changeCountTitle = 0;
    private final int THRESHOLD = 5;
    private final int DELAY = 1000;
    private Runnable lastTask = null;

    private boolean ignoreNext = false;

    private Long curNoteId = null;
    private Integer curNoteIndex = null;

    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noteTitle.textProperty().addListener((_, _, newValue) -> {
            if (curNoteIndex == null || curNoteId == null || newValue.trim().isEmpty()) {
                return; // Ignore updates when no note is selected or title is empty
            }
            changeCountTitle++;
            //System.out.println("Change has occured at " + curNoteIndex);
            try{
                Platform.runLater(() -> {
                    notes.set(curNoteIndex, new Pair<>(curNoteId, newValue.trim()));
                });
            } catch(Exception e){
                System.out.println(e.getMessage());
            }


            debounce(() -> {
                try {
                    server.updateNoteTitleByID(curNoteId, newValue.trim());

                    //refreshNotes(); // Update the list titles after a successful update
                    changeCountTitle = 0;
                } catch (Exception e) {
                    System.out.println("Failed to update title: " + e.getMessage());
                }
            },DELAY);
            if (changeCountTitle >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
                try {
                    server.updateNoteTitleByID(curNoteId, newValue.trim());
                    //refreshNotes(); // Update the list titles after a successful update
                    changeCountTitle = 0;
                } catch (Exception e) {
                    System.out.println("Failed to update title: " + e.getMessage());
                }
                changeCountContent = 0; // Reset change count
                debounceTimer.cancel(); // Cancel any pending debounced update
            }
        });

        noteDisplay.setEditable(false);
        notesList.getSelectionModel().selectedItemProperty().addListener((_, oldNote, newNote) -> {

            if (newNote != null) {

                noteDisplay.setEditable(true);
                noteTitle.setEditable(true);
                if(lastTask != null) {
                    debounceTimer.cancel();
                    lastTask.run();
                    lastTask = null;
                }
                curNoteId = newNote.getKey();
                curNoteIndex = notesList.getSelectionModel().getSelectedIndex();
                var newTitle = newNote.getValue();
                if(oldNote!=null && !Objects.equals(oldNote.getKey(), newNote.getKey())) {
                    updateNoteTitle(newTitle);
                } else if(oldNote==null){
                    updateNoteTitle(newTitle);
                }
                var id = newNote.getKey();



                var content = server.getNoteContentByID(id);
                updateNoteDisplay(content);
                try {
                    renderMarkdown(content);
                } catch (InterruptedException e) {
                    mainCtrl.showError(e.toString());
                }

            } else{
                noteDisplay.setEditable(false);
            }
        });
        noteDisplay.textProperty().addListener((_, _, newValue) -> {
            changeCountContent++; // Count the amount of changes
            try {
                renderMarkdown(newValue);
            } catch (InterruptedException e) {
                mainCtrl.showError(e.toString());
            }
            debounce(() -> {
                try{
                    server.updateNoteContentByID(curNoteId,newValue);
                } catch(Exception e){
                    System.out.println("Failed to update content: " + e.getMessage());
                }
                changeCountContent = 0; // Reset change count if the scheduled task has been executed
            }, DELAY);
            if (changeCountContent >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
                try{
                    server.updateNoteContentByID(curNoteId,newValue);
                } catch(Exception e){
                    System.out.println("Failed to update content: " + e.getMessage());
                }
                changeCountContent = 0; // Reset change count
                debounceTimer.cancel(); // Cancel any pending debounced update
            }
        });
        removeNoteButton.setDisable(true);

        notesList.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            removeNoteButton.setDisable(newValue == null);
        });

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
                    "font-family: -apple-system,BlinkMacSystemFont,\"Segoe UI\",\"Noto Sans\",Helvetica,Arial,sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\";" +
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
     * Method that refreshes the notes. (Currently not connected to the server)
     */
    public void refreshNotes() {
        System.out.println("Refreshed the note list");
        var notesFromServer = server.getNoteTitles();
        List<Pair<Long, String>> notesAsPairs = new ArrayList<>();
        for(var row: notesFromServer){
            Long id = ((Integer)row[0]).longValue();
            String title = (String)row[1];
            notesAsPairs.add(new Pair<>(id, title));
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
    }

    /**
     * Method to add notes (Currently not functional)
     */
    public void addNote(){
        System.out.println("Adding a new note");
        server.addNote();
        refreshNotes();
    }

    /**
     * Method to remove notes form UI
     */
    @FXML
    private void removeNote() throws InterruptedException {
        var selectedNote = notesList.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm deletion");
            alert.setHeaderText("Are you sure you want to delete this note?");
            alert.setContentText("You are trying to delete note: " + selectedNote.getValue() + "." +
                    "\nDeleting a note is irreversible!");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if(notes.size() == 1){
                    renderMarkdown("");
                    updateNoteDisplay("");
                    updateNoteTitle("");

                }
                server.deleteNoteByID(selectedNote.getKey());
                notesList.getItems().remove(selectedNote);
                curNoteId = notesList.getSelectionModel().getSelectedItem().getKey();
                curNoteIndex = notesList.getSelectionModel().getSelectedIndex();
            } else {
                System.out.println("Deletion canceled.");
            }
        }
    }


}