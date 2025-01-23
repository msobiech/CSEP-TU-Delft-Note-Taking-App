package WebSocketServerTest;

import client.InjectorProvider;
import client.WebSockets.GlobalWebSocketManager;
import client.WebSockets.WebSocketMessageListener;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalWebsocketHandlerTest {

    private static GlobalWebSocketManager manager;
    @BeforeAll
    public static void setUp() {
        manager= InjectorProvider.getInjector().getInstance(GlobalWebSocketManager.class);

    }

    @Test
    public void testSingletonInstance() {
        GlobalWebSocketManager instance1 = GlobalWebSocketManager.getInstance();
        GlobalWebSocketManager instance2 = GlobalWebSocketManager.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    public void testSendMessage() {
        String serverUri = "ws://localhost:8008/websocket-endpoint";

        manager.initializeWebSocket(serverUri);

        String testMessage = "Test Message";
        manager.sendMessage(testMessage);
    }

    @Test
    public void testAddRemoveListeners() {
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

        manager.setNoteOverviewId(42);
        assertEquals(42, manager.getNoteOverviewId().intValue());
    }






}
