package client.controllers;

import client.controllers.MainCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;



public class ErrorPopUpCtrl {

    @FXML
    public Label errorLabel;

    private final MainCtrl mainCtrl;
    private final ServerUtils server;


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
     */
    public void setErrorLabel(String error) {
        errorLabel.setText("Oh no! It seems the following error occurred:\n" + error +
            "\nFor common fixes please visit: \nhttps://emmer.dev/blog/common-markdown-mistakes/");
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
