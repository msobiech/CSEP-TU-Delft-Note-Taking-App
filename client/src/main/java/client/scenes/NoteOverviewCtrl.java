package client.scenes;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.ext.tables.TablesExtension;


public class NoteOverviewCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField noteTitle;

    @FXML
    private TextField searchBar;

    @FXML
    private ListView<String> notesList;

    @FXML
    private TextArea noteDisplay;

    @FXML
    private ScrollPane markdownPreview;

    @FXML
    private VBox markdownContent;

    @FXML
    private Button addNoteButton, removeNoteButton, refreshNotesButton;

    private ObservableList<String> notes;

    private final Parser markdownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();


    @Inject
    public NoteOverviewCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notesList.setItems(notes);
        notesList.getSelectionModel().selectedItemProperty().addListener((_, _, newNote) -> {
            if (newNote != null) {
                updateNoteDisplay(newNote);
            }
        });
        noteDisplay.textProperty().addListener((_, _, newValue) -> renderMarkdown(newValue));
    }

    private void renderMarkdown(String markdownText) {
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


    private void updateNoteDisplay(String note) {
        noteTitle.setText(note);
    }

    public void refreshNotes() {
        var quotes = server.getQuotes();
        System.out.println("Refreshed the note list");
        notes = FXCollections.observableArrayList();
        notes.add("Note 1");
        notesList.setItems(notes);
    }

    public void addNote(){
        System.out.println("Adding a new note");
        mainCtrl.showAdd();
    }

    public void removeNote(){
        System.out.println("Removing a note");
    }
}