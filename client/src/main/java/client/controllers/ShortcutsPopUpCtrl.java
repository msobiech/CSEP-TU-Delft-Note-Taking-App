package client.controllers;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ShortcutsPopUpCtrl {

    @FXML
    public Button ShortcutPopUpOKButton;

    private final MainCtrl mainCtrl;

    @Inject
    public ShortcutsPopUpCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }


    public void closePopUp(ActionEvent actionEvent) {
        mainCtrl.hideShortcuts();
    }
}
