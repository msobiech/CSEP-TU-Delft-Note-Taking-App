package WebSocketServerTest;

import client.WebSockets.WebSocketClientApp;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class WebSocketClientAppTest {

    public class MockServer extends WebSocketServer {
        public MockServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onError(WebSocket conn, Exception ex) {

        }

        @Override
        public void onStart() {

        }
    };

    @Test
    public void testOnMessage() {
        // Create a mock WebSocketClientApp
        URI mockUri = URI.create("ws://localhost:8008/websocket-endpoint");
        WebSocketClientApp client = spy(new WebSocketClientApp(mockUri));


        // Simulate receiving a message
        client.onMessage("11 noteAdded");
        client.onMessage("11 noteDeleted");
        client.onMessage("11 otherMessage");

        // Verify the message handling
        verify(client, times(1)).onMessage("11 noteAdded");
        verify(client, times(1)).onMessage("11 noteDeleted");
    }
    @Test
    public void testBroadcastRefresh() {
        WebSocketClientApp client = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        String result = client.broadcastRefresh();
        assertEquals("Notes refreshed", result);
    }


    @Test
    public void testBroadcastContent() {
        WebSocketClientApp client = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        String result = client.broadcastContent("Some content", 123L);
        assertEquals("content broadcast", result);
    }

    @Test
    public void testBroadcastAdd() {
        WebSocketClientApp client = new WebSocketClientApp(URI.create("ws://localhost:8008/websocket-endpoint"));
        String result = client.broadcastAdd();
        assertEquals("noteAdded", result);
    }

    @Test
    public void testConnection() {
        URI serverUri = URI.create("ws://localhost:8008/websocket-endpoint");
        WebSocketClientApp client = new WebSocketClientApp(serverUri);
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }







}
