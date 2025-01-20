package client.event;

import java.util.function.Consumer;

public class UndoableActionEvent extends Event{

    public enum ActionType { EDIT_TEXT, EDIT_TITLE, ADD_FILE, MOVE_COLLECTION }

    private final ActionType type;
    private final Object previousState;
    private final Consumer<Object> undoLogic;

    public UndoableActionEvent(ActionType type, Object previousState, Consumer<Object> undoLogic) {
        this.type = type;
        this.previousState = previousState;
        this.undoLogic = undoLogic;
    }

    public void undo() {
        undoLogic.accept(previousState);
    }

    public ActionType getType() {
        return type;
    }

    public Object getPreviousState() {
        return previousState;
    }

    @Override
    public String toString() {
        return "UndoableActionEvent{" +
                "type=" + type +
                ", previousState=" + previousState +
                '}';
    }
}