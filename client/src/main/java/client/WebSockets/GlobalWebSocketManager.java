package client.WebSockets;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GlobalWebSocketManager {

    // Singleton instance
    private static GlobalWebSocketManager instance;

    // WebSocket client instance
    private WebSocketClientApp webSocketClient;

    // Listeners to notify on message reception
    private final List<WebSocketMessageListener> listeners = new ArrayList<>();

    // Private constructor to enforce singleton pattern
    private GlobalWebSocketManager() { }

    // Get the singleton instance
    public static synchronized GlobalWebSocketManager getInstance() {
        if (instance == null) {
            instance = new GlobalWebSocketManager();
        }
        return instance;
    }

    // Initialize the WebSocket connection
    public void initializeWebSocket(String serverUri) {
        try {
            URI uri = new URI(serverUri);
            webSocketClient = new WebSocketClientApp(uri) {

                @Override
                public void onMessage(String message) {
                    System.out.println("Message received: " + message);
                    // Notify all listeners of the new message
                    for (WebSocketMessageListener listener : listeners) {
                        listener.onMessageReceived(message);
                    }
                }
            };

            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send a message through the WebSocket
    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            System.out.println("WebSocket is not open. Cannot send message.");
        }
    }

    // Add a listener to receive messages
    public void addMessageListener(WebSocketMessageListener listener) {
        listeners.add(listener);
    }

    // Remove a listener
    public void removeMessageListener(WebSocketMessageListener listener) {
        listeners.remove(listener);
    }
}

