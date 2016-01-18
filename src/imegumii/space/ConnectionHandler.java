package imegumii.space;

import java.io.*;
import java.net.Socket;

/**
 * Created by imegumii on 1/18/16.
 */
public class ConnectionHandler extends Thread {

    private Socket socket;
    private Server server;

    public ConnectionHandler(Server server, Socket s) {
        socket = s;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("Starting thread: " + socket.getInetAddress());
        handleConnection();
    }

    public void handleCommand(boolean binary, Object content) {
        if (binary) {
            System.out.println(content);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            //// TODO: 1/18/16

        } else {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                bw.write((String) content);
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleConnection() {
        System.out.println("Handling connection");

        boolean isDone = false;

        try {
            BufferedReader lines = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            while (!isDone) {
                String readLine = lines.readLine();
                System.out.println("Received line: " + readLine);
                bw.write(readLine + Main.NEWLINE);
                bw.flush();
                switch (readLine) {
                    case "test":
                        server.sendToAllClients(false, "Hallo, dit is een globaal testbericht" + Main.NEWLINE);
                        break;
                    case "quit":
                        lines.close();
                        bw.close();
                        socket.close();
                        isDone = true;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
