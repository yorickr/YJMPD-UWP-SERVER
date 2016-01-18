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

    private ArrayList<Connection> activeConnections;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            activeConnections = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Timer thread to check if enough players available
        new Thread(()->
        {
            while(true)
            {
                if(activeConnections.size() >= 3)
                {
                    System.out.println("Enough players connected to begin match");
                    new Match(activeConnections).start();
                    activeConnections.clear();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void addClient(Connection c)
    {
        activeConnections.add(c);
    }

    public void removeClient(Connection c) {
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
                Connection c = new Connection(this, s);
                c.start();
                System.out.println("Accepted new connection: " + s.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
