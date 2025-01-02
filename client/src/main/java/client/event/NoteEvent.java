package client.event;

import org.apache.commons.lang3.StringUtils;

public class NoteEvent extends Event {

    public enum EventType {
        CONTENT_CHANGE,
        TITLE_CHANGE,
        TITLE_REMOVE,
        NOTE_REMOVE,
        NOTE_ADD
    }

    private final EventType eventType;
    private final String change;
    private Long NoteId;
    private Integer ListIndex;

    public NoteEvent(EventType eventType, String change) {
        this.eventType = eventType;
        this.change = change;
    }

    public NoteEvent(EventType eventType, String change, Long NoteId, Integer ListIndex) {
        this(eventType, change);
        this.NoteId = NoteId;
        this.ListIndex = ListIndex;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getChange() {
        return change;
    }

    public Long getNoteId() {
        return NoteId;
    }

    public Integer getListIndex() {
        return ListIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{eventType=" + eventType +
                ", change='" + StringUtils.left(change, 100) + '\'' +
                ", NoteId=" + NoteId +
                ", ListIndex=" + ListIndex +
                '}';
    }
}
