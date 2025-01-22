package client.event;

import java.util.Map;

public class NoteContentEvent extends NoteEvent {

    public NoteContentEvent(EventType eventType, String change, Long noteId, Integer listIndex, Map<String, String> aliases) {
        super(eventType, change, noteId, listIndex, aliases);
    }
}
