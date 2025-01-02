package client.event;

public class NoteContentEvent extends NoteEvent {



    public NoteContentEvent(EventType eventType, String change, Long NoteId, Integer ListIndex) {
        super(eventType, change, NoteId, ListIndex);
    }
}
