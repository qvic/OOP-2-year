package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public EchoClient(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public Message sendAndGet(Message message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        out.println(mapper.writeValueAsString(message));

        return mapper.readValue(in.readLine(), Message.class);
    }

    public void close() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
