package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {

    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    private BufferedReader in;
    private PrintWriter out;
    private LinkedBlockingQueue<Message> messages;

    Connection(Socket socket, LinkedBlockingQueue<Message> messages) throws IOException {
        this.messages = messages;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    String requestString = in.readLine();
                    if (requestString != null) {
                        Message request = jsonObjectMapper.readValue(requestString, Message.class);
                        messages.put(request);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        reader.setDaemon(true);
        reader.start();
    }

    protected void sendMessage(Message message) {
        try {
            out.println(jsonObjectMapper.writeValueAsString(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
