package client.event;

public class NoteStatusEvent extends NoteEvent {
    private final Long changeID;

    public NoteStatusEvent(EventType eventType, Long changeID) {
        super(eventType, null);
        this.changeID = changeID;
    }

    public NoteStatusEvent(EventType eventType, String change, Long changeID, Integer listIndex) {
        super(eventType, change, changeID, listIndex);
        this.changeID = changeID;
    }

    public Long getChangeID() {
        return changeID;
    }
}
