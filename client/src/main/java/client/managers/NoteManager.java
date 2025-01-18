package client.managers;

import client.InjectorProvider;
import client.WebSockets.WebSocketClientApp;
import client.controllers.NoteOverviewCtrl;
import client.event.EventBus;
import client.event.MainEventBus;
import client.event.NoteEvent;
import client.event.NoteEvent.EventType;
import client.event.NoteStatusEvent;
import client.utils.NoteService;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class NoteManager {
    private static final EventBus eventBus = InjectorProvider.getInjector().getInstance(MainEventBus.class);
    private static int changeCountTitle = 0;
    private static int changeCountContent = 0;
    private Timer debounceTimer = new Timer();
    private Runnable lastTask = null;
    private final NoteService noteService;
    private final int DELAY = 1000;
    private static final int THRESHOLD = 5;
    private final ServerUtils server;

    private final WebSocketClientApp webSocketClientApp;




    public NoteManager(NoteService noteService, ServerUtils server) {
        this.noteService = noteService;
        this.server = server;
        eventBus.subscribe(NoteEvent.class, this::handleContentChange);
        webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
    }

    private void handleContentChange(NoteEvent event) {
        EventType type = event.getEventType();
        System.out.println(event + " has been received by " + this.getClass().getSimpleName());
        switch(type){
            case TITLE_CHANGE:
                handleNoteTitleChanged(event);
                break;
            case CONTENT_CHANGE:
                handleNoteContentChanged(event);
                break;
            case NOTE_ADD:
                handleNoteAddition();
                webSocketClientApp.broadcastAdd();
                break;
            case NOTE_REMOVE:
                handleNoteDeletion((NoteStatusEvent) event);
                webSocketClientApp.broadcastDelete();
                break;

        }
    }

    private void handleNoteContentChanged(NoteEvent event) {
        changeCountContent++; // Count the amount of changes
        debounce(() -> {
            if (event.getNoteId() != null) {
                try {
                    noteService.updateNoteContent(event.getChange(), event.getNoteId());
                    changeCountContent = 0;
                } catch (Exception e) {
                    System.err.println("Failed to update content: " + e.getMessage());
                }
            }
        }, DELAY);
        if (changeCountContent >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
            if (event.getNoteId() != null) {
                try {
                    noteService.updateNoteContent(event.getChange(), event.getNoteId());
                    changeCountContent = 0;
                    debounceTimer.cancel(); // Cancel any pending debounced update
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
        changeCountTitle++;
        debounce(() -> {
            try {
                noteService.updateNoteTitle(event.getChange(), event.getNoteId());
                System.out.println("Note " + event.getNoteId() + " title update handled successfully.");
            } catch (Exception e) {
                System.err.println("Failed to update title: " + e.getMessage());
            }
        },DELAY);
        if (changeCountTitle >= THRESHOLD) { // If the change count is bigger than the threshold set (here 5 characters) we need to update
            try {
                noteService.updateNoteTitle(event.getChange(), event.getNoteId());
                System.out.println("Note " + event.getNoteId() + " title update handled successfully.");
                changeCountTitle = 0;
            } catch (Exception e) {
                System.err.println("Failed to update title: " + e.getMessage());
            }
            debounceTimer.cancel(); // Cancel any pending debounced update
        }
    }

    private void handleNoteAddition(){
        server.addNote();
    }

    private void handleNoteDeletion(NoteStatusEvent event) {
        try {
            if (event.getChangeID() != null) {
                // Delete the note from the backend
                server.deleteNoteByID(event.getChangeID());
                System.out.println("Note " + event.getChangeID() + " deleted successfully.");

                // Update the UI (must be done on the JavaFX thread)

                    // Assuming there's a way to get the ObservableList from the controller
                    NoteOverviewCtrl controller = InjectorProvider.getInjector().getInstance(NoteOverviewCtrl.class);
                    ObservableList<Pair<Long, String>> notes = controller.getNotes();

                    // Find and remove the note
                    if(notes!=null){
                        notes.removeIf(note -> note.getKey().equals(event.getChangeID()));
                        controller.getNotesList().refresh();
                    }

                }else {
                System.err.println("Note ID is null. Cannot delete note.");
            }
        } catch (Exception e) {
            System.err.println("Error while deleting note: " + e.getMessage());
        }
    }

}

