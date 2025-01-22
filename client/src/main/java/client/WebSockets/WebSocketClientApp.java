package client.WebSockets;


import client.controllers.NoteOverviewCtrl;
import com.google.inject.Inject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetSocketAddress;
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
        switch(message){
            case "noteAdded" :
                System.out.println("addition received by " + this );
                break;
            case "noteDeleted":
                System.out.println("Delete received");
                break;
            default:
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server. Reason: " + reason);
        GlobalWebSocketManager.removeConnection(this);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void startClient(){
        this.connect();
    }

    public void broadcastAdd() {
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                webSocketClientApp.send("noteAdded");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
        }
        webSocketClientApp.close();

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        reserve = this.id;
        this.id = id;
    }

    public void revertId(){
        this.id = reserve;
    }

    public void broadcastDelete(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                webSocketClientApp.send("noteDeleted");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
        }
        webSocketClientApp.close();

    }

    public void broadcastChange(String change){
        try {
            if (this.connectBlocking()) {
                this.send("NewChangeDetected "+change);

            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
        }

    }

    public void broadcastContent(String change, Long id){
        System.out.println("content arrived");
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                System.out.println("content broadcast");
                webSocketClientApp.send(NoteOverviewCtrl.getId() +" " +id +" UpdatedChangedNote " + change);
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException e) {

        }
        webSocketClientApp.close();
    }

    public int getLocalPort() {
        if (getConnection() != null) {
            InetSocketAddress localSocketAddress = getConnection().getLocalSocketAddress();
            if (localSocketAddress != null) {
                return localSocketAddress.getPort();
            }
        }
        return -1; // Return -1 if not connected
    }

    public void broadcastTitle(String title, Long id){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                System.out.println("title broadcast");
                webSocketClientApp.send(NoteOverviewCtrl.getId() +" " +id +" UpdatedNoteTitle " + title);
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException e) {

        }
        webSocketClientApp.close();
    }

    public void broadcastRefresh(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if(webSocketClientApp.connectBlocking()){
                webSocketClientApp.send("refreshNotes");

            }
        } catch (InterruptedException e) {
            System.out.println("Problem here buddy");
        }
        webSocketClientApp.close();
    }
}
