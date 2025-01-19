package client.event;

import java.util.Map;

public class NoteContentEvent extends NoteEvent {

    public NoteContentEvent(EventType eventType, String change, Long NoteId, Integer ListIndex, Map<String, String> aliases) {
        super(eventType, change, NoteId, ListIndex, aliases);
    }
}
