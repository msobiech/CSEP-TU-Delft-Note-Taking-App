package WebSocketServerTest;

import client.WebSockets.GlobalWebSocketManager;
import client.WebSockets.WebSocketMessageListener;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalWebsocketHandlerTest {

    @Test
    public void testSingletonInstance() {
        GlobalWebSocketManager instance1 = GlobalWebSocketManager.getInstance();
        GlobalWebSocketManager instance2 = GlobalWebSocketManager.getInstance();

        assertSame(instance1, instance2); // Ensure both references point to the same object
    }

    @Test
    public void testSendMessage() {
        GlobalWebSocketManager manager = GlobalWebSocketManager.getInstance();
        String serverUri = "ws://localhost:8008/websocket-endpoint";

        manager.initializeWebSocket(serverUri);

        String testMessage = "Test Message";
        manager.sendMessage(testMessage);

        // Check logs or mock server to verify message receipt
    }

    @Test
    public void testAddRemoveListeners() {
        GlobalWebSocketManager manager = GlobalWebSocketManager.getInstance();
        WebSocketMessageListener listener = Mockito.mock(WebSocketMessageListener.class);

        manager.addMessageListener(listener);
        assertTrue(manager.getListeners().contains(listener));

        manager.removeMessageListener(listener);
        assertFalse(manager.getListeners().contains(listener));
    }

    @Test
    public void testAddRemoveConnections() {
        WebSocket mockConnection = Mockito.mock(WebSocket.class);

        GlobalWebSocketManager.addConnection(mockConnection);
        assertTrue(GlobalWebSocketManager.getConnections().contains(mockConnection));

        GlobalWebSocketManager.removeConnection(mockConnection);
        assertFalse(GlobalWebSocketManager.getConnections().contains(mockConnection));
    }

    @Test
    public void testNoteOverviewId() {
        GlobalWebSocketManager manager = GlobalWebSocketManager.getInstance();

        manager.setNoteOverviewId(42);
        assertEquals(42, manager.getNoteOverviewId().intValue());
    }






}
