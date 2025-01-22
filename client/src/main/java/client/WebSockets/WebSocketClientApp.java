package client.WebSockets;


import client.controllers.NoteOverviewCtrl;
import com.google.inject.Inject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public class WebSocketClientApp extends WebSocketClient {

    private Integer id = 0;
    private Integer reserve = 0;
    @Inject
    public WebSocketClientApp(URI serverUri) {
        super(serverUri);
        System.out.println("Connection established");
        this.id += 1;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        GlobalWebSocketManager.addConnection(this);
    }

    @Override
    public void onMessage(String message) {
        if(Integer.parseInt(message.split(" ")[0]) == NoteOverviewCtrl.getId() && (message.split(" ")).length == 2){
            String actualMessage = message.split(" ")[1];
            switch(actualMessage){
                case "noteAdded" :
                    System.out.println("addition received");
                    break;
                case "noteDeleted":
                    System.out.println("Delete received");
                    break;
                default:
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server. Reason: " + reason);
        GlobalWebSocketManager.removeConnection(this);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error caught "+ ex.getCause());
    }

    public void startClient(){
        this.connect();
    }

    public String broadcastAdd(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                webSocketClientApp.send(NoteOverviewCtrl.getId()+ " noteAdded");
                return "noteAdded";
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
            return "ErrorCaught";
        }
        return null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        reserve = this.id;
        this.id = id;
    }


    public String broadcastDelete(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                webSocketClientApp.send(NoteOverviewCtrl.getId()+ " noteDeleted");
                return "noteDeleted";
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
            return ("ErrorCaught");
        }
        return null;
    }

    public String broadcastChange(String change){
        try {
            if (this.connectBlocking()) {
                this.send("NewChangeDetected "+change);
                return ("NewChangeDetected");

            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
            return ("Erro Caught");
        }
        return null;
    }

    public String broadcastContent(String change, Long id){
        System.out.println("content arrived");
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                System.out.println("content broadcast");
                webSocketClientApp.send(NoteOverviewCtrl.getId() +" " +id +" UpdatedChangedNote " + change);
                return ("content broadcast");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException e) {
            return "Error Caught";

        }
        return null;
    }


    public String broadcastTitle(String title, Long id){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                System.out.println("title broadcast");
                webSocketClientApp.send(NoteOverviewCtrl.getId() +" " +id +" UpdatedNoteTitle " + title);
                return ("Title broadcasted");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException e) {
            return ("Error caught");

        }
        return null;
    }

    public String broadcastRefresh(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if(webSocketClientApp.connectBlocking()){
                webSocketClientApp.send("refreshNotes");
                return ("Notes refreshed");

            }
        } catch (InterruptedException e) {
            System.out.println("Problem here buddy");
            return "Error Caught";
        }
        return null;
    }
}
