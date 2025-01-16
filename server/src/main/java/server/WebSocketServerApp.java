package server;

import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;

public class WebSocketServerApp extends WebSocketServer {

    public WebSocketServerApp(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(org.java_websocket.WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        conn.send("Received: " + message);
    }

    @Override
    public void onError(org.java_websocket.WebSocket conn, Exception ex) {
        ex.printStackTrace();
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
}
