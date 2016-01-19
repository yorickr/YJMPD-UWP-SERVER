package imegumii.space;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by imegumii on 1/18/16.
 */
public class Match extends Thread{

    public enum State{
        Running,
        Stopped,
        Started,
        Waiting
    }

    private State currentState;

    private ArrayList<Connection> participants;

    private Connection selectedParticipant;

    public Match(ArrayList<Connection> list) {
        System.out.println("Starting match");
        this.participants = new ArrayList<>(list);
    }

    @Override
    public void run() {
        //handle game logic
        pickSelected();
        currentState = State.Started;
        while (true) {
            if (currentState == State.Running) {
                //Game running
            }
            if (currentState == State.Started) {
                //Tell selected player to take a picture
                JSONObject o = new JSONObject();
                o.put("command", Connection.Commands.Picture.toString());
                participants.forEach(p ->{
                    JSONObject temp = o;
                    if (p == selectedParticipant) {
                        temp.put("selected", true);
                    }
                    else
                    {
                        temp.put("selected", false);
                    }
                    p.sendJSONMessage(temp);
                });
                currentState = State.Waiting;
            }
            if (currentState == State.Stopped) {
                //Game ended, bubay
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

    private void pickSelected()
    {
        Connection c = participants.get((int) (Math.random() * participants.size()));
        selectedParticipant = c;
    }

    public void sendToAllClients(boolean binary, Object content) {
        participants.forEach(client -> {
            client.handleCommand(binary, content);
        });
    }
}
