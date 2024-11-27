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

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.kordamp.bootstrapfx.BootstrapFX;


public class MainCtrl {

    private Stage primaryStage;

    private NoteOverviewCtrl overviewCtrl;
    private Scene overview;

    private AddQuoteCtrl addCtrl;
    private Scene add;

    /**
     * Initialization of the main Stage
     * @param primaryStage the stage that will be used to display the app's fronted
     * @param overview the pair of controller for NoteOverview and JavaFX class Parent that links the corresponding UI with its controller
     * @param add the pair of controller for NoteAddition and JavaFX class Parent that links the corresponding UI with its controller
     *            (Currently not functional, uses old quote addition)
     */
    public void initialize(Stage primaryStage, Pair<NoteOverviewCtrl, Parent> overview,
            Pair<AddQuoteCtrl, Parent> add) {
        this.primaryStage = primaryStage;
        this.overviewCtrl = overview.getKey();
        this.overview = new Scene(overview.getValue());

        this.addCtrl = add.getKey();
        this.add = new Scene(add.getValue());

        showOverview();
        primaryStage.show();
    }

    /**
     * Method to show the scene for notes overview
     */
    public void showOverview() {
        primaryStage.setTitle("Notes: Overview");
        primaryStage.setScene(overview);
        overview.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        primaryStage.setResizable(true);
        overviewCtrl.refreshNotes();
    }

    /**
     * Method to show the scene for note addition (Currently not functional, uses old Quote add)
     */
    public void showAdd() {
        primaryStage.setTitle("Quotes: Adding Quote");
        primaryStage.setScene(add);
        primaryStage.setResizable(false);
        //add.setOnKeyPressed(e -> addCtrl.keyPressed(e));
    }
}