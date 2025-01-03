package client.managers;

import client.event.MainEventBus;
import client.event.NoteEvent;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;

public class NoteListManager {
    private ObservableList<Pair<Long, String>> notes;
    private final ListView<Pair<Long, String>> notesList;
    private final MainEventBus eventBus = MainEventBus.getInstance();

    public NoteListManager(ListView<Pair<Long, String>> notesList) {
        this.notesList = notesList;
        eventBus.subscribe(NoteEvent.class, event -> {
            handleChange(event);
        });
    }


    private void handleChange(NoteEvent event) {
        NoteEvent.EventType type = event.getEventType();
        System.out.println(event + " has been received by " + this.getClass().getSimpleName());
        switch(type){
            case TITLE_CHANGE:
                try{
                    notes.set(event.getListIndex(), new Pair<>(event.getNoteId(), event.getChange().trim()));
                } catch(Exception e){
                    System.err.println(e.getMessage());
                }
                break;
            case NOTE_REMOVE:
                handleNoteDeletion();
                break;
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
