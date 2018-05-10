package server;

import client.SocketClient;
import models.Message;
import models.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SocketServerTest {
    private static final int TEST_PORT = 7777;

    private SocketServer socketServer;

    private SocketClient socketClient1;
    private SocketClient socketClient2;
    private SocketClient socketClient3;
    private final Message testMessage;

    public SocketServerTest() {
        testMessage = new Message(Messages.Type.TEXT, "TestBody", "TestAuthor", new Date(0));
    }

    @Before
    public void setUp() throws Exception {
        socketServer = new SocketServer(TEST_PORT);
        socketServer.startListening();
    }

    @Test
    public void testMessaging() throws Exception {
        socketClient1 = new SocketClient("localhost", TEST_PORT, message -> {
            assertEquals(testMessage, message);
        });
        socketClient1.send(testMessage);

        socketClient2 = new SocketClient("localhost", TEST_PORT, message -> {
            assertEquals(testMessage, message);
        });

        socketClient3 = new SocketClient("localhost", TEST_PORT, message -> {
            assertEquals(testMessage, message);
        });

        // TODO guarantee all threads finished
    }

    @After
    public void tearDown() throws Exception {
        socketClient1.close();
        socketClient2.close();
        socketClient3.close();
        socketServer.close();
    }
}