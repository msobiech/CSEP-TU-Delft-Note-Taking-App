package client.WebSockets;


import com.google.inject.Inject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public class WebSocketClientApp extends WebSocketClient {


    @Inject
    public WebSocketClientApp(URI serverUri) {
        super(serverUri);
//        System.out.println("FUCKCCKCKC");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
        send("Hello from client!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received from server: " + message);
        switch(message){
            case "noteAdded" :
                System.out.println("addition received");
                break;
            case "noteDeleted":
                System.out.println("Delete received");
                break;
            default:
                System.out.println("Command not recognized: " + message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server. Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void startClient(){
        this.connect();
    }

    public void broadcastAdd(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                webSocketClientApp.send("noteAdded");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
        }

    }
    public void broadcastDelete(){
        WebSocketClientApp webSocketClientApp = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        try {
            if (webSocketClientApp.connectBlocking()) {
                Thread.sleep(200);
                webSocketClientApp.send("noteDeleted");
            } else {
                System.err.println("WebSocket is not connected!");
            }
        } catch (InterruptedException _) {
        }

    }
}
