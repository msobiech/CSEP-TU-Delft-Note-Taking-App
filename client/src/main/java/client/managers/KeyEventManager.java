package client.managers;

import client.InjectorProvider;
import client.controllers.MainCtrl;
import client.controllers.NoteOverviewCtrl;
import client.event.EditCollectionsEvent;
import client.event.EscapeKeyEvent;
import client.event.EventBus;
import jakarta.inject.Inject;
import javafx.scene.control.TextField;

public class KeyEventManager {

    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(EventBus.class);
    private static final MainCtrl mainCtrl = InjectorProvider.getInjector().getInstance(MainCtrl.class);
    private static final NoteOverviewCtrl noteOverviewCtrl = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);



    @Inject
    public KeyEventManager() {
        this.eventBus.subscribe(EscapeKeyEvent.class, event -> handleEscapeKey());
        this.eventBus.subscribe(EditCollectionsEvent.class, event -> handleEditCollections());

    }

    private void handleEscapeKey() {
        // Focus the search bar when ESC is pressed
        if (noteOverviewCtrl != null) {
            noteOverviewCtrl.getSearchBar().requestFocus();
        }
    }

    private void handleEditCollections() {
        if (mainCtrl != null) {
            mainCtrl.showEditCollections();
        }
        if (noteOverviewCtrl != null) {
            noteOverviewCtrl.refreshNotes();
        }
    }
}