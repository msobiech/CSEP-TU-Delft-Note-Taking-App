package client.event;

import org.apache.commons.lang3.StringUtils;

public class CollectionEvent extends Event {

    public enum EventType {
        COLLECTION_REMOVE
    }

    private final EventType eventType;
    private final String change;
    private Long collectionID;
    private Integer listIndex;


    public CollectionEvent(EventType eventType, String change, Long changeID, Integer listIndex) {
        this.eventType = eventType;
        this.change = change;
        this.collectionID = changeID;
        this.listIndex = listIndex;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getChange() {
        return change;
    }

    public Long getChangeID() {
        return collectionID;
    }

    public Integer getListIndex() {
        return listIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{eventType=" + getEventType() +
                ", change='" + StringUtils.left(change, 10) + '\'' +
                ", collectionID=" + getChangeID() +
                ", ListIndex=" + getListIndex() +
                '}';
    }
}
