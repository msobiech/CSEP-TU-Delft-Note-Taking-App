package client.managers;

import client.InjectorProvider;
import client.WebSockets.WebSocketClientApp;
import client.controllers.NoteOverviewCtrl;
import client.event.*;
import client.event.NoteEvent.EventType;
import client.utils.NoteService;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import models.Note;

import java.net.URI;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class NoteManager {
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);
    private final NoteOverviewCtrl noteOverviewCtrl;
    private static int changeCountTitle = 0;
    private static int changeCountContent = 0;
    private Timer debounceTimer = new Timer();
    private Runnable lastTask = null;
    private final NoteService noteService;
    private final int DELAY = 1000;
    private static final int THRESHOLD = 5;
    private final ServerUtils server;

    private WebSocketClientApp webSocketClientApp;




    public NoteManager(NoteService noteService, ServerUtils server, NoteOverviewCtrl noteOverviewCtrl, WebSocketClientApp app) {

        this.noteService = noteService;
        this.server = server;
        this.noteOverviewCtrl = noteOverviewCtrl;
        eventBus.subscribe(NoteEvent.class, this::handleContentChange);
        webSocketClientApp = app;
    }

    private void handleContentChange(NoteEvent event) {
        EventType type = event.getEventType();
        switch(type){
            case TITLE_CHANGE:
                handleNoteTitleChanged(event);
                webSocketClientApp.broadcastRefresh();
                break;
            case CONTENT_CHANGE:
                handleNoteContentChanged(event);
                break;
            case NOTE_ADD:
                handleNoteAddition();
                webSocketClientApp.broadcastRefresh();
                break;
            case NOTE_REMOVE:
                handleNoteDeletion((NoteStatusEvent) event);
                webSocketClientApp.broadcastRefresh();
                break;

        }
    }

    private void handleNoteContentChanged(NoteEvent event) {
        changeCountContent++; // Count the amount of changes
        debounce(() -> {
            if (event.getNoteId() != null) {
                try {
                    String previousContent = noteService.getNoteContent(event.getNoteId()); // Fetch current content before change
                    noteService.updateNoteContent(event.getChange(), event.getNoteId());
                    eventBus.publish(new UndoableActionEvent(
                            event.getNoteId(),
                            UndoableActionEvent.ActionType.EDIT_TEXT,
                            previousContent,
                            state -> {
                                noteService.updateNoteContent((String) state, event.getNoteId());
                                Platform.runLater(() -> {
                                    noteOverviewCtrl.refreshNotes();
                                    noteOverviewCtrl.updateNoteDisplay((String) state);
                                });
                            }
                    ));
                } catch (Exception e) {
                    System.err.println("Failed to update content: " + e.getMessage());
                }
            }
        }, DELAY);
        if (changeCountContent >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
            debounceTimer.cancel();
            if (event.getNoteId() != null) {
                try {
                    String previousContent = noteService.getNoteContent(event.getNoteId());
                    noteService.updateNoteContent(event.getChange(), event.getNoteId());
                    changeCountContent = 0;
                    eventBus.publish(new UndoableActionEvent(
                            event.getNoteId(),
                            UndoableActionEvent.ActionType.EDIT_TEXT,
                            previousContent,
                            state -> {
                                noteService.updateNoteContent((String) state, event.getNoteId());
                                Platform.runLater(() -> {
                                    noteOverviewCtrl.refreshNotes();
                                    noteOverviewCtrl.updateNoteDisplay((String) state);
                                });
                            }
                    ));
                } catch (Exception e) {
                    System.err.println("Failed to update content: " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method allows you to schedule a task that will be executed in the given delay
     * @param task to schedule
     * @param delayMillis delay which we wait
     * Nice explanation of the concept <a href="https://www.geeksforgeeks.org/debouncing-in-javascript/">...</a>
     */
    private void debounce(Runnable task, int delayMillis) {
        debounceTimer.cancel(); // If the previously scheduled task is still there it means that the inactivity wasn't long enough and we can cancel.
        debounceTimer = new Timer(); // Setup new timer

        lastTask = task;

        debounceTimer.schedule(new TimerTask() { // Schedule new task TimerTask will schedule the task on your timer and execute in a given time
            @Override
            public void run() {
                Platform.runLater(task); // Run the task on a platform thread
            }
        }, delayMillis);
    }

    private void handleNoteTitleChanged(NoteEvent event) {
        Long noteId = event.getNoteId();
        int noteIndex = event.getListIndex();
        String newTitle = event.getChange();
        WebSocketClientApp webSocketClientApp1 = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        if (noteId == null || noteIndex == -1 || newTitle == null) {
            System.err.println("Invalid title change event.");
            return;
        }
        try {
            if (noteService.titleExists(newTitle)) {
                System.err.println("This title is already in use. Please choose a different title.");
                return;
            }
            String previousTitle = noteService.getNoteTitle(noteId); // Fetch the current title before updating

            ObservableList<Pair<Long, String>> notes = noteOverviewCtrl.getNotes();
            notes.set(noteIndex, new Pair<>(noteId, newTitle));


            // Update the title on the server
            noteService.updateNoteTitle(newTitle, noteId);
            webSocketClientApp1.broadcastTitle(newTitle,noteId);


            // Refresh the notes list
            Platform.runLater(noteOverviewCtrl::refreshNotes);

            // Publish an UndoableActionEvent for title change
            eventBus.publish(new UndoableActionEvent(
                    event.getNoteId(),
                    UndoableActionEvent.ActionType.EDIT_TITLE,
                    previousTitle,
                    state -> {
                        noteService.updateNoteTitle((String) state, noteId);
                        Platform.runLater(() -> {
                            notes.set(noteIndex, new Pair<>(noteId, (String) state));
                            noteOverviewCtrl.refreshNotes();
                        });
                    }
            ));
            System.out.println("Title updated successfully!");
        } catch (Exception e) {
            System.err.println("Failed to update the title: " + e.getMessage());
        }
    }

    private void handleNoteAddition() {
        ResourceBundle lang = noteOverviewCtrl.getLanguage();
        try {
            Note addedNote = server.addNote();
            System.out.println("ADDED NOTE: " + addedNote);
            Long noteId = addedNote.getId();
            String noteTitle = addedNote.getTitle();

            eventBus.publish(new UndoableActionEvent(
                    -1,
                    UndoableActionEvent.ActionType.ADD_FILE,
                    new Pair<>(noteId, noteTitle), // Store the note as a Pair for undo purposes
                    state -> server.deleteNoteByID(((Pair<Long, String>) state).getKey()) // Undo logic: Delete the added note
            ));
            noteOverviewCtrl.showFadeBox(lang.getString("good.add"), true);
        } catch (Exception e) {
            noteOverviewCtrl.showFadeBox(lang.getString("bad.add"), false);
        }
    }

    private void handleNoteDeletion(NoteStatusEvent event) {
        ResourceBundle lang = noteOverviewCtrl.getLanguage();
        try {
            if (event.getChangeID() != null) {
                // Delete the note from the backend
                server.deleteNoteByID(event.getChangeID());
                noteOverviewCtrl.showFadeBox(lang.getString("good.delete"), true);

                noteOverviewCtrl.getNoteListManager().handleNoteDeletion();

                    // Assuming there's a way to get the ObservableList from the controller
                    NoteOverviewCtrl controller = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);
                    ObservableList<Pair<Long, String>> notes = controller.getNotes();

                    // Find and remove the note
                    if(notes!=null){
                        notes.removeIf(note -> note.getKey().equals(event.getChangeID()));
                        controller.getNotesList().refresh();
                    }

                }else {
                noteOverviewCtrl.showFadeBox(lang.getString("bad.delete"), false);
            }
        } catch (Exception e) {
            noteOverviewCtrl.showFadeBox(lang.getString("bad.delete"), false);
        }
    }

}

