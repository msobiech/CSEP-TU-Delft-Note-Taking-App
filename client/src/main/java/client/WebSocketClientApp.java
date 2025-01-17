package client;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public class WebSocketClientApp extends WebSocketClient {


    public WebSocketClientApp(URI serverUri) {
        super(serverUri);
        System.out.println("WebSocketClientApp started");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
        send("Hello from client!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received from server: " + message);
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
        URI serverUri = URI.create("ws://localhost:3030");
        this.connect();
    }
}
