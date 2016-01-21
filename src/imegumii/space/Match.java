package imegumii.space;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by imegumii on 1/18/16.
 */
public class Match extends Thread {

    private State currentState;
    private ArrayList<Connection> participants;
    private Connection selectedParticipant;
    private int currentTime = 0;

    public Match(ArrayList<Connection> list) {
        System.out.println("Starting match");
        this.participants = new ArrayList<>(list);
        participants.forEach(p -> p.setMatch(this));
        // TODO PlayerReady (ready : true)

    }

    public void sendMessageToAllClients(String s) {
        sendToAllClients(false, s + Main.NEWLINE);
    }

    public void pictureTaken() {
        currentState = State.Running;
    }

    public void matchWon(Connection c) {
        //Game won, send GameEnded
        assignPoints(c);
        JSONObject o = new JSONObject();
        o.put("command", Connection.Commands.GameEnded.toString());
        o.put("winner", c.getPlayerName());
        HashMap<String, HashMap<String, Double>> hashMapHashMap = new HashMap<>();
        HashMap<String, Double> hm;
        for (Connection x : participants) {
            hm = new HashMap<>();
            hm.put("points", x.getPoints());
            hm.put("pointstotal", x.getPointstotal());
            hashMapHashMap.put(x.getPlayerName(), hm);
        }
        o.put("players", hashMapHashMap);
        System.out.println(o);
        participants.forEach(p -> p.sendJSONMessage(o));
    }

    public void assignPoints(Connection winner)
    {
        participants.forEach(p->{
            if (p == winner) {
                //Appends to pointstotal and points
                winner.addPoints(100);
            }
        });
    }

    @Override
    public void run() {
        //handle game logic
        pickSelected();
        currentState = State.Started;
        while (true) {
            if (currentState == State.Running) {
                //Game running
                try {
                    Thread.sleep(1000);
                    currentTime++;
                    if (currentTime >= 300) {
                        //Pic taker wins.
                        matchWon(selectedParticipant);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (currentState == State.Started) {
                //Tell selected player to take a picture
                JSONObject o = new JSONObject();
                o.put("command", Connection.Commands.Picture.toString());
                participants.forEach(p -> {
                    JSONObject temp = o;
                    if (p == selectedParticipant) {
                        temp.put("selected", true);
                    } else {
                        temp.put("selected", false);
                    }
                    p.sendJSONMessage(temp);
                });
                currentState = State.Waiting;
            }
            if (currentState == State.Stopped) {
                //Game ended, bubay
                System.out.println("Game took: " + currentTime + " seconds");
                //Wait for gameready from clients
                final int[] i = {0};
                participants.forEach(p -> {
                    if (p.isReady()) {
                        i[0]++;
                    }
                    if (i[0] >= participants.size()) {
                        //start new game
                        pickSelected();
                        currentState = State.Started;
                        currentTime = 0;
                        participants.forEach(c->{c.clearPoints();c.setReady(false);});
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (currentState == State.Waiting) {
                //Waiting, sleep in order to preserve CPU cycles.
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void pickSelected() {
        Connection c = participants.get((int) (Math.random() * (participants.size())));
        selectedParticipant = c;

        //TODO, remove preference for kennyboy55
        participants.forEach(p -> {
            if (p.getPlayerName().equals("kennyboy55")) {
                selectedParticipant = p;
            }
        });
    }

    public void sendToAllClients(boolean binary, Object content) {
        participants.forEach(client -> {
            client.handleCommand(binary, content);
        });
    }

    public enum State {
        Running,
        Stopped,
        Started,
        Waiting
    }
}
