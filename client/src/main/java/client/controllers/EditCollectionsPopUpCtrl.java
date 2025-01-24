package client.controllers;

import client.DialogFactory;
import client.event.CollectionEvent;
import client.event.EventBus;
import client.InjectorProvider;
import client.managers.CollectionListManager;
import client.managers.CollectionManager;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.Collection;
import client.utils.CollectionService;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditCollectionsPopUpCtrl implements Initializable {

    private final ServerUtils server;
    private final CollectionService collectionService;
    private final DialogFactory dialogFactory;
    private final EventBus eventBus;

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

    private CollectionManager collectionManager;
    private final ObservableList<Collection> collections;
    private CollectionListManager collectionListManager;
    private NoteOverviewCtrl noteOverviewCtrl = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);

    @Inject
    public EditCollectionsPopUpCtrl(ServerUtils server, CollectionService collectionService, DialogFactory dialogFactory, EventBus eventBus) {
        this.server = server;
        this.collectionService = collectionService;
        this.dialogFactory = dialogFactory;
        this.eventBus = eventBus;
        this.collections = FXCollections.observableArrayList();
    }

    public void initialize(URL location, ResourceBundle resources) {
        collectionListManager = new CollectionListManager(collectionListView);
        collectionManager = new CollectionManager(this, collectionService, server);
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
            Collection addedCollection = server.addCollection(newCollection);
            collections.add(addedCollection);
            noteOverviewCtrl.refreshCollectionChoice();
            noteOverviewCtrl.refreshNoteCollectionDropdown();
            collectionTitleField.clear();
            System.out.println("Collection added successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteCollection() {
        var selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if (selectedCollection != null) {
            Optional<ButtonType> result = dialogFactory.createConfirmationDialog(
                    "Confirm deletion",
                    "Are you sure you want to delete this collection?",
                    "You are trying to delete collection: " + selectedCollection.getName() + ".\nDeleting a collection is irreversible!"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                eventBus.publish(new CollectionEvent(CollectionEvent.EventType.COLLECTION_REMOVE, null, selectedCollection.getId(), null));
            } else {
                System.out.println("Deletion cancelled.");
            }
        }
    }

    public void refreshCollections() {
        try {
            List<Collection> fetchedCollections = server.getAllCollectionsFromServer();
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
            String status = server.getCollectionStatus(title);
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
    }

    public CollectionListManager getCollectionListManager() {
        return collectionListManager;
    }

}
