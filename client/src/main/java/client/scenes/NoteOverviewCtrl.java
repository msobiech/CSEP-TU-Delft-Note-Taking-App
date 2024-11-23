/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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