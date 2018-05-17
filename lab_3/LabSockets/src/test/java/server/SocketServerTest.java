package server;

import client.SocketClient;
import external.diff_match_patch;
import models.Message;
import models.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class SocketServerTest {
    private static final int TEST_PORT = 7777;

    private SocketServer socketServer;

    private SocketClient socketClient1;
    private SocketClient socketClient2;
    private SocketClient socketClient3;
    private Message testMessage;
    private diff_match_patch dmp;

    @Before
    public void setUp() throws Exception {
        dmp = new diff_match_patch();
        testMessage = new Message(dmp.patch_make("qwty", "qwerty"), "TestAuthor", 0);
        socketServer = new SocketServer(TEST_PORT);
        socketServer.startListening();
    }

    @Test
    public void testMessaging() throws Exception {
        AtomicInteger calls = new AtomicInteger(0);

        socketClient1 = new SocketClient("localhost", TEST_PORT, jsonNode -> {
            assertEquals(testMessage, Messages.toMessage(jsonNode));
            calls.incrementAndGet();
        });
        socketClient1.send(Messages.Type.TEXT, testMessage);

        socketClient2 = new SocketClient("localhost", TEST_PORT, jsonNode -> {
            assertEquals(testMessage, Messages.toMessage(jsonNode));
            calls.incrementAndGet();
        });

        socketClient3 = new SocketClient("localhost", TEST_PORT, jsonNode -> {
            assertEquals(testMessage, Messages.toMessage(jsonNode));
            calls.incrementAndGet();
        });


        // guarantee all threads finished
        waitUntilNoError(5000L, () -> {
            assertEquals(calls.get(), 3);
        });
    }

    private static  void waitUntilNoError(long millis, Runnable runnable) {
        long end = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < end) {
            try {
                runnable.run();
                return;
            } catch (Throwable e1) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e2) {
                    throw new RuntimeException("Interrupted exception", e1);
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        socketClient1.close();
        socketClient2.close();
        socketClient3.close();
        socketServer.close();
    }
}