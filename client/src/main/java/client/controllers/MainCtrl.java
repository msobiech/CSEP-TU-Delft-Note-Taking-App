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
package client.controllers;


import com.google.inject.Singleton;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.ResourceBundle;

@Singleton
public class MainCtrl {

    MainCtrl() {
    }

    private Stage primaryStage;
    private Stage popUp;

    private NoteOverviewCtrl overviewCtrl;
    private Scene overview;

    private AddNoteCtrl addCtrl;
    private Scene add;

    private ErrorPopUpCtrl errorCtrl;
    private Scene errorScene;

    private ServerSelectionCtrl serverCtrl;
    private Scene serverScene;

    private EditCollectionsPopUpCtrl editCtrl;
    private Scene editScene;

    private ShortcutsPopUpCtrl shortcutsCtrl;
    private Scene shortcutsScene;

    private ResourceBundle language;

    /**
     * Initialization of the main Stage
     * @param primaryStage the stage that will be used to display the app's fronted
     * @param overview the pair of controller for NoteOverview and JavaFX class Parent that links the corresponding UI with its controller
     * @param add the pair of controller for NoteAddition and JavaFX class Parent that links the corresponding UI with its controller
     *            (Currently not functional)
     * @param error the pair of controller for the handling of error message popups and JavaFX class Parent that links the corresponding UI
     *              with its controller.
     * @param serverURL the pair of controller for the handling of Server selection
     *                  popups and JavaFX class Parent that links the corresponding UI
     *                  with its controller.
     * @param collectionEdit the pair of controller for the handling of edit collection
     *                       popups and JavaFX class Parent that links th corresponding UI
     *                       with its controller
     */
    public void initialize(Stage primaryStage, Pair<NoteOverviewCtrl, Parent> overview,
            Pair<AddNoteCtrl, Parent> add, Pair<ErrorPopUpCtrl, Parent> error, Pair<ServerSelectionCtrl, Parent> serverURL, Pair<EditCollectionsPopUpCtrl, Parent> collectionEdit
            ,Pair<ShortcutsPopUpCtrl, Parent> showShortcuts) {
        this.primaryStage = primaryStage;
        this.overviewCtrl = overview.getKey();
        this.overview = new Scene(overview.getValue());

        this.addCtrl = add.getKey();
        this.add = new Scene(add.getValue());

        this.errorCtrl = error.getKey();
        this.errorScene = new Scene(error.getValue());

        this.serverCtrl = serverURL.getKey();
        this.serverScene = new Scene(serverURL.getValue());

        this.editCtrl = collectionEdit.getKey();
        this.editScene = new Scene(collectionEdit.getValue());

        this.shortcutsCtrl = showShortcuts.getKey();
        this.shortcutsScene = new Scene(showShortcuts.getValue());

        //showServerSelection();
        showOverview();
        primaryStage.show();
    }

    public void setLanguage(ResourceBundle language){
        this.language = language;
    }
    /**
     * Method to show the scene for notes overview
     */
    public void showOverview() {
        primaryStage.setTitle(language.getString("window.primary.title"));
        primaryStage.setScene(overview);
        overview.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        primaryStage.setResizable(true);
        overviewCtrl.refreshNotes();

    }

    /**
     * Method to show the scene for note addition
     */
    public void showAdd() {
        //primaryStage.setTitle("Notes: Adding Note");
        //primaryStage.setScene(add);
        //primaryStage.setResizable(false);
    }

    /**
     * This method shows the error popUp
     * @param error String representing the error message text
     */
    public void showError(String error){
        this.popUp = new Stage();
        popUp.setScene(errorScene);
        popUp.setTitle(language.getString("window.error.title"));
        popUp.setResizable(false);
        errorCtrl.setErrorLabel(error);
        popUp.show();

    }

    /**
     * This method hides the error window
     */
    public void hideError(){
        popUp.hide();
    }

    /**
     * This method shows the server selection popUp
     */
    public void showServerSelection(){
        this.popUp = new Stage();
        popUp.setScene(serverScene);
        popUp.setTitle("Notes: Server Selection");
        popUp.setResizable(false);
        popUp.show();
    }

    /**
     * this method hides the popup and shows the notes start page
     */
    public void hideServerSelection(){
        popUp.hide();
        showOverview();
        primaryStage.show();
    }

    /**
     * Method to show the scene for collection editing
     */
    public void showEditCollections(){
        this.popUp = new Stage();
        popUp.setScene(editScene);
        popUp.setTitle("Collections: Edit");
        popUp.setResizable(false);
        popUp.show();
    }

    public void showShortcuts(){
        this.popUp = new Stage();
        popUp.setScene(shortcutsScene);
        popUp.setTitle("Shortcuts: Overview");
        popUp.setResizable(false);
        popUp.show();
    }

    public void hideShortcuts() {
        popUp.hide();
    }

    public void updateOverview(Pair<NoteOverviewCtrl, Parent> overview) {
        this.overviewCtrl = overview.getKey();
        this.overview = new Scene(overview.getValue());
        showOverview();
    }

}