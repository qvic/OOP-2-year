package socket;

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

    public String sendAndGet(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
