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
package client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

import client.controllers.*;
import client.managers.LanguageManager;
import com.google.inject.Injector;

import client.utils.ServerUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

	private static final Injector INJECTOR = InjectorProvider.getInjector();
	private static final MyFXML FXML = new MyFXML(INJECTOR);

	public static void main(String[] args) throws URISyntaxException, IOException {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		var serverUtils = INJECTOR.getInstance(ServerUtils.class);
		if (!serverUtils.isServerAvailable()) {
			var msg = "Server needs to be started before the client, but it does not seem to be available. Shutting down.";
			System.err.println(msg);
			return;
		}

		Locale currentLanguage = LanguageManager.getLanguage();

		ResourceBundle bundle = ResourceBundle.getBundle("client.controllers.language", currentLanguage);

		var overview = FXML.load(NoteOverviewCtrl.class, bundle, "client", "views", "NoteOverview.fxml");
		var add = FXML.load(AddNoteCtrl.class, bundle, "client", "views", "AddNote.fxml");
		var error = FXML.load(ErrorPopUpCtrl.class, bundle, "client", "views", "ErrorPopUp.fxml");
		var serverURL = FXML.load(ServerSelectionCtrl.class, bundle, "client", "views", "ServerSelection.fxml");
		var collectionsEdit = FXML.load(EditCollectionsPopUpCtrl.class, bundle, "client", "views", "EditCollectionsPopUp.fxml");

		var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
		mainCtrl.setLanguage(bundle);
		mainCtrl.initialize(primaryStage, overview, add, error, serverURL, collectionsEdit);
                primaryStage.setOnCloseRequest(_ -> {
			Platform.exit();
			System.exit(0); // Force stop (IDK nothing worked for ending the process when closing the app)
		});
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}



}