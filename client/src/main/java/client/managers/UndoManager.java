package client.managers;

import client.InjectorProvider;
import client.event.EventBus;
import client.event.RedoRequestedEvent;
import client.event.UndoRequestedEvent;
import client.event.UndoableActionEvent;
import jakarta.inject.Inject;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {

    private final Deque<UndoableActionEvent> undoStack = new ArrayDeque<>();
    private final Deque<UndoableActionEvent> redoStack = new ArrayDeque<>();
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(EventBus.class);

    @Inject
    public UndoManager() {
        this.eventBus.subscribe(UndoableActionEvent.class, this::handleUndoableAction);
        this.eventBus.subscribe(UndoRequestedEvent.class, event -> undo());
        this.eventBus.subscribe(RedoRequestedEvent.class, event -> redo());
    }

    private void handleUndoableAction(UndoableActionEvent event) {
        undoStack.push(event);
        redoStack.clear(); // Clear redo stack on any new action
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            UndoableActionEvent action = undoStack.pop();
            System.out.println("popped from stack: " + action);
            action.undo();
            redoStack.push(action);
        } else {
            System.out.println("Nothing to undo.");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            UndoableActionEvent action = redoStack.pop();
            eventBus.publish(action);
            undoStack.push(action); // Add the redone action back to the undo stack
        } else {
            System.out.println("Nothing to redo.");
        }
    }


    public void clearUndoStack() {
        undoStack.clear();
        redoStack.clear();
    }
}