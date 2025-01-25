package client.managers;

import client.InjectorProvider;
import client.controllers.EditCollectionsPopUpCtrl;
import client.controllers.MainCtrl;
import client.controllers.NoteOverviewCtrl;
import client.event.CollectionEvent;
import client.event.CollectionEvent.EventType;
import client.event.EventBus;
import client.event.MainEventBus;
import client.utils.ServerUtils;
import client.utils.CollectionService;

import java.util.ResourceBundle;

public class CollectionManager {
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);
    private final EditCollectionsPopUpCtrl collectionsCtrl;
    private final CollectionService collectionService;
    private final NoteOverviewCtrl noteOverviewCtrl = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);
    private final ServerUtils server;
    private final MainCtrl mainCtrl = InjectorProvider.getInjector().getInstance(MainCtrl.class);

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
        ResourceBundle language = noteOverviewCtrl.getLanguage();
        if (event.getChangeID() != null && server.deleteCollectionByID(event.getChangeID())){
            collectionsCtrl.getCollectionListManager().handleCollectionDeletion();
            noteOverviewCtrl.refreshCollectionChoice();
            noteOverviewCtrl.refreshNoteCollectionDropdown();
            System.out.println("Collection " + event.getChangeID() + " successfully deleted.");
        } else {
            mainCtrl.showError(language.getString("collection.delete.fail"));
        }
    }
}
