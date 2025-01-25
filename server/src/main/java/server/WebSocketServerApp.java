package server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebSocketServerApp extends WebSocketServer {

    public WebSocketServerApp(int port) {
        super(new InetSocketAddress(port));
    }

    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

    public WebSocketServerApp(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message received from " + conn.getRemoteSocketAddress() + ": " + message);
        if(message.split(" ")[0].equals("NewChangeDetected")){
            broadcastChange(conn, message.split("NewChangeDetected ")[1]);
            System.out.println("change broadcasted");
        } else if (message.contains("UpdatedChangedNote")){
            broadcastChangeContent(conn, message);
        } else if (message.contains("UpdatedNoteTitle")) {
            broadcastTitleChange(message);

        }
        String formattedMessage = "";
        if(message.contains("noteAdded") || message.contains("noteDeleted")){
            formattedMessage = message.split(" ")[1].trim();
        }
        switch(formattedMessage){
            case "noteAdded" :
                broadcastAdd();
                System.out.println("addition broadcasted");
                break;
            case "noteDeleted":
                broadcastDelete();
                System.out.println("Delete broadcasted");
                break;
            case "refreshNotes":
                broadcastRefresh();
                break;
            default:
                System.out.println("Command not recognized: " + message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully");
    }

    public void startWebSocketServer() {
        WebSocketServerApp server = new WebSocketServerApp(8008);
        server.start();
        System.out.println("WebSocket server started on ws://localhost:8008");
    }

    public void broadcastAdd() {
        synchronized (connections) {
            for (WebSocket conn : connections) {
                if (conn.isOpen()) {
                    conn.send("noteAdded");
                }
            }
        }
    }

    public void broadcastDelete() {
        synchronized (connections) {
            for (WebSocket conn : connections) {
                if (conn.isOpen()) {
                    conn.send("noteDeleted");
                }
            }
        }
    }

    public void broadcastChange(WebSocket conn, String change){
        synchronized (connections) {
            for (WebSocket client : connections) {
                if (client.isOpen() && !client.equals(conn)) {
                    client.send(change);
                }
            }
        }
    }

    public void broadcastChangeContent(WebSocket conn, String change){
        try {
            int x = Integer.parseInt(change.split(" ")[0]);
            synchronized (connections) {
                for (WebSocket client : connections) {
                    if (client.isOpen() && !conn.equals(client)) {
                        client.send(change);
                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error here buddy: "+ e);
        }
    }

    public void broadcastTitleChange(String change){
        try {
            synchronized (connections) {
                for (WebSocket client : connections) {
                    if (client.isOpen()) {
                        client.send(change);
                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error here buddy " + e );
        }
    }

    public void broadcastRefresh(){
        try {
            synchronized (connections) {
                for (WebSocket client : connections) {
                    if (client.isOpen()) {
                        client.send("refreshNotes");
                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error here buddy " + e);
        }
    }
}

