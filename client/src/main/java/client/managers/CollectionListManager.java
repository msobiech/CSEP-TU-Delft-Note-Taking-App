package client.managers;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import models.Collection;

public class CollectionListManager {
    public ObservableList<Collection> collections;
    public final ListView<Collection> collectionsList;

    public CollectionListManager(ListView<Collection> collectionsList) {
        this.collectionsList = collectionsList;
    }

    public void setCollections(ObservableList<Collection> collections) { this.collections = collections; }
}
