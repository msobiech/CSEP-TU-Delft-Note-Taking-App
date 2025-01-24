package client.managers;

import client.InjectorProvider;
import client.controllers.EditCollectionsPopUpCtrl;
import client.event.CollectionEvent;
import client.event.CollectionEvent.EventType;
import client.event.EventBus;
import client.event.MainEventBus;
import client.utils.ServerUtils;
import client.utils.CollectionService;

public class CollectionManager {
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);
    private final EditCollectionsPopUpCtrl collectionsCtrl;
    private final CollectionService collectionService;
    private final ServerUtils server;

    public CollectionManager(EditCollectionsPopUpCtrl collectionsCtrl, CollectionService collectionService, ServerUtils server) {
        this.collectionsCtrl = collectionsCtrl;
        this.collectionService = collectionService;
        this.server = server;
        eventBus.subscribe(CollectionEvent.class, this::handleChange);
    }

    private void handleChange(CollectionEvent event) {
        EventType type = event.getEventType();
        //Made it switch-case so other eventTypes can also eventually be refactored in this way
        switch (type) {
            case COLLECTION_REMOVE:
                handleCollectionDeletion(event);
                break;
        }
    }

    private void handleCollectionDeletion(CollectionEvent event) {
        if (event.getChangeID() != null) {
            server.deleteCollectionByID(event.getChangeID());

            collectionsCtrl.getCollectionListManager().handleCollectionDeletion();
            System.out.println("Collection " + event.getChangeID() + " successfully deleted.");
        } else {
            System.out.println("Collection deletion failed.");
        }
    }
}
