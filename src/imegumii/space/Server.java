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

    private ArrayList<Socket> activeConnections;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            activeConnections = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleConnections() {
        while (true) {
            try {
                Socket s = serverSocket.accept();
                activeConnections.add(s);
                new ConnectionHandler(this, s).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
