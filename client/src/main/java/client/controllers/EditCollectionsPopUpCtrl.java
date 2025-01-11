package client.controllers;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import models.Collection;

import java.util.List;

public class EditCollectionsPopUpCtrl {
    @FXML
    public Button addCollectionButton;

    @FXML
    public Button deleteCollectionButton;

    @FXML
    public Button refreshCollectionsButton;

    @FXML
    public ListView<Collection> collectionListView;

    @FXML
    public TextField collectionTitleField;

    private final ServerUtils serverUtils;
    private ObservableList<Collection> collections;

    @Inject
    public EditCollectionsPopUpCtrl(ServerUtils serverUtils) {
        this.serverUtils = serverUtils;
        this.collections = FXCollections.observableArrayList();
    }

    public void addCollection(ActionEvent actionEvent) {
        String title = collectionTitleField.getText();
        if (title.isEmpty()) {
            System.out.println("Collection title is empty");
            return;
        }

        Collection newCollection = new Collection();
        newCollection.setName(title);

        try {
            Collection addedCollection = serverUtils.addCollection(newCollection);
            collections.add(addedCollection);
            collectionTitleField.clear();
            System.out.println("Collection added successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteCollection(ActionEvent actionEvent) {
        //to be implemented
    }

    public void refreshCollections(ActionEvent actionEvent) {
        try {
            List<Collection> fetchedCollections = serverUtils.getAllCollectionsFromServer();
            collections.setAll(fetchedCollections);
            System.out.println("Collections refreshed successfully");
        } catch (Exception e) {
            System.out.println("Failed to refresh collections");
            System.out.println(e.getMessage());
        }
    }

}
