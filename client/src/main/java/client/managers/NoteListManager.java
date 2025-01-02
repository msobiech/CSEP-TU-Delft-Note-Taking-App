package client.managers;

import client.event.MainEventBus;
import client.event.NoteEvent;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class NoteListManager {
    private ObservableList<Pair<Long, String>> notes;
    private final MainEventBus eventBus = MainEventBus.getInstance();

    public NoteListManager() {
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
        }
    }


    public void setNotes(ObservableList<Pair<Long, String>> notes) {
        this.notes = notes;
    }
}
