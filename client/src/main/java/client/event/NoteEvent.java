package client.event;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class NoteEvent extends Event {



    public enum EventType {
        CONTENT_CHANGE,
        TITLE_CHANGE,
        TITLE_REMOVE,
        NOTE_REMOVE,
        NOTE_ADD
    }

    private Map<String,String> aliases;
    private final EventType eventType;
    private final String change;
    private Long NoteId;
    private Integer ListIndex;

    public NoteEvent(EventType eventType, String change) {
        this.eventType = eventType;
        this.change = change;
    }
    public NoteEvent(EventType eventType, String change, Long changeID, Integer listIndex) {
        this.eventType = eventType;
        this.change = change;
        this.NoteId = changeID;
        this.ListIndex = listIndex;
    }

    public NoteEvent(EventType eventType, String change, Map<String,String> aliases) {
        this.eventType = eventType;
        this.change = change;
        this.aliases = aliases;
    }

    public NoteEvent(EventType eventType, String change, Long noteId, Integer listIndex, Map<String,String> aliases) {
        this(eventType, change, aliases);
        this.NoteId = noteId;
        this.ListIndex = listIndex;
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

    public Map<String, String> getAliases() {
        return aliases;
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{eventType=" + eventType +
                ", change='" + StringUtils.left(change, 10) + '\'' +
                ", NoteId=" + NoteId +
                ", ListIndex=" + ListIndex +
                ", aliases=" + aliases +
                '}';
    }
}
