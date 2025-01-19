package client.managers;

import client.event.EscapeKeyEvent;
import client.event.EventBus;
import jakarta.inject.Inject;
import javafx.scene.control.TextField;

public class KeyEventManager {

    private final EventBus eventBus;
    private final TextField searchBar;

    @Inject
    public KeyEventManager(EventBus eventBus, TextField searchBar) {
        this.eventBus = eventBus;
        this.searchBar = searchBar;

        // Subscribe to the EscapeKeyEvent
        this.eventBus.subscribe(EscapeKeyEvent.class, event -> handleEscapeKey());
    }

    private void handleEscapeKey() {
        // Focus the search bar when ESC is pressed
        if (searchBar != null) {
            searchBar.requestFocus();
        }
    }
}