package client.controllers;

import client.managers.CollectionListManager;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.Collection;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditCollectionsPopUpCtrl implements Initializable {
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

    @FXML
    public TextField collectionServerField;

    @FXML
    public TextField userFriendlyNameField;

    @FXML
    public Button makeDefaultButton;

    @FXML
    public Button SaveCollectionButton;

    @FXML
    private Label statusLabel;

    private ResourceBundle language;

    private final ServerUtils serverUtils;
    private final ObservableList<Collection> collections;
    private CollectionListManager collectionListManager;

    @Inject
    public EditCollectionsPopUpCtrl(ServerUtils serverUtils) {
        this.serverUtils = serverUtils;
        this.collections = FXCollections.observableArrayList();
    }

    public void initialize(URL location, ResourceBundle resources) {
        collectionListManager = new CollectionListManager(collectionListView);
        refreshCollections();
    }

    public void addCollection() {
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

    public void deleteCollection() {
        //to be implemented
    }

    public void refreshCollections() {
        try {
            List<Collection> fetchedCollections = serverUtils.getAllCollectionsFromServer();
            collections.setAll(fetchedCollections);
            System.out.println("Collections refreshed successfully");
        } catch (Exception e) {
            System.out.println("Failed to refresh collections");
            System.out.println(e.getMessage());
        }

        collectionListManager.setCollections(collections);

        collectionListView.setItems(collections);

        collectionListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Collection item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    @FXML
    private void checkCollectionStatus() {
        String title = collectionTitleField.getText();
        if (title.isEmpty()) {
            statusLabel.setText("Please enter a collection title.");
            return;
        }
        try {
            String status = serverUtils.getCollectionStatus(title);
            if (status.equals("Collection exists")) {
                statusLabel.setText("Collection already exists");
            } else if (status.equals("Collection will be created")) {
                statusLabel.setText("Collection will be created");
            } else {
                statusLabel.setText("Unknown status: " + status);
            }
        } catch (Exception e) {
            statusLabel.setText("Server unreachable");
        }
    }}
