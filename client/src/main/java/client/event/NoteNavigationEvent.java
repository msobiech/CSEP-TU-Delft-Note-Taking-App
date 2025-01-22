package client.event;

public class NoteNavigationEvent extends Event {

    public enum Direction {
        NEXT, PREVIOUS
    }

    private final Direction direction;

    public NoteNavigationEvent(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "NoteNavigationEvent{direction=" + direction + '}';
    }
}