package client.controllers;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class ServerSelectionCtrl {

    @FXML
    public TextField serverTextField;

    private final MainCtrl mainCtrl;
    private final ServerUtils server;


    @Inject
    public ServerSelectionCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.serverTextField = new TextField();
    }

    /**
     * Method to act on clicking the connected element.
     */
    public void click() {
        server.setServerURL(serverTextField.getText());
        mainCtrl.hideServerSelection();
        mainCtrl.showOverview();
    }


}
