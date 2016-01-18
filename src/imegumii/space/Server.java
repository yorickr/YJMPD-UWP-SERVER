package imegumii.space;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by imegumii on 1/18/16.
 */
public class Server {

    private static final int PORT = 3333;

    ServerSocket serverSocket;

    private ArrayList<ConnectionHandler> activeConnections;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            activeConnections = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void removeClient(ConnectionHandler c)
    {
        activeConnections.remove(c);
    }

    public void sendToAllClients(boolean binary, Object content) {
        activeConnections.forEach(client -> {
            client.handleCommand(binary, content);
        });
    }

    public void handleConnections() {
        while (true) {
            try {
                Socket s = serverSocket.accept();
                ConnectionHandler ch = new ConnectionHandler(this, s);
                activeConnections.add(ch);
                ch.start();
                System.out.println("Accepted new connection: " + s.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
