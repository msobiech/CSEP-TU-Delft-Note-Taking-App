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
        switch(message){
            case "noteAdded" :
                broadcastAdd();
                System.out.println("addition broadcasted");
                break;
            case "noteDeleted":
                broadcastDelete();
                System.out.println("Delete broadcasted");
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
}
