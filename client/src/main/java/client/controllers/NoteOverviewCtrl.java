package client.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.utils.ServerUtils;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private VBox markdownContent;

    @FXML
    private Button addNoteButton, removeNoteButton, refreshNotesButton;

    private ObservableList<Pair<Long, String>> notes; // pair of the note ID and note title
    // We don't want to store the whole note here since we only need to fetch the one that is currently selected.

    private final Parser markdownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();

    private Long curNoteId = null;

    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        notesList.getSelectionModel().selectedItemProperty().addListener((_, _, newNote) -> {
            if (newNote != null) {
                var newTitle = newNote.getValue();
                updateNoteTitle(newTitle);
                var id = newNote.getKey();
                var content = server.getNoteContentByID(id);
                updateNoteDisplay(content);
                curNoteId = newNote.getKey();
            }
        });
        noteDisplay.textProperty().addListener((_, _, newValue) -> {
            delay(1000, () -> {
                try {
                    renderMarkdown(newValue);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    /**
     * This method delays the changes made by the user by 1000
     * milliseconds before showing them on the markdown preview
     * @param milli the amount of milliseconds
     * @param continues what to perform after the delay.
     */
    public static void delay(int milli, Runnable continues) {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(milli);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continues.run());
        new Thread(sleeper).start();
    }

    private void renderMarkdown(String markdownText) throws InterruptedException {
        String htmlContent = "<style>" +
            "body { font-family: Tahoma, Arial, sans-serif; font-weight: lighter;}" +
            "blockquote { margin: 20px 0; padding: 10px 20px;" +
            "border-left: 5px solid #ccc; background-color: #f9f9f9; font-style: italic; color: #555; }" +
            "table { width: 50%; border-collapse: collapse;}" +
            "table, th, td { border: 1px solid #333; }" +
            "th, td { padding: 8px; text-align: left; }" +
            "th { background-color: #f2f2f2; }" +
            "</style>" + htmlRenderer.render(markdownParser.parse(markdownText));
        WebView webView = new WebView();
        webView.getEngine().loadContent(htmlContent);
        webView.setPrefHeight(markdownPreview.getHeight());
        webView.setPrefWidth(markdownPreview.getWidth());
        markdownContent.getChildren().clear();
        markdownContent.getChildren().add(webView);
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
        mainCtrl.showAdd();
    }

    /**
     * Method to remove notes(Currently not functional)
     */
    public void removeNote(){
        System.out.println("Removing a note");
    }
}