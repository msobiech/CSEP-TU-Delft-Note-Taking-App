package client.controllers;

import client.InjectorProvider;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ResourceBundle;


public class ErrorPopUpCtrl {

    @FXML
    public Label errorLabel;

    private final MainCtrl mainCtrl;
    private final ServerUtils server;

    private final NoteOverviewCtrl noteOverviewCtrl = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);


    @Inject
    public ErrorPopUpCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.errorLabel = new Label();
    }

    /**
     * This method sets the error label to a helpful message
     *
     * @param error the String representing the error message
     * @return String for the text
     */
    public String setErrorLabel(String error) {
        ResourceBundle lang = noteOverviewCtrl.getLanguage();
        errorLabel.setText(lang.getString("error.label") + "\n\n" + error);
        return errorLabel.getText();
    }

    /**
     * this method is connected to the button in the popUp window
     * this closes the window.
     */
    public void click() {
        mainCtrl.hideError();
        mainCtrl.showOverview();
    }
}
