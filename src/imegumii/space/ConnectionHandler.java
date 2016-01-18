package imegumii.space;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by imegumii on 1/18/16.
 */
public class ConnectionHandler extends Thread {

    private Socket socket;
    private Server server;

    private DataInputStream dis;
    private DataOutputStream dos;

    public ConnectionHandler(Server server, Socket s) {
        socket = s;
        this.server = server;
    }

    @Override
    public void run() {
        handleConnection();
    }

    public void handleConnection() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String readline = "";
                while ((readline = dis.readUTF()) != null) {
                    System.out.println(readline);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
