package server;

import client.SocketClient;
import external.diff_match_patch;
import models.Message;
import models.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class SocketServerTest {
    private static final int TEST_PORT = 7777;

    private SocketServer socketServer;

    private SocketClient socketClient1;
    private SocketClient socketClient2;
    private SocketClient socketClient3;
    private final Message testMessage;
    private diff_match_patch dmp;

    public SocketServerTest() {
        testMessage = new Message(dmp.patch_make("qwty", "qwerty"), "TestAuthor", 0);
    }

    @Before
    public void setUp() throws Exception {
        dmp = new diff_match_patch();
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