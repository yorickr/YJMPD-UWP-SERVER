package imegumii.space;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by imegumii on 1/18/16.
 */
public class Connection extends Thread {

    private Socket socket;
    private Server server;
    private Match match;

    private String name;
    private BufferedReader br;
    private BufferedWriter bw;

    private double lon;
    private double lat;

    private double points;
    private double pointstotal;

    private boolean ready;

    private boolean running = true;

    public Connection(Server server, Socket s) {
        socket = s;
        try {
            socket.setSoTimeout(100000);
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.server = server;
        points = 0;
        pointstotal = 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public double getPointstotal() {
        return pointstotal;
    }

    public void setPointstotal(double pointstotal) {
        this.pointstotal = pointstotal;
    }

    public double getPoints() {
        return points;
    }

    public void clearPoints()
    {
        this.points = 0;
    }

    public void addPoints(double points) {
        this.points += points;
        this.pointstotal += points;
    }

    @Override
    public void run() {
        System.out.println("Starting thread: " + socket.getInetAddress());
        handleConnection();
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public void handleCommand(boolean binary, Object content) {
        if (binary) {
            System.out.println(content);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            //// TODO: 1/18/16, BINARY

        } else {
            try {
                bw.write((String) content);
                bw.flush();
            } catch (java.net.SocketException e) {
                e.printStackTrace();
                try {
                    bw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                running = false;
            }
        }
    }

    public void sendJSONMessage(JSONObject message) {
        System.out.println("Sending: " + message.toString());
        handleCommand(false, message.toString() + Main.NEWLINE);
    }

    public void handleConnection() {
        System.out.println("Handling connection");

        boolean isDone = false;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            while (!isDone && running) {
                String readLine = br.readLine();
                System.out.println("Received line: " + readLine);

                try {
                    JSONObject o = new JSONObject(readLine);
                    JSONObject tempObj = new JSONObject();
                    String s = o.getString("command");
                    System.out.println(Commands.valueOf(s));
                    switch (Commands.valueOf(s)) {
                        case Hi:
                            //Keep alive ping
                            System.out.println("Ping received from " + socket.getInetAddress() + "!");
                            break;
                        case Name:
                            //Received name from client
                            this.name = o.getString("name");
                            this.lon = o.getDouble("lon");
                            this.lat = o.getDouble("lat");
                            tempObj = new JSONObject();
                            tempObj.put("command", Commands.Msg.toString());
                            tempObj.put(Commands.Msg.toString(), "ok");
                            sendJSONMessage(tempObj);
                            server.addClient(this);
                            break;
                        case PlayerRemoved:
                            if(match != null)
                            {
                                match.sendToAllClients(new JSONObject().put("command", Commands.StopGame.toString()));
                            }
                            server.removeClient(this);
                            break;
                        case PictureUrl:
                            if (match != null) {
                                match.sendMessageToAllClients(readLine);
                                match.pictureTaken();
                            }
                            break;
                        case DestinationReached:
                            if (match != null) {
                                match.matchWon(this);

                            }
                            break;
                        case PlayerReady:
                            if (o.getBoolean("ready")) {
                                //player is ready
                                ready = true;
                            }
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //// TODO: 1/18/16 REMOVE THIS, DEBUG ONLY
                switch (readLine) {
                    case "quit":
                        br.close();
                        bw.close();
                        socket.close();
                        server.removeClient(this);
                        isDone = true;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!(e instanceof NullPointerException)) {

                server.removeClient(this);

            }
        }

    }

    public String getPlayerName() {
        return name;
    }

    public enum Commands {
        Hi,
        Name,
        Msg,
        Picture,
        PlayerJoined,
        PlayerRemoved,
        PictureUrl,
        DestinationReached,
        GameEnded,
        PlayerReady,
        StopGame
    }
}
