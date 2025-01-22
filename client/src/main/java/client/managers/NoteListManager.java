package client.managers;

import client.InjectorProvider;
import client.event.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;

public class NoteListManager {
    private ObservableList<Pair<Long, String>> notes;
    private final ListView<Pair<Long, String>> notesList;
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);

    public NoteListManager(ListView<Pair<Long, String>> notesList) {
        this.notesList = notesList;
        eventBus.subscribe(NoteEvent.class, this::handleChange);
        eventBus.subscribe(NoteNavigationEvent.class, this::handleNavigation);
    }

    private void handleNavigation(NoteNavigationEvent event) {
        Platform.runLater(() -> {
            ObservableList<Pair<Long, String>> items = notesList.getItems();
            int currentIndex = notesList.getSelectionModel().getSelectedIndex();

            if (event.getDirection() == NoteNavigationEvent.Direction.NEXT) {
                if (currentIndex < items.size() - 1) {
                    notesList.getSelectionModel().select(currentIndex + 1);
                    notesList.scrollTo(currentIndex + 1);
                }
            } else if (event.getDirection() == NoteNavigationEvent.Direction.PREVIOUS) {
                if (currentIndex > 0) {
                    notesList.getSelectionModel().select(currentIndex - 1);
                    notesList.scrollTo(currentIndex - 1);
                    System.out.println("Navigating to PREVIOUS");
                }
            }
        });
    }

    private void handleChange(NoteEvent event) {
        NoteEvent.EventType type = event.getEventType();
        switch(type){
            case TITLE_CHANGE:
                handleTitleChange(event);
                break;
            case NOTE_REMOVE:
                handleNoteDeletion();
                break;
        }
    }

    private void handleTitleChange(NoteEvent event) {
        try {
            int index = event.getListIndex();
            Pair<Long, String> oldNote = notes.get(index);
            Pair<Long, String> newNote = new Pair<>(event.getNoteId(), event.getChange().trim());

            // Update the title in the notes list
            notes.set(index, newNote);

            // Register undo action
            eventBus.publish(new UndoableActionEvent(
                    event.getNoteId(),
                    UndoableActionEvent.ActionType.EDIT_TITLE,
                    oldNote, // Save the old title as the previous state
                    previousState -> {
                        notes.set(index, (Pair<Long, String>) previousState);
                        notesList.refresh();
                    } // Undo logic
            ));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void handleNoteDeletion() {
        var selectedNote = notesList.getSelectionModel().getSelectedItem();
        notesList.getItems().remove(selectedNote);
    }


    public void setNotes(ObservableList<Pair<Long, String>> notes) {
        this.notes = notes;
    }
}
