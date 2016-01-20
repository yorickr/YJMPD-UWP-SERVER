package imegumii.space;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

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

    public Connection(Server server, Socket s) {
        socket = s;
        this.server = server;
        points = 0;
        pointstotal = 0;
    }

    @Override
    public String toString() {
        return "hallo";
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

    public void setPoints(double points) {
        this.points = points;
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
            } catch (Exception e) {
                e.printStackTrace();
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
            while (!isDone) {
                String readLine = br.readLine();
                System.out.println("Received line: " + readLine);

                try {
                    JSONObject o = new JSONObject(readLine);
                    JSONObject tempObj = new JSONObject();
                    String s = o.getString("command");
                    System.out.println(Commands.valueOf(s));
                    switch (Commands.valueOf(s)) {
                        case Hi:
                            //succesful connection
                            tempObj = new JSONObject();
                            tempObj.put("command", Commands.Msg.toString());
                            tempObj.put(Commands.Msg.toString(), "hi there");
                            sendJSONMessage(tempObj);
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
            server.removeClient(this);
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
        PlayerReady
    }
}
